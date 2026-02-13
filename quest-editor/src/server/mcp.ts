import { McpServer } from '@modelcontextprotocol/sdk/server/mcp.js';
import { StdioServerTransport } from '@modelcontextprotocol/sdk/server/stdio.js';
import { z } from 'zod';

import { listFiles, readFile, readAll, writeFile, deleteFile } from './services/fileService.js';
import { listDrafts, getDraft, createDraft, updateDraft, deleteDraft, promoteDraft } from './services/draftService.js';
import { buildQuestGraph, getFactionQuestChain, validateQuestChain } from './services/questGraphService.js';
import { simulateWorldState } from './services/worldStateSimulator.js';
import { getCharacterArc } from './services/characterArcService.js';
import { getFactionConnections, getFactionRelationshipMatrix } from './services/factionAnalysisService.js';
import { promptTemplates } from './mcpPrompts.js';

const server = new McpServer({
  name: 'mc-rpg-quest-editor',
  version: '1.0.0'
});

// ─── Helper ──────────────────────────────────────────────────────────────────

function ok(result: unknown) {
  return { content: [{ type: 'text' as const, text: JSON.stringify(result, null, 2) }] };
}

function err(message: string) {
  return { content: [{ type: 'text' as const, text: `Error: ${message}` }], isError: true as const };
}

// ─── Data Tools ──────────────────────────────────────────────────────────────

server.tool(
  'list_quests',
  'List quests, optionally filtered by faction_id and/or level',
  { faction_id: z.string().optional(), level: z.number().optional() },
  async ({ faction_id, level }) => {
    const all = readAll('quests') as Record<string, any>;
    let quests = Object.values(all);
    if (faction_id) quests = quests.filter(q => q.faction_id === faction_id);
    if (level !== undefined) quests = quests.filter(q => q.level === level);
    return ok(quests);
  }
);

server.tool(
  'get_quest',
  'Get a single quest by ID',
  { id: z.string() },
  async ({ id }) => {
    const quest = readFile('quests', id);
    if (!quest) return err(`Quest "${id}" not found`);
    return ok(quest);
  }
);

server.tool(
  'get_faction_quest_chain',
  'Get ordered quest chain for a faction',
  { faction_id: z.string() },
  async ({ faction_id }) => {
    const chain = getFactionQuestChain(faction_id);
    return ok(chain);
  }
);

server.tool(
  'get_dialog_tree',
  'Get a dialog tree by ID',
  { id: z.string() },
  async ({ id }) => {
    const dialog = readFile('dialogs', id);
    if (!dialog) return err(`Dialog tree "${id}" not found`);
    return ok(dialog);
  }
);

server.tool(
  'list_characters',
  'List characters, optionally filtered by faction membership',
  { faction_id: z.string().optional() },
  async ({ faction_id }) => {
    if (faction_id) {
      const faction = readFile('factions', faction_id) as any;
      if (!faction) return err(`Faction "${faction_id}" not found`);
      const members = (faction.members || []) as string[];
      const characters = members
        .map(id => readFile('characters', id))
        .filter(c => c !== null);
      return ok(characters);
    }
    const all = readAll('characters');
    return ok(Object.values(all));
  }
);

server.tool(
  'get_character',
  'Get a character enriched with extras from all quests',
  { id: z.string() },
  async ({ id }) => {
    const character = readFile('characters', id) as any;
    if (!character) return err(`Character "${id}" not found`);

    // Scan all quests for character_extras mentioning this character
    const allQuests = readAll('quests') as Record<string, any>;
    const extras: { quest_id: string; branch_id: string; text: string }[] = [];
    for (const [qId, quest] of Object.entries(allQuests)) {
      for (const branch of quest.branches || []) {
        for (const extra of branch.outcomes?.character_extras || []) {
          if (extra.character === id) {
            extras.push({
              quest_id: quest.id || qId,
              branch_id: branch.id,
              text: extra.text
            });
          }
        }
      }
    }

    return ok({ ...character, quest_extras: extras });
  }
);

server.tool(
  'list_locations',
  'List locations, optionally filtered by tags (comma-separated)',
  { tags: z.string().optional() },
  async ({ tags }) => {
    const all = readAll('locations') as Record<string, any>;
    let locations = Object.values(all);
    if (tags) {
      const tagList = tags.split(',').map(t => t.trim().toLowerCase());
      locations = locations.filter(loc =>
        (loc.tags || []).some((t: string) => tagList.includes(t.toLowerCase()))
      );
    }
    return ok(locations);
  }
);

