import { readFile } from './fileService.js';

interface CompletedQuest {
  quest_id: string;
  branch_id: string;
}

interface WorldState {
  faction_stats: Record<string, Record<string, number>>;
  global_vars: Record<string, any>;
  location_states: Record<string, string>;
  character_locations: Record<string, string>;
  character_extras: { character: string; text: string; quest_id: string }[];
  unlocked_quests: string[];
  blocked_quests: string[];
}

export function simulateWorldState(completedQuests: CompletedQuest[]): WorldState {
  const state: WorldState = {
    faction_stats: {},
    global_vars: {},
    location_states: {},
    character_locations: {},
    character_extras: [],
    unlocked_quests: [],
    blocked_quests: []
  };

  for (const { quest_id, branch_id } of completedQuests) {
    const quest = readFile('quests', quest_id) as any;
    if (!quest) continue;

    const branch = (quest.branches || []).find((b: any) => b.id === branch_id);
    if (!branch) continue;

    const outcomes = branch.outcomes || {};

    // Faction stats
    for (const [key, value] of Object.entries(outcomes.faction_stats || {})) {
      const [factionId, stat] = key.split('.');
      if (!state.faction_stats[factionId]) state.faction_stats[factionId] = {};
      state.faction_stats[factionId][stat] = (state.faction_stats[factionId][stat] || 0) + (value as number);
    }

    // Global vars
    Object.assign(state.global_vars, outcomes.global_vars || {});

    // Character extras
    for (const extra of outcomes.character_extras || []) {
      state.character_extras.push({ ...extra, quest_id });
    }

    // Move characters
    for (const [charId, locId] of Object.entries(outcomes.move_character || {})) {
      state.character_locations[charId] = locId as string;
    }

    // Change location states
    for (const [locId, newState] of Object.entries(outcomes.change_location_state || {})) {
      state.location_states[locId] = newState as string;
    }

    // Unlocks
    for (const id of outcomes.unlocks || []) {
      if (!state.unlocked_quests.includes(id)) state.unlocked_quests.push(id);
    }

    // Blocks
    for (const id of outcomes.blocks || []) {
      if (!state.blocked_quests.includes(id)) state.blocked_quests.push(id);
    }
  }

  return state;
}
