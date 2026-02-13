import { readAll, readFile, listFiles } from './services/fileService.js';
import { getFactionQuestChain } from './services/questGraphService.js';
import { getFactionConnections } from './services/factionAnalysisService.js';
import { getCharacterArc } from './services/characterArcService.js';

type PromptResult = {
  messages: [{ role: 'user'; content: { type: 'text'; text: string } }];
};

function prompt(text: string): PromptResult {
  return {
    messages: [{ role: 'user', content: { type: 'text', text } }]
  };
}

// ─── 1. Analyze Connections ──────────────────────────────────────────────────

async function analyzeConnections(factionId: string): Promise<PromptResult> {
  const faction = readFile('factions', factionId) as any;
  if (!faction) return prompt(`Error: Faction "${factionId}" not found.`);

  const chain = getFactionQuestChain(factionId);

  // Load characters from faction members list
  const memberIds = (faction.members || []) as string[];
  const characters = memberIds
    .map(id => readFile('characters', id))
    .filter(c => c !== null);

  // Collect all location IDs used by quests in the chain
  const locationIds = [...new Set(chain.quests.map(q => q.location).filter(Boolean))];
  const locations = locationIds
    .map(id => readFile('locations', id))
    .filter(l => l !== null);

  const factionName = faction.name || factionId;

  return prompt(
    `Analyze the connectedness and narrative flow of the "${factionName}" faction quest line.

## Faction
${JSON.stringify(faction, null, 2)}

## Quest Chain
${JSON.stringify(chain, null, 2)}

## Characters
${JSON.stringify(characters, null, 2)}

## Locations
${JSON.stringify(locations, null, 2)}

Please analyze:
1. How well do the quests connect to each other?
2. Are there narrative gaps or missed opportunities?
3. How effectively are the characters utilized across the quest chain?
4. Are the locations used in interesting ways?
5. Suggestions for improving connectedness.`
  );
}

// ─── 2. Brainstorm Quests ────────────────────────────────────────────────────

async function brainstormQuests(factionId: string, level: string): Promise<PromptResult> {
  const faction = readFile('factions', factionId) as any;
  if (!faction) return prompt(`Error: Faction "${factionId}" not found.`);

  // Get existing quests for this faction
  const allQuests = readAll('quests') as Record<string, any>;
  const factionQuests = Object.values(allQuests).filter(q => q.faction_id === factionId);

  // Get characters from faction members
  const memberIds = (faction.members || []) as string[];
  const characters = memberIds
    .map(id => readFile('characters', id))
    .filter(c => c !== null);

  // Get 3 random lore entries for inspiration
  const allLore = readAll('lore') as Record<string, any>;
  const loreEntries = Object.values(allLore);
  for (let i = loreEntries.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [loreEntries[i], loreEntries[j]] = [loreEntries[j], loreEntries[i]];
  }
  const randomLore = loreEntries.slice(0, 3);

  const factionName = faction.name || factionId;

  return prompt(
    `Brainstorm new quest ideas for the "${factionName}" faction at level ${level}.

## Faction
${JSON.stringify(faction, null, 2)}

## Existing Quests for This Faction
${JSON.stringify(factionQuests, null, 2)}

## Available Characters
${JSON.stringify(characters, null, 2)}

## Lore for Inspiration
${JSON.stringify(randomLore, null, 2)}

Please brainstorm 3-5 new quest ideas that:
1. Fit naturally at level ${level} in this faction's progression.
2. Build on or reference existing quests where appropriate.
3. Make use of available characters (or suggest new ones if needed).
4. Draw on the provided lore for thematic consistency.
5. Include branching outcomes that affect the world state.
6. For each quest idea, provide: title, summary, suggested branches with outcomes, characters involved, and location.`
  );
}

// ─── 3. Check Continuity ────────────────────────────────────────────────────

async function checkContinuity(factionA: string, factionB: string): Promise<PromptResult> {
  const factionDataA = readFile('factions', factionA) as any;
  const factionDataB = readFile('factions', factionB) as any;

  if (!factionDataA) return prompt(`Error: Faction "${factionA}" not found.`);
  if (!factionDataB) return prompt(`Error: Faction "${factionB}" not found.`);

  const chainA = getFactionQuestChain(factionA);
  const chainB = getFactionQuestChain(factionB);
  const connections = getFactionConnections(factionA, factionB);

  const nameA = factionDataA.name || factionA;
  const nameB = factionDataB.name || factionB;

  return prompt(
    `Check for continuity issues between the "${nameA}" and "${nameB}" factions.

## Faction A: ${nameA}
${JSON.stringify(factionDataA, null, 2)}

## Faction A Quest Chain
${JSON.stringify(chainA, null, 2)}

## Faction B: ${nameB}
${JSON.stringify(factionDataB, null, 2)}

## Faction B Quest Chain
${JSON.stringify(chainB, null, 2)}

## Cross-Faction Connections
${JSON.stringify(connections, null, 2)}

Please check for:
1. Contradictions in shared character behavior or motivations across the two faction lines.
2. Shared locations that change state in conflicting ways.
3. Global variables that are set by both factions in incompatible ways.
4. Timeline issues — do cross-faction quest references make sense given the level progression?
5. Missed opportunities for interesting cross-faction interactions.
6. Any shared characters whose dialog or behavior is inconsistent between the two quest lines.`
  );
}

// ─── 4. Write Dialog ─────────────────────────────────────────────────────────

async function writeDialog(characterId: string, questId: string): Promise<PromptResult> {
  const character = readFile('characters', characterId) as any;
  if (!character) return prompt(`Error: Character "${characterId}" not found.`);

  const quest = readFile('quests', questId) as any;
  if (!quest) return prompt(`Error: Quest "${questId}" not found.`);

  // Get character arc for extra context
  const arc = getCharacterArc(characterId);

  // Find applicable writing styles
  const allStyles = readAll('writing_styles') as Record<string, any>;
  const applicableStyles = Object.values(allStyles).filter((style: any) => {
    const applicableTo = style.applicable_to || [];
    // Match by character speech formality or faction_id
    return applicableTo.includes(character.speech?.formality) ||
           applicableTo.includes(quest.faction_id);
  });

  const characterName = character.name || characterId;

  return prompt(
    `Write dialog for "${characterName}" in the context of quest "${quest.id}".

## Character
${JSON.stringify(character, null, 2)}

## Character Arc (appearances across quests)
${JSON.stringify(arc, null, 2)}

## Quest
${JSON.stringify(quest, null, 2)}

${applicableStyles.length > 0 ? `## Applicable Writing Styles\n${JSON.stringify(applicableStyles, null, 2)}` : ''}

Please write dialog that:
1. Matches the character's speech patterns (formality: "${character.speech?.formality || 'unknown'}", vocabulary: "${character.speech?.vocabulary || 'unknown'}").
2. Incorporates their verbal tics: ${JSON.stringify(character.speech?.verbal_tics || [])}.
3. Avoids language they wouldn't use: ${JSON.stringify(character.speech?.avoids || [])}.
4. Reflects their personality: "${character.personality || 'unknown'}".
5. Accounts for the character's history and arc up to this point.
6. Covers all quest branches with appropriate dialog nodes and player responses.
7. Follows the dialog tree format with nodes, speaker, text, responses, conditions, and outcome fields.`
  );
}

// ─── Exports ─────────────────────────────────────────────────────────────────

export const promptTemplates = {
  analyzeConnections,
  brainstormQuests,
  checkContinuity,
  writeDialog
};
