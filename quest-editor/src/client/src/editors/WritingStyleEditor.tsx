import React, { useState, useEffect } from 'react';
import { api } from '../api';

export default function WritingStyleEditor() {
  const [ids, setIds] = useState<string[]>([]);
  const [selected, setSelected] = useState<string | null>(null);
  const [data, setData] = useState<any>(null);
  const [newId, setNewId] = useState('');

  useEffect(() => { loadList(); }, []);

  const loadList = async () => {
    const list = await api.listWritingStyles();
    setIds(list);
  };

  const loadStyle = async (id: string) => {
    const style = await api.getWritingStyle(id);
    setSelected(id);
    setData(style);
  };

  const save = async () => {
    if (!selected || !data) return;
    await api.saveWritingStyle(selected, data);
    alert('Saved!');
  };

  const create = async () => {
    if (!newId.trim()) return;
    const template = { id: newId, name: newId, description: '', prompt: '', applicable_to: [], example: '' };
    await api.saveWritingStyle(newId, template);
    setNewId('');
    loadList();
  };

  const del = async () => {
    if (!selected) return;
    if (!confirm(`Delete writing style ${selected}?`)) return;
    await api.deleteWritingStyle(selected);
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
      <h2>Writing Styles</h2>
      <div className="two-column">
        <div className="panel">
          <div style={{ marginBottom: 10 }}>
            <input value={newId} onChange={e => setNewId(e.target.value)} placeholder="New style ID" />
            <button onClick={create} style={{ marginTop: 5 }}>Create</button>
          </div>
          {ids.map(id => (
            <div key={id} className={`list-item ${selected === id ? 'selected' : ''}`} onClick={() => loadStyle(id)}>
              {id}
            </div>
          ))}
        </div>
        {data && (
          <div className="panel">
            <div className="form-group"><label>ID</label><input value={data.id} onChange={e => update('id', e.target.value)} /></div>
            <div className="form-group"><label>Name</label><input value={data.name || ''} onChange={e => update('name', e.target.value)} /></div>
            <div className="form-group"><label>Description</label>
              <textarea value={data.description || ''} onChange={e => update('description', e.target.value)} />
            </div>
            <div className="form-group"><label>Prompt</label>
              <textarea style={{ minHeight: 200 }} value={data.prompt || ''} onChange={e => update('prompt', e.target.value)} />
            </div>
            <div className="form-group"><label>Applicable To (comma-separated)</label>
              <input value={(data.applicable_to || []).join(', ')} onChange={e => update('applicable_to', e.target.value.split(',').map((t: string) => t.trim()).filter(Boolean))} />
            </div>
            <div className="form-group"><label>Example</label>
              <textarea value={data.example || ''} onChange={e => update('example', e.target.value)} />
            </div>
            <button onClick={save}>Save</button>
            <button className="danger" onClick={del}>Delete</button>
          </div>
        )}
      </div>
    </div>
  );
}
