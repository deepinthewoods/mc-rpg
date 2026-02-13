const BASE = '/api';

async function fetchJson(url: string): Promise<any> {
  const res = await fetch(BASE + url);
  if (!res.ok) throw new Error(`HTTP ${res.status}`);
  return res.json();
}

async function putJson(url: string, data: any): Promise<any> {
  const res = await fetch(BASE + url, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data)
  });
  return res.json();
}

async function deleteReq(url: string): Promise<any> {
  const res = await fetch(BASE + url, { method: 'DELETE' });
  return res.json();
}

async function postJson(url: string, data?: any): Promise<any> {
  const res = await fetch(BASE + url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: data ? JSON.stringify(data) : undefined
  });
  return res.json();
}

export const api = {
  // Characters
  listCharacters: () => fetchJson('/characters'),
  getCharacter: (id: string) => fetchJson(`/characters/${id}`),
  saveCharacter: (id: string, data: any) => putJson(`/characters/${id}`, data),
  deleteCharacter: (id: string) => deleteReq(`/characters/${id}`),

  // Locations
  listLocations: () => fetchJson('/locations'),
  getLocation: (id: string) => fetchJson(`/locations/${id}`),
  saveLocation: (id: string, data: any) => putJson(`/locations/${id}`, data),
  deleteLocation: (id: string) => deleteReq(`/locations/${id}`),

  // Factions
  listFactions: () => fetchJson('/factions'),
  getFaction: (id: string) => fetchJson(`/factions/${id}`),
  saveFaction: (id: string, data: any) => putJson(`/factions/${id}`, data),
  deleteFaction: (id: string) => deleteReq(`/factions/${id}`),

  // Quests
  listQuests: () => fetchJson('/quests'),
  getQuest: (id: string) => fetchJson(`/quests/${id}`),
  saveQuest: (id: string, data: any) => putJson(`/quests/${id}`, data),
  deleteQuest: (id: string) => deleteReq(`/quests/${id}`),

  // Dialogs
  listDialogs: () => fetchJson('/dialogs'),
  getDialog: (id: string) => fetchJson(`/dialogs/${id}`),
  saveDialog: (id: string, data: any) => putJson(`/dialogs/${id}`, data),
  deleteDialog: (id: string) => deleteReq(`/dialogs/${id}`),

  // Validation
  validate: () => fetchJson('/validation'),

  // Lore
  listLore: () => fetchJson('/lore'),
  getLore: (id: string) => fetchJson(`/lore/${id}`),
  saveLore: (id: string, data: any) => putJson(`/lore/${id}`, data),
  deleteLore: (id: string) => deleteReq(`/lore/${id}`),

  // Writing Styles
  listWritingStyles: () => fetchJson('/writing-styles'),
  getWritingStyle: (id: string) => fetchJson(`/writing-styles/${id}`),
  saveWritingStyle: (id: string, data: any) => putJson(`/writing-styles/${id}`, data),
  deleteWritingStyle: (id: string) => deleteReq(`/writing-styles/${id}`),

  // Drafts
  listDrafts: (type?: string) => fetchJson(`/drafts${type ? `?type=${type}` : ''}`),
  getDraft: (type: string, id: string) => fetchJson(`/drafts/${type}/${id}`),
  saveDraft: (type: string, id: string, data: any) => putJson(`/drafts/${type}/${id}`, data),
  deleteDraft: (type: string, id: string) => deleteReq(`/drafts/${type}/${id}`),
  promoteDraft: (type: string, id: string) => postJson(`/drafts/${type}/${id}/promote`),

  // Faction Relationships
  getFactionRelationships: () => fetchJson('/faction-relationships'),
  saveFactionRelationships: (data: any) => putJson('/faction-relationships', data),

  // Analysis
  getQuestGraph: (factionId?: string) => fetchJson(`/analysis/quest-graph${factionId ? `?faction_id=${factionId}` : ''}`),
  getFactionQuestChain: (factionId: string) => fetchJson(`/analysis/faction-quest-chain/${factionId}`),
  simulateWorldState: (completedQuests: any[]) => postJson('/analysis/world-state', { completed_quests: completedQuests }),
  getCharacterArc: (id: string) => fetchJson(`/analysis/character-arc/${id}`),
  getFactionConnections: (a: string, b: string) => fetchJson(`/analysis/faction-connections/${a}/${b}`),
  validateQuestChain: (factionId?: string) => fetchJson(`/analysis/validate-quest-chain${factionId ? `?faction_id=${factionId}` : ''}`)
};
