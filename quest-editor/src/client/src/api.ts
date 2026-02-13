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
  validate: () => fetchJson('/validation')
};
