import React, { useState, useEffect } from 'react';
import { api } from '../api';

export default function LoreEditor() {
  const [ids, setIds] = useState<string[]>([]);
  const [selected, setSelected] = useState<string | null>(null);
  const [data, setData] = useState<any>(null);
  const [newId, setNewId] = useState('');

  useEffect(() => { loadList(); }, []);

  const loadList = async () => {
    const list = await api.listLore();
    setIds(list);
  };

  const loadLore = async (id: string) => {
    const lore = await api.getLore(id);
    setSelected(id);
    setData(lore);
  };

  const save = async () => {
    if (!selected || !data) return;
    await api.saveLore(selected, data);
    alert('Saved!');
  };

  const create = async () => {
    if (!newId.trim()) return;
    const template = { id: newId, title: newId, body: '', tags: [], related_entities: [], secret: false };
    await api.saveLore(newId, template);
    setNewId('');
    loadList();
  };

  const del = async () => {
    if (!selected) return;
    if (!confirm(`Delete lore ${selected}?`)) return;
    await api.deleteLore(selected);
    setSelected(null);
    setData(null);
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

  return (
    <div>
      <h2>Lore</h2>
      <div className="two-column">
        <div className="panel">
          <div style={{ marginBottom: 10 }}>
            <input value={newId} onChange={e => setNewId(e.target.value)} placeholder="New lore ID" />
            <button onClick={create} style={{ marginTop: 5 }}>Create</button>
          </div>
          {ids.map(id => (
            <div key={id} className={`list-item ${selected === id ? 'selected' : ''}`} onClick={() => loadLore(id)}>
              {id}
            </div>
          ))}
        </div>
        {data && (
          <div className="panel">
            <div className="form-group"><label>ID</label><input value={data.id} onChange={e => update('id', e.target.value)} /></div>
            <div className="form-group"><label>Title</label><input value={data.title || ''} onChange={e => update('title', e.target.value)} /></div>
            <div className="form-group"><label>Body</label>
              <textarea style={{ minHeight: 200 }} value={data.body || ''} onChange={e => update('body', e.target.value)} />
            </div>
            <div className="form-group"><label>Tags (comma-separated)</label>
              <input value={(data.tags || []).join(', ')} onChange={e => update('tags', e.target.value.split(',').map((t: string) => t.trim()).filter(Boolean))} />
            </div>
            <div className="form-group"><label>Related Entities (comma-separated)</label>
              <input value={(data.related_entities || []).join(', ')} onChange={e => update('related_entities', e.target.value.split(',').map((t: string) => t.trim()).filter(Boolean))} />
            </div>
            <div className="form-group"><label>Secret</label>
              <select value={data.secret ? 'true' : 'false'} onChange={e => update('secret', e.target.value === 'true')}>
                <option value="false">False</option>
                <option value="true">True</option>
              </select>
            </div>
            <button onClick={save}>Save</button>
            <button className="danger" onClick={del}>Delete</button>
          </div>
        )}
      </div>
    </div>
  );
}
