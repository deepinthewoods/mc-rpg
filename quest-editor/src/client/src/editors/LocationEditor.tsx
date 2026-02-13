import React, { useState, useEffect } from 'react';
import { api } from '../api';

export default function LocationEditor() {
  const [ids, setIds] = useState<string[]>([]);
  const [selected, setSelected] = useState<string | null>(null);
  const [data, setData] = useState<any>(null);
  const [newId, setNewId] = useState('');
  const [editState, setEditState] = useState<string | null>(null);

  useEffect(() => { loadList(); }, []);

  const loadList = async () => { setIds(await api.listLocations()); };

  const loadLocation = async (id: string) => {
    setSelected(id);
    setData(await api.getLocation(id));
    setEditState(null);
  };

  const save = async () => {
    if (!selected || !data) return;
    await api.saveLocation(selected, data);
    alert('Saved!');
  };

  const create = async () => {
    if (!newId.trim()) return;
    const template = {
      id: newId, name: newId, tags: [], default_connections: [],
      states: { normal: { description: '', atmosphere: { lighting: '', sounds: '', smells: '', mood: '' } } },
      default_state: 'normal'
    };
    await api.saveLocation(newId, template);
    setNewId('');
    loadList();
  };

  const del = async () => {
    if (!selected || !confirm(`Delete ${selected}?`)) return;
    await api.deleteLocation(selected);
    setSelected(null); setData(null);
    loadList();
  };

  const update = (path: string, value: any) => {
    const copy = JSON.parse(JSON.stringify(data));
    const keys = path.split('.');
    let obj = copy;
    for (let i = 0; i < keys.length - 1; i++) obj = obj[keys[i]];
    obj[keys[keys.length - 1]] = value;
    setData(copy);
  };

  const stateNames = data ? Object.keys(data.states || {}) : [];

  return (
    <div>
      <h2>Locations</h2>
      <div className="two-column">
        <div className="panel">
          <div style={{ marginBottom: 10 }}>
            <input value={newId} onChange={e => setNewId(e.target.value)} placeholder="New location ID" />
            <button onClick={create} style={{ marginTop: 5 }}>Create</button>
          </div>
          {ids.map(id => (
            <div key={id} className={`list-item ${selected === id ? 'selected' : ''}`} onClick={() => loadLocation(id)}>
              {id}
            </div>
          ))}
        </div>
        {data && (
          <div className="panel">
            <div className="form-group"><label>ID</label><input value={data.id} onChange={e => update('id', e.target.value)} /></div>
            <div className="form-group"><label>Name</label><input value={data.name} onChange={e => update('name', e.target.value)} /></div>
            <div className="form-group"><label>Tags (comma-separated)</label>
              <input value={(data.tags || []).join(', ')} onChange={e => update('tags', e.target.value.split(',').map((s: string) => s.trim()).filter(Boolean))} />
            </div>
            <div className="form-group"><label>Default Connections (comma-separated)</label>
              <input value={(data.default_connections || []).join(', ')} onChange={e => update('default_connections', e.target.value.split(',').map((s: string) => s.trim()).filter(Boolean))} />
            </div>
            <div className="form-group"><label>Default State</label><input value={data.default_state || ''} onChange={e => update('default_state', e.target.value)} /></div>

            <h4>States</h4>
            <div style={{ display: 'flex', gap: 5, marginBottom: 10 }}>
              {stateNames.map(s => (
                <button key={s} className={editState === s ? '' : 'secondary'} onClick={() => setEditState(s)}>{s}</button>
              ))}
            </div>

            {editState && data.states[editState] && (
              <div style={{ padding: 10, background: '#1a1a2e', borderRadius: 4 }}>
                <div className="form-group"><label>Description</label>
                  <textarea value={data.states[editState].description || ''} onChange={e => update(`states.${editState}.description`, e.target.value)} />
                </div>
                <div className="form-group"><label>Lighting</label>
                  <input value={data.states[editState].atmosphere?.lighting || ''} onChange={e => update(`states.${editState}.atmosphere.lighting`, e.target.value)} />
                </div>
                <div className="form-group"><label>Sounds</label>
                  <input value={data.states[editState].atmosphere?.sounds || ''} onChange={e => update(`states.${editState}.atmosphere.sounds`, e.target.value)} />
                </div>
                <div className="form-group"><label>Mood</label>
                  <input value={data.states[editState].atmosphere?.mood || ''} onChange={e => update(`states.${editState}.atmosphere.mood`, e.target.value)} />
                </div>
              </div>
            )}

            <div style={{ marginTop: 15 }}>
              <button onClick={save}>Save</button>
              <button className="danger" onClick={del}>Delete</button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
