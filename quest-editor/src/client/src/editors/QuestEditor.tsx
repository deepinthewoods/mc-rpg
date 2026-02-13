import React, { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { api } from '../api';

export default function QuestEditor() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [ids, setIds] = useState<string[]>([]);
  const [selected, setSelected] = useState<string | null>(null);
  const [data, setData] = useState<any>(null);
  const [newId, setNewId] = useState('');
  const [rawJson, setRawJson] = useState('');
  const [jsonMode, setJsonMode] = useState(false);

  useEffect(() => { loadList(); }, []);
  const loadList = async () => { setIds(await api.listQuests()); };

  // Auto-select from URL param
  useEffect(() => {
    const idParam = searchParams.get('id');
    if (idParam && ids.length > 0 && !selected) {
      loadQuest(idParam);
    }
  }, [ids]);

  const loadQuest = async (id: string) => {
    const quest = await api.getQuest(id);
    setSelected(id);
    setData(quest);
    setRawJson(JSON.stringify(quest, null, 2));
  };

  const save = async () => {
    if (!selected) return;
    const toSave = jsonMode ? JSON.parse(rawJson) : data;
    await api.saveQuest(selected, toSave);
    alert('Saved!');
  };

  const create = async () => {
    if (!newId.trim()) return;
    const template = {
      id: newId, faction_id: '', level: 1, consequential: false,
      summary: '', dialog_id: '', location: '', characters: [],
      requirements: {}, branches: [], auto_resolve: { type: 'fail' }
    };
    await api.saveQuest(newId, template);
    setNewId('');
    loadList();
  };

  const del = async () => {
    if (!selected || !confirm(`Delete ${selected}?`)) return;
    await api.deleteQuest(selected);
    setSelected(null); setData(null);
    loadList();
  };

  const update = (key: string, value: any) => {
    const copy = { ...data, [key]: value };
    setData(copy);
    setRawJson(JSON.stringify(copy, null, 2));
  };

  const openBtn = (path: string, value: string) => value ? (
    <button className="secondary" style={{ padding: '2px 8px', fontSize: 11, flexShrink: 0 }}
      onClick={() => navigate(`/${path}?id=${encodeURIComponent(value)}`)}>Open</button>
  ) : null;

  return (
    <div>
      <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
        <h2>Quests</h2>
        {searchParams.get('from') === 'graph' && (
          <button className="secondary" onClick={() => navigate('/quest-graph')} style={{ fontSize: 12 }}>Back to Graph</button>
        )}
      </div>
      <div className="two-column">
        <div className="panel">
          <div style={{ marginBottom: 10 }}>
            <input value={newId} onChange={e => setNewId(e.target.value)} placeholder="New quest ID" />
            <button onClick={create} style={{ marginTop: 5 }}>Create</button>
          </div>
          {ids.map(id => (
            <div key={id} className={`list-item ${selected === id ? 'selected' : ''}`} onClick={() => loadQuest(id)}>{id}</div>
          ))}
        </div>
        {data && (
          <div className="panel">
            <div style={{ marginBottom: 10 }}>
              <button className={!jsonMode ? '' : 'secondary'} onClick={() => setJsonMode(false)}>Form</button>
              <button className={jsonMode ? '' : 'secondary'} onClick={() => setJsonMode(true)}>JSON</button>
            </div>
            {jsonMode ? (
              <div className="form-group">
                <textarea style={{ minHeight: 400, fontFamily: 'monospace', fontSize: 12 }}
                  value={rawJson} onChange={e => setRawJson(e.target.value)} />
              </div>
            ) : (
              <>
                <div className="form-group"><label>ID</label><input value={data.id} onChange={e => update('id', e.target.value)} /></div>
                <div className="form-group"><label>Faction ID</label>
                  <div style={{ display: 'flex', gap: 5, alignItems: 'center' }}>
                    {openBtn('factions', data.faction_id)}
                    <input style={{ flex: 1 }} value={data.faction_id || ''} onChange={e => update('faction_id', e.target.value)} />
                  </div>
                </div>
                <div className="form-group"><label>Level</label><input type="number" value={data.level || 1} onChange={e => update('level', parseInt(e.target.value) || 1)} /></div>
                <div className="form-group"><label>Summary</label><textarea value={data.summary || ''} onChange={e => update('summary', e.target.value)} /></div>
                <div className="form-group"><label>Dialog ID</label>
                  <div style={{ display: 'flex', gap: 5, alignItems: 'center' }}>
                    {openBtn('dialogs', data.dialog_id)}
                    <input style={{ flex: 1 }} value={data.dialog_id || ''} onChange={e => update('dialog_id', e.target.value)} />
                  </div>
                </div>
                <div className="form-group"><label>Location</label>
                  <div style={{ display: 'flex', gap: 5, alignItems: 'center' }}>
                    {openBtn('locations', data.location)}
                    <input style={{ flex: 1 }} value={data.location || ''} onChange={e => update('location', e.target.value)} />
                  </div>
                </div>
                <div className="form-group"><label>Characters (comma-separated)</label>
                  <input value={(data.characters || []).join(', ')} onChange={e => update('characters', e.target.value.split(',').map((s: string) => s.trim()).filter(Boolean))} />
                  {(data.characters || []).length > 0 && (
                    <div style={{ display: 'flex', gap: 4, marginTop: 4, flexWrap: 'wrap' }}>
                      {(data.characters || []).map((c: string) => (
                        <button key={c} className="secondary" style={{ padding: '2px 8px', fontSize: 11 }}
                          onClick={() => navigate(`/characters?id=${encodeURIComponent(c)}`)}>
                          {c} &rarr;
                        </button>
                      ))}
                    </div>
                  )}
                </div>
                <div className="form-group"><label>Consequential</label>
                  <div style={{ display: 'flex', gap: 8 }}>
                    <label style={{ display: 'flex', alignItems: 'center', gap: 4, cursor: 'pointer', fontSize: 14, color: '#e0e0e0', textTransform: 'none' }}>
                      <input type="radio" name="consequential" checked={!data.consequential} onChange={() => update('consequential', false)} /> No
                    </label>
                    <label style={{ display: 'flex', alignItems: 'center', gap: 4, cursor: 'pointer', fontSize: 14, color: '#e0e0e0', textTransform: 'none' }}>
                      <input type="radio" name="consequential" checked={!!data.consequential} onChange={() => update('consequential', true)} /> Yes
                    </label>
                  </div>
                </div>
                <h4>Branches ({(data.branches || []).length})</h4>
                {(data.branches || []).map((branch: any, i: number) => (
                  <div key={i} style={{ padding: 8, background: '#1a1a2e', borderRadius: 4, marginBottom: 5 }}>
                    <strong>{branch.id}</strong>: {branch.summary}
                  </div>
                ))}
                <p style={{ color: '#888', fontSize: 12 }}>Switch to JSON mode for full branch/outcome editing</p>
              </>
            )}
            <div style={{ marginTop: 10 }}>
              <button onClick={save}>Save</button>
              <button className="danger" onClick={del}>Delete</button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
