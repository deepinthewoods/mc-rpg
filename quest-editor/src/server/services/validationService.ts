import { readAll } from './fileService.js';

interface ValidationResult {
  errors: string[];
  warnings: string[];
}

export function validateAll(): ValidationResult {
  const characters = readAll('characters') as Record<string, any>;
  const locations = readAll('locations') as Record<string, any>;
  const factions = readAll('factions') as Record<string, any>;
  const quests = readAll('quests') as Record<string, any>;
  const dialogs = readAll('dialogs') as Record<string, any>;

  const errors: string[] = [];
  const warnings: string[] = [];

  const characterIds = new Set(Object.values(characters).map((c: any) => c.id));
  const locationIds = new Set(Object.values(locations).map((l: any) => l.id));
  const factionIds = new Set(Object.values(factions).map((f: any) => f.id));
  const dialogIds = new Set(Object.values(dialogs).map((d: any) => d.id));

  // Validate quests
  for (const [file, quest] of Object.entries(quests) as [string, any][]) {
    const fullId = `${quest.faction_id}.${quest.id}`;

    if (!factionIds.has(quest.faction_id)) {
      errors.push(`Quest ${fullId}: references unknown faction '${quest.faction_id}'`);
    }
    if (!locationIds.has(quest.location)) {
      errors.push(`Quest ${fullId}: references unknown location '${quest.location}'`);
    }
    for (const charId of quest.characters || []) {
      if (!characterIds.has(charId)) {
        errors.push(`Quest ${fullId}: references unknown character '${charId}'`);
      }
    }
    if (quest.dialog_id && !dialogIds.has(quest.dialog_id)) {
      warnings.push(`Quest ${fullId}: references unknown dialog '${quest.dialog_id}'`);
    }
    if (!quest.branches || quest.branches.length === 0) {
      warnings.push(`Quest ${fullId}: has no branches defined`);
    }
  }

  // Validate characters
  for (const [file, char] of Object.entries(characters) as [string, any][]) {
    if (!locationIds.has(char.home_location)) {
      warnings.push(`Character ${char.id}: has unknown home_location '${char.home_location}'`);
    }
  }

  // Validate factions
  for (const [file, faction] of Object.entries(factions) as [string, any][]) {
    for (const memberId of faction.members || []) {
      if (!characterIds.has(memberId)) {
        warnings.push(`Faction ${faction.id}: references unknown member '${memberId}'`);
      }
    }
  }

  return { errors, warnings };
}
