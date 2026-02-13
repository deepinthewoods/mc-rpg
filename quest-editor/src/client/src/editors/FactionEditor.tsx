import React, { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import { api } from '../api';

export default function FactionEditor() {
  const [searchParams] = useSearchParams();
  const [ids, setIds] = useState<string[]>([]);
  const [selected, setSelected] = useState<string | null>(null);
  const [data, setData] = useState<any>(null);
  const [newId, setNewId] = useState('');

  useEffect(() => { loadList(); }, []);
  const loadList = async () => { setIds(await api.listFactions()); };

  useEffect(() => {
    const idParam = searchParams.get('id');
    if (idParam && ids.length > 0 && !selected) loadFaction(idParam);
  }, [ids]);

  const loadFaction = async (id: string) => {
    setSelected(id);
    setData(await api.getFaction(id));
  };

  const save = async () => {
    if (!selected || !data) return;
    await api.saveFaction(selected, data);
    alert('Saved!');
  };

  const create = async () => {
    if (!newId.trim()) return;
    await api.saveFaction(newId, { id: newId, name: newId, unlocked: true, stats: {}, members: [] });
    setNewId('');
    loadList();
  };

  const del = async () => {
    if (!selected || !confirm(`Delete ${selected}?`)) return;
    await api.deleteFaction(selected);
    setSelected(null); setData(null);
    loadList();
  };

  const update = (key: string, value: any) => {
    setData({ ...data, [key]: value });
  };

  return (
    <div>
      <h2>Factions</h2>
      <div className="two-column">
        <div className="panel">
          <div style={{ marginBottom: 10 }}>
            <input value={newId} onChange={e => setNewId(e.target.value)} placeholder="New faction ID" />
            <button onClick={create} style={{ marginTop: 5 }}>Create</button>
          </div>
          {ids.map(id => (
            <div key={id} className={`list-item ${selected === id ? 'selected' : ''}`} onClick={() => loadFaction(id)}>{id}</div>
          ))}
        </div>
        {data && (
          <div className="panel">
            <div className="form-group"><label>ID</label><input value={data.id} onChange={e => update('id', e.target.value)} /></div>
            <div className="form-group"><label>Name</label><input value={data.name} onChange={e => update('name', e.target.value)} /></div>
            <div className="form-group"><label>Unlocked</label>
              <select value={data.unlocked ? 'true' : 'false'} onChange={e => update('unlocked', e.target.value === 'true')}>
                <option value="true">Yes</option><option value="false">No</option>
              </select>
            </div>
            <div className="form-group"><label>Members (comma-separated)</label>
              <input value={(data.members || []).join(', ')} onChange={e => update('members', e.target.value.split(',').map((s: string) => s.trim()).filter(Boolean))} />
            </div>
            <div className="form-group"><label>Stats (JSON)</label>
              <textarea value={JSON.stringify(data.stats || {}, null, 2)} onChange={e => {
                try { update('stats', JSON.parse(e.target.value)); } catch {}
              }} />
            </div>
            <button onClick={save}>Save</button>
            <button className="danger" onClick={del}>Delete</button>
          </div>
        )}
      </div>
    </div>
  );
}
