import React, { useState, useEffect, useRef, useCallback } from 'react';
import { api } from '../api';

interface NodePosition {
  x: number;
  y: number;
}

export default function DialogEditor() {
  const [ids, setIds] = useState<string[]>([]);
  const [selected, setSelected] = useState<string | null>(null);
  const [data, setData] = useState<any>(null);
  const [newId, setNewId] = useState('');
  const [selectedNode, setSelectedNode] = useState<string | null>(null);
  const [positions, setPositions] = useState<Record<string, NodePosition>>({});
  const [dragging, setDragging] = useState<string | null>(null);
  const [dragOffset, setDragOffset] = useState({ x: 0, y: 0 });
  const canvasRef = useRef<HTMLDivElement>(null);

  useEffect(() => { loadList(); }, []);
  const loadList = async () => { setIds(await api.listDialogs()); };

  const loadDialog = async (id: string) => {
    const dialog = await api.getDialog(id);
    setSelected(id);
    setData(dialog);
    setSelectedNode(null);
    autoLayout(dialog);
  };

  const autoLayout = (dialog: any) => {
    const nodes = Object.keys(dialog.nodes || {});
    const pos: Record<string, NodePosition> = {};
    const cols: string[][] = [];
    const visited = new Set<string>();

    // BFS from start node
    const queue = [dialog.start_node];
    visited.add(dialog.start_node);
    while (queue.length > 0) {
      const level: string[] = [];
      const size = queue.length;
      for (let i = 0; i < size; i++) {
        const nodeId = queue.shift()!;
        level.push(nodeId);
        const node = dialog.nodes[nodeId];
        if (node?.responses) {
          for (const resp of node.responses) {
            if (resp.next_node && !visited.has(resp.next_node)) {
              visited.add(resp.next_node);
              queue.push(resp.next_node);
            }
          }
        }
      }
      cols.push(level);
    }

    // Add unvisited nodes
    for (const nodeId of nodes) {
      if (!visited.has(nodeId)) cols.push([nodeId]);
    }

    cols.forEach((col, colIdx) => {
      col.forEach((nodeId, rowIdx) => {
        pos[nodeId] = { x: 30 + colIdx * 250, y: 30 + rowIdx * 150 };
      });
    });

    setPositions(pos);
  };

  const save = async () => {
    if (!selected || !data) return;
    await api.saveDialog(selected, data);
    alert('Saved!');
  };

  const create = async () => {
    if (!newId.trim()) return;
    const template = {
      id: newId, start_node: 'start',
      nodes: { start: { id: 'start', speaker: '', text: 'Hello!', responses: [] } }
    };
    await api.saveDialog(newId, template);
    setNewId('');
    loadList();
  };

  const del = async () => {
    if (!selected || !confirm(`Delete ${selected}?`)) return;
    await api.deleteDialog(selected);
    setSelected(null); setData(null);
    loadList();
  };

  const handleMouseDown = (e: React.MouseEvent, nodeId: string) => {
    e.stopPropagation();
    const pos = positions[nodeId];
    setDragging(nodeId);
    setDragOffset({ x: e.clientX - pos.x, y: e.clientY - pos.y });
  };

  const handleMouseMove = useCallback((e: React.MouseEvent) => {
    if (!dragging) return;
    setPositions(prev => ({
      ...prev,
      [dragging]: {
        x: e.clientX - dragOffset.x,
        y: e.clientY - dragOffset.y
      }
    }));
  }, [dragging, dragOffset]);

  const handleMouseUp = () => setDragging(null);

  const updateNodeField = (nodeId: string, field: string, value: any) => {
    const copy = JSON.parse(JSON.stringify(data));
    copy.nodes[nodeId][field] = value;
    setData(copy);
  };

  const nodeEntries = data ? Object.entries(data.nodes || {}) as [string, any][] : [];

  // Build connection lines
  const lines: { from: string; to: string }[] = [];
  if (data) {
    for (const [nodeId, node] of nodeEntries) {
      for (const resp of (node as any).responses || []) {
        if (resp.next_node && positions[resp.next_node]) {
          lines.push({ from: nodeId, to: resp.next_node });
        }
      }
    }
  }

  return (
    <div>
      <h2>Dialog Trees</h2>
      <div className="two-column">
        <div className="panel">
          <div style={{ marginBottom: 10 }}>
            <input value={newId} onChange={e => setNewId(e.target.value)} placeholder="New dialog ID" />
            <button onClick={create} style={{ marginTop: 5 }}>Create</button>
          </div>
          {ids.map(id => (
            <div key={id} className={`list-item ${selected === id ? 'selected' : ''}`} onClick={() => loadDialog(id)}>{id}</div>
          ))}
        </div>
        {data && (
          <div>
            <div className="dialog-canvas" ref={canvasRef} onMouseMove={handleMouseMove} onMouseUp={handleMouseUp} onMouseLeave={handleMouseUp}>
              <svg style={{ position: 'absolute', top: 0, left: 0, width: '100%', height: '100%', pointerEvents: 'none' }}>
                {lines.map((line, i) => {
                  const from = positions[line.from];
                  const to = positions[line.to];
                  if (!from || !to) return null;
                  return <line key={i} x1={from.x + 100} y1={from.y + 50} x2={to.x + 100} y2={to.y}
                    stroke="#0f3460" strokeWidth={2} markerEnd="url(#arrow)" />;
                })}
                <defs>
                  <marker id="arrow" markerWidth="10" markerHeight="7" refX="10" refY="3.5" orient="auto">
                    <polygon points="0 0, 10 3.5, 0 7" fill="#0f3460" />
                  </marker>
                </defs>
              </svg>
              {nodeEntries.map(([nodeId, node]) => {
                const pos = positions[nodeId] || { x: 0, y: 0 };
                return (
                  <div key={nodeId} className={`dialog-node ${selectedNode === nodeId ? 'selected' : ''}`}
                    style={{ left: pos.x, top: pos.y }}
                    onMouseDown={e => handleMouseDown(e, nodeId)}
                    onClick={() => setSelectedNode(nodeId)}>
                    <div className="node-header">{nodeId}</div>
                    <div className="node-text">{(node as any).speaker}: {(node as any).text?.substring(0, 60)}...</div>
                    <div style={{ fontSize: 10, color: '#666', marginTop: 4 }}>
                      {((node as any).responses || []).length} response(s)
                    </div>
                  </div>
                );
              })}
            </div>

            {selectedNode && data.nodes[selectedNode] && (
              <div className="panel" style={{ marginTop: 10 }}>
                <h3>Edit Node: {selectedNode}</h3>
                <div className="form-group"><label>Speaker</label>
                  <input value={data.nodes[selectedNode].speaker || ''} onChange={e => updateNodeField(selectedNode, 'speaker', e.target.value)} />
                </div>
                <div className="form-group"><label>Text</label>
                  <textarea value={data.nodes[selectedNode].text || ''} onChange={e => updateNodeField(selectedNode, 'text', e.target.value)} />
                </div>
                <h4>Responses</h4>
                {(data.nodes[selectedNode].responses || []).map((resp: any, i: number) => (
                  <div key={i} style={{ padding: 5, background: '#1a1a2e', borderRadius: 4, marginBottom: 5, fontSize: 12 }}>
                    "{resp.text}" → {resp.next_node}
                  </div>
                ))}
                <p style={{ color: '#888', fontSize: 12 }}>Edit responses in JSON mode for full control</p>
              </div>
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