server.tool(
  'get_location',
  'Get a location by ID',
  { id: z.string() },
  async ({ id }) => {
    const location = readFile('locations', id);
    if (!location) return err(`Location "${id}" not found`);
    return ok(location);
  }
);

server.tool(
  'list_factions',
  'List all factions with full data',
  {},
  async () => {
    const all = readAll('factions');
    return ok(Object.values(all));
  }
);

server.tool(
  'get_faction',
  'Get a faction by ID',
  { id: z.string() },
  async ({ id }) => {
    const faction = readFile('factions', id);
    if (!faction) return err(`Faction "${id}" not found`);
    return ok(faction);
  }
);

// ─── Analysis Tools ──────────────────────────────────────────────────────────

server.tool(
  'get_quest_graph',
  'Build quest dependency graph, optionally scoped to a faction',
  { faction_id: z.string().optional() },
  async ({ faction_id }) => {
    const graph = buildQuestGraph(faction_id);
    return ok(graph);
  }
);

server.tool(
  'validate_quest_chain',
  'Validate quest chain for issues, optionally scoped to a faction',
  { faction_id: z.string().optional() },
  async ({ faction_id }) => {
    const result = validateQuestChain(faction_id);
    return ok(result);
  }
);

server.tool(
  'get_faction_connections',
  'Get connections between two factions (shared characters, locations, cross-references)',
  { faction_a: z.string(), faction_b: z.string() },
  async ({ faction_a, faction_b }) => {
    const connections = getFactionConnections(faction_a, faction_b);
    return ok(connections);
  }
);

server.tool(
  'get_world_state_at',
  'Simulate world state after a set of completed quests. Pass completed_quests as JSON string of array of {quest_id, branch_id}.',
  { completed_quests: z.string() },
  async ({ completed_quests }) => {
    try {
      const parsed = JSON.parse(completed_quests);
      if (!Array.isArray(parsed)) return err('completed_quests must be a JSON array');
      const state = simulateWorldState(parsed);
      return ok(state);
    } catch (e) {
      return err(`Invalid JSON for completed_quests: ${(e as Error).message}`);
    }
  }
);

server.tool(
  'get_character_arc',
  'Get a character\'s arc across all quests',
  { character_id: z.string() },
  async ({ character_id }) => {
    const arc = getCharacterArc(character_id);
    return ok(arc);
  }
);

server.tool(
  'get_faction_relationships',
  'Get faction relationship matrix with defined relationships and computed cross-faction links',
  {},
  async () => {
    const matrix = getFactionRelationshipMatrix();
    return ok(matrix);
  }
);

// ─── Draft Tools ─────────────────────────────────────────────────────────────

server.tool(
  'create_draft',
  'Create a new draft. Data should be a JSON string.',
  { type: z.string(), id: z.string(), data: z.string() },
  async ({ type, id, data }) => {
    try {
      const parsed = JSON.parse(data);
      const draft = createDraft(type, id, parsed);
      return ok(draft);
    } catch (e) {
      return err(`Invalid JSON for data: ${(e as Error).message}`);
    }
  }
);

server.tool(
  'list_drafts',
  'List drafts, optionally filtered by type',
  { type: z.string().optional() },
  async ({ type }) => {
    const drafts = listDrafts(type);
    return ok(drafts);
  }
);

server.tool(
  'get_draft',
  'Get a draft by type and ID',
  { type: z.string(), id: z.string() },
  async ({ type, id }) => {
    const draft = getDraft(type, id);
    if (!draft) return err(`Draft "${type}/${id}" not found`);
    return ok(draft);
  }
);

server.tool(
  'update_draft',
  'Update an existing draft. Data should be a JSON string.',
  { type: z.string(), id: z.string(), data: z.string() },
  async ({ type, id, data }) => {
    try {
      const parsed = JSON.parse(data);
      const draft = updateDraft(type, id, parsed);
      if (!draft) return err(`Draft "${type}/${id}" not found`);
      return ok(draft);
    } catch (e) {
      return err(`Invalid JSON for data: ${(e as Error).message}`);
    }
  }
);

server.tool(
  'delete_draft',
  'Delete a draft',
  { type: z.string(), id: z.string() },
  async ({ type, id }) => {
    const success = deleteDraft(type, id);
    if (!success) return err(`Draft "${type}/${id}" not found`);
    return ok({ deleted: true, type, id });
  }
);

// ─── Lore Tools ──────────────────────────────────────────────────────────────

