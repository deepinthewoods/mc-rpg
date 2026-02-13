import fs from 'fs';
import path from 'path';
import { readAll, readFile } from './fileService.js';
import { DATA_DIR } from '../config.js';

export function getFactionConnections(factionA: string, factionB: string) {
  const allQuests = readAll('quests') as Record<string, any>;
  const allChars = readAll('characters') as Record<string, any>;
  const allLocations = readAll('locations') as Record<string, any>;

  const questsA = Object.values(allQuests).filter((q: any) => q.faction_id === factionA);
  const questsB = Object.values(allQuests).filter((q: any) => q.faction_id === factionB);

  // Shared characters
  const charsA = new Set(questsA.flatMap((q: any) => q.characters || []));
  const charsB = new Set(questsB.flatMap((q: any) => q.characters || []));
  const sharedCharacters = [...charsA].filter(c => charsB.has(c));

  // Shared locations
  const locsA = new Set(questsA.map((q: any) => q.location));
  const locsB = new Set(questsB.map((q: any) => q.location));
  const sharedLocations = [...locsA].filter(l => locsB.has(l));

  // Cross-faction quest references (unlocks/triggers/blocks from A to B or B to A)
  const crossReferences: { from: string; to: string; type: string; branch_id: string }[] = [];
  const bQuestIds = new Set(questsB.map((q: any) => q.id));
  const aQuestIds = new Set(questsA.map((q: any) => q.id));

  for (const quest of questsA) {
    for (const branch of (quest as any).branches || []) {
      const outcomes = branch.outcomes || {};
      for (const type of ['unlocks', 'triggers', 'blocks']) {
        for (const targetId of outcomes[type] || []) {
          if (bQuestIds.has(targetId)) {
            crossReferences.push({ from: (quest as any).id, to: targetId, type, branch_id: branch.id });
          }
        }
      }
    }
  }
  for (const quest of questsB) {
    for (const branch of (quest as any).branches || []) {
      const outcomes = branch.outcomes || {};
      for (const type of ['unlocks', 'triggers', 'blocks']) {
        for (const targetId of outcomes[type] || []) {
          if (aQuestIds.has(targetId)) {
            crossReferences.push({ from: (quest as any).id, to: targetId, type, branch_id: branch.id });
          }
        }
      }
    }
  }

  // Shared global vars
  const varsA = new Set<string>();
  const varsB = new Set<string>();
  for (const q of questsA) for (const b of (q as any).branches || []) for (const v of Object.keys(b.outcomes?.global_vars || {})) varsA.add(v);
  for (const q of questsB) for (const b of (q as any).branches || []) for (const v of Object.keys(b.outcomes?.global_vars || {})) varsB.add(v);
  const sharedVars = [...varsA].filter(v => varsB.has(v));

  return {
    faction_a: factionA,
    faction_b: factionB,
    shared_characters: sharedCharacters,
    shared_locations: sharedLocations,
    shared_global_vars: sharedVars,
    cross_references: crossReferences
  };
}

export function getFactionRelationshipMatrix() {
  const relPath = path.join(DATA_DIR, 'faction_relationships.json');
  let relationships: any[] = [];
  if (fs.existsSync(relPath)) {
    const content = fs.readFileSync(relPath, 'utf-8');
    relationships = JSON.parse(content).relationships || [];
  }

  const allQuests = readAll('quests') as Record<string, any>;
  const factionIds = new Set<string>();
  for (const q of Object.values(allQuests)) {
    if ((q as any).faction_id) factionIds.add((q as any).faction_id);
  }

  // Compute cross-faction links
  const crossLinks: Record<string, { from: string; to: string; type: string }[]> = {};
  for (const quest of Object.values(allQuests)) {
    for (const branch of (quest as any).branches || []) {
      const outcomes = branch.outcomes || {};
      for (const type of ['unlocks', 'triggers', 'blocks']) {
        for (const targetId of outcomes[type] || []) {
          const target = allQuests[targetId] as any;
          if (target && target.faction_id !== (quest as any).faction_id) {
            const key = [(quest as any).faction_id, target.faction_id].sort().join(':');
            if (!crossLinks[key]) crossLinks[key] = [];
            crossLinks[key].push({ from: (quest as any).id, to: targetId, type });
          }
        }
      }
    }
  }

  return {
    factions: [...factionIds],
    defined_relationships: relationships,
    computed_cross_links: crossLinks
  };
}
