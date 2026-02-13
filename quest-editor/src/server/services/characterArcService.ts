import { readAll, readFile } from './fileService.js';

interface Appearance {
  quest_id: string;
  quest_summary: string;
  faction_id: string;
  level: number;
  role: 'quest_character' | 'extra_mention';
  extras: string[];
  moves_to?: string;
}

export function getCharacterArc(characterId: string) {
  const character = readFile('characters', characterId);
  const allQuests = readAll('quests') as Record<string, any>;
  const appearances: Appearance[] = [];

  for (const [qId, quest] of Object.entries(allQuests)) {
    const isQuestChar = (quest.characters || []).includes(characterId);
    const branchExtras: string[] = [];
    let movesTo: string | undefined;

    for (const branch of quest.branches || []) {
      const outcomes = branch.outcomes || {};
      for (const extra of outcomes.character_extras || []) {
        if (extra.character === characterId) {
          branchExtras.push(`[${branch.id}] ${extra.text}`);
        }
      }
      const moveTarget = (outcomes.move_character || {})[characterId];
      if (moveTarget) movesTo = moveTarget;
    }

    if (isQuestChar || branchExtras.length > 0) {
      appearances.push({
        quest_id: quest.id || qId,
        quest_summary: quest.summary,
        faction_id: quest.faction_id,
        level: quest.level,
        role: isQuestChar ? 'quest_character' : 'extra_mention',
        extras: branchExtras,
        moves_to: movesTo
      });
    }
  }

  // Sort by level
  appearances.sort((a, b) => a.level - b.level);

  return { character_id: characterId, character, appearances };
}