server.tool(
  'create_lore',
  'Create a new lore entry',
  {
    id: z.string(),
    title: z.string(),
    body: z.string(),
    tags: z.string().optional(),
    related_entities: z.string().optional(),
    secret: z.boolean().optional()
  },
  async ({ id, title, body, tags, related_entities, secret }) => {
    const entry = {
      id,
      title,
      body,
      tags: tags ? tags.split(',').map(t => t.trim()) : [],
      related_entities: related_entities ? related_entities.split(',').map(e => e.trim()) : [],
      secret: secret || false,
      created_at: new Date().toISOString(),
      updated_at: new Date().toISOString()
    };
    writeFile('lore', id, entry);
    return ok(entry);
  }
);

server.tool(
  'update_lore',
  'Update a lore entry. Data should be a JSON string of fields to update.',
  { id: z.string(), data: z.string() },
  async ({ id, data }) => {
    const existing = readFile('lore', id) as any;
    if (!existing) return err(`Lore entry "${id}" not found`);
    try {
      const updates = JSON.parse(data);
      const updated = { ...existing, ...updates, updated_at: new Date().toISOString() };
      writeFile('lore', id, updated);
      return ok(updated);
    } catch (e) {
      return err(`Invalid JSON for data: ${(e as Error).message}`);
    }
  }
);

server.tool(
  'delete_lore',
  'Delete a lore entry',
  { id: z.string() },
  async ({ id }) => {
    const success = deleteFile('lore', id);
    if (!success) return err(`Lore entry "${id}" not found`);
    return ok({ deleted: true, id });
  }
);

server.tool(
  'search_lore',
  'Search lore entries by query (case-insensitive in title+body) and/or tags (comma-separated, match any)',
  { query: z.string().optional(), tags: z.string().optional() },
  async ({ query, tags }) => {
    const all = readAll('lore') as Record<string, any>;
    let entries = Object.values(all);

    if (query) {
      const q = query.toLowerCase();
      entries = entries.filter(e =>
        ((e.title || '') as string).toLowerCase().includes(q) ||
        ((e.body || '') as string).toLowerCase().includes(q)
      );
    }

    if (tags) {
      const tagList = tags.split(',').map(t => t.trim().toLowerCase());
      entries = entries.filter(e =>
        (e.tags || []).some((t: string) => tagList.includes(t.toLowerCase()))
      );
    }

    return ok(entries);
  }
);

server.tool(
  'get_random_lore',
  'Get random lore entries',
  { count: z.number() },
  async ({ count }) => {
    const all = readAll('lore') as Record<string, any>;
    const entries = Object.values(all);
    // Fisher-Yates shuffle
    for (let i = entries.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [entries[i], entries[j]] = [entries[j], entries[i]];
    }
    return ok(entries.slice(0, count));
  }
);

server.tool(
  'get_lore',
  'Get a lore entry by ID',
  { id: z.string() },
  async ({ id }) => {
    const entry = readFile('lore', id);
    if (!entry) return err(`Lore entry "${id}" not found`);
    return ok(entry);
  }
);

// ─── Style Tools ─────────────────────────────────────────────────────────────

server.tool(
  'list_writing_styles',
  'List all writing styles with full data',
  {},
  async () => {
    const all = readAll('writing_styles');
    return ok(Object.values(all));
  }
);

server.tool(
  'get_writing_style',
  'Get a writing style by ID',
  { id: z.string() },
  async ({ id }) => {
    const style = readFile('writing_styles', id);
    if (!style) return err(`Writing style "${id}" not found`);
    return ok(style);
  }
);

// ─── Prompt Templates ────────────────────────────────────────────────────────

server.prompt(
  'analyze_connections',
  'Analyze the connectedness and narrative flow of a faction quest line',
  { faction_id: z.string() },
  async ({ faction_id }) => {
    return promptTemplates.analyzeConnections(faction_id);
  }
);

server.prompt(
  'brainstorm_quests',
  'Brainstorm new quest ideas for a faction at a given level',
  { faction_id: z.string(), level: z.string() },
  async ({ faction_id, level }) => {
    return promptTemplates.brainstormQuests(faction_id, level);
  }
);

server.prompt(
  'check_continuity',
  'Check for continuity issues between two factions',
  { faction_a: z.string(), faction_b: z.string() },
  async ({ faction_a, faction_b }) => {
    return promptTemplates.checkContinuity(faction_a, faction_b);
  }
);

server.prompt(
  'write_dialog',
  'Write dialog for a character in the context of a quest',
  { character_id: z.string(), quest_id: z.string() },
  async ({ character_id, quest_id }) => {
    return promptTemplates.writeDialog(character_id, quest_id);
  }
);

// ─── Start Server ────────────────────────────────────────────────────────────

const transport = new StdioServerTransport();
await server.connect(transport);
