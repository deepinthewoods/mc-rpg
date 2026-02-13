import React, { useState, useEffect } from 'react';
import { api } from '../api';

const DRAFT_TYPES = ['all', 'quest', 'dialog', 'character', 'location', 'faction', 'lore', 'writing_style'];

export default function DraftReviewPanel() {
  const [drafts, setDrafts] = useState<any[]>([]);
  const [filterType, setFilterType] = useState('all');
  const [selected, setSelected] = useState<any>(null);

  useEffect(() => { loadDrafts(); }, [filterType]);

  const loadDrafts = async () => {
    const type = filterType === 'all' ? undefined : filterType;
    const list = await api.listDrafts(type);
    setDrafts(list);
    setSelected(null);
  };

  const loadDraft = async (type: string, id: string) => {
    const draft = await api.getDraft(type, id);
    setSelected(draft);
  };

  const promote = async () => {
    if (!selected) return;
    if (!confirm(`Promote draft ${selected.id} to live data? This will overwrite any existing ${selected.type}/${selected.id}.`)) return;
    await api.promoteDraft(selected.type, selected.id);
    setSelected(null);
    loadDrafts();
  };

  const del = async () => {
    if (!selected) return;
    if (!confirm(`Delete draft ${selected.type}/${selected.id}?`)) return;
    await api.deleteDraft(selected.type, selected.id);
    setSelected(null);
    loadDrafts();
  };

  return (
    <div>
      <h2>Draft Review</h2>
      <div style={{ marginBottom: 15, display: 'flex', gap: 5, flexWrap: 'wrap' }}>
        {DRAFT_TYPES.map(t => (
          <button key={t} className={filterType === t ? '' : 'secondary'} onClick={() => setFilterType(t)}>
            {t.charAt(0).toUpperCase() + t.slice(1).replace('_', ' ')}
          </button>
        ))}
      </div>
      <div className="two-column">
        <div className="panel">
          {drafts.length === 0 && <div style={{ color: '#888' }}>No drafts found</div>}
          {drafts.map((d: any) => (
            <div key={`${d.type}-${d.id}`} className={`list-item ${selected?.id === d.id && selected?.type === d.type ? 'selected' : ''}`}
              onClick={() => loadDraft(d.type, d.id)}>
              <span className="badge" style={{ background: '#0f3460', marginRight: 8 }}>{d.type}</span>
              {d.id}
              <div style={{ fontSize: 11, color: '#666', marginTop: 2 }}>
                {d.updated_at ? new Date(d.updated_at).toLocaleString() : ''}
              </div>
            </div>
          ))}
        </div>
        {selected && (
          <div className="panel">
            <h3>Draft: {selected.type}/{selected.id}</h3>
            <div style={{ fontSize: 12, color: '#888', marginBottom: 10 }}>
              Status: {selected.status} | Created: {new Date(selected.created_at).toLocaleString()}
              {selected.updated_at && ` | Updated: ${new Date(selected.updated_at).toLocaleString()}`}
            </div>
            <div className="form-group">
              <label>Data Preview</label>
              <pre style={{ background: '#1a1a2e', padding: 12, borderRadius: 4, overflow: 'auto', maxHeight: 400, fontSize: 12, color: '#e0e0e0', whiteSpace: 'pre-wrap' }}>
                {JSON.stringify(selected.data, null, 2)}
              </pre>
            </div>
            <button onClick={promote}>Promote to Live</button>
            <button className="danger" onClick={del}>Delete Draft</button>
          </div>
        )}
      </div>
    </div>
  );
}
