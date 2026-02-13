import React, { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import { api } from '../api';

export default function CharacterEditor() {
  const [searchParams] = useSearchParams();
  const [ids, setIds] = useState<string[]>([]);
  const [selected, setSelected] = useState<string | null>(null);
  const [data, setData] = useState<any>(null);
  const [newId, setNewId] = useState('');

  useEffect(() => { loadList(); }, []);

  useEffect(() => {
    const idParam = searchParams.get('id');
    if (idParam && ids.length > 0 && !selected) loadCharacter(idParam);
  }, [ids]);

  const loadList = async () => {
    const list = await api.listCharacters();
    setIds(list);
  };

  const loadCharacter = async (id: string) => {
    const char = await api.getCharacter(id);
    setSelected(id);
    setData(char);
  };

  const save = async () => {
    if (!selected || !data) return;
    await api.saveCharacter(selected, data);
    alert('Saved!');
  };

  const create = async () => {
    if (!newId.trim()) return;
    const template = {
      id: newId, name: newId, appearance: { race: 'human', gender: 'male', age_range: 'adult', notable_features: [] },
      personality: '', speech: { formality: 'casual', vocabulary: 'simple', verbal_tics: [], avoids: [], sample_lines: [] },
      backstory: '', home_location: ''
    };
    await api.saveCharacter(newId, template);
    setNewId('');
    loadList();
  };

  const del = async () => {
    if (!selected) return;
    if (!confirm(`Delete character ${selected}?`)) return;
    await api.deleteCharacter(selected);
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
      <h2>Characters</h2>
      <div className="two-column">
        <div className="panel">
          <div style={{ marginBottom: 10 }}>
            <input value={newId} onChange={e => setNewId(e.target.value)} placeholder="New character ID" />
            <button onClick={create} style={{ marginTop: 5 }}>Create</button>
          </div>
          {ids.map(id => (
            <div key={id} className={`list-item ${selected === id ? 'selected' : ''}`} onClick={() => loadCharacter(id)}>
              {id}
            </div>
          ))}
        </div>
        {data && (
          <div className="panel">
            <div className="form-group"><label>ID</label><input value={data.id} onChange={e => update('id', e.target.value)} /></div>
            <div className="form-group"><label>Name</label><input value={data.name} onChange={e => update('name', e.target.value)} /></div>
            <div className="form-group"><label>Race</label><input value={data.appearance?.race || ''} onChange={e => update('appearance.race', e.target.value)} /></div>
            <div className="form-group"><label>Gender</label><input value={data.appearance?.gender || ''} onChange={e => update('appearance.gender', e.target.value)} /></div>
            <div className="form-group"><label>Age Range</label><input value={data.appearance?.age_range || ''} onChange={e => update('appearance.age_range', e.target.value)} /></div>
            <div className="form-group"><label>Personality</label><textarea value={data.personality || ''} onChange={e => update('personality', e.target.value)} /></div>
            <div className="form-group"><label>Backstory</label><textarea value={data.backstory || ''} onChange={e => update('backstory', e.target.value)} /></div>
            <div className="form-group"><label>Home Location</label><input value={data.home_location || ''} onChange={e => update('home_location', e.target.value)} /></div>
            <div className="form-group"><label>Speech Formality</label><input value={data.speech?.formality || ''} onChange={e => update('speech.formality', e.target.value)} /></div>
            <div className="form-group"><label>Speech Vocabulary</label><input value={data.speech?.vocabulary || ''} onChange={e => update('speech.vocabulary', e.target.value)} /></div>
            <button onClick={save}>Save</button>
            <button className="danger" onClick={del}>Delete</button>
          </div>
        )}
      </div>
    </div>
  );
}
