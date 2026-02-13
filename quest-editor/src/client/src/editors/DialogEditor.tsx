import React, { useState, useEffect, useRef, useCallback } from 'react';
import { useSearchParams } from 'react-router-dom';
import { api } from '../api';

interface NodePosition {
  x: number;
  y: number;
}

const NODE_W = 220;
const LAYOUT_NODE_H = 130;
const H_GAP = 80;
const V_GAP = 50;
const EDGE_ANCHOR_Y = 25;

function storageKey(dialogId: string) {
  return `dialog-positions-${dialogId}`;
}

function savePositions(dialogId: string, positions: Record<string, NodePosition>) {
  try {
    localStorage.setItem(storageKey(dialogId), JSON.stringify(positions));
  } catch {}
}

function loadPositions(dialogId: string): Record<string, NodePosition> | null {
  try {
    const raw = localStorage.getItem(storageKey(dialogId));
    if (raw) return JSON.parse(raw);
  } catch {}
  return null;
}

function computeLayout(dialog: any): Record<string, NodePosition> {
  const nodes = Object.keys(dialog.nodes || {});
  if (nodes.length === 0) return {};

  const children: Record<string, string[]> = {};
  for (const nodeId of nodes) {
    children[nodeId] = [];
    const node = dialog.nodes[nodeId];
    if (node?.responses) {
      for (const resp of node.responses) {
        if (resp.next_node && dialog.nodes[resp.next_node]) {
          children[nodeId].push(resp.next_node);
        }
      }
    }
  }

  const layers: string[][] = [];
  const nodeLayer: Record<string, number> = {};
  const visited = new Set<string>();
  const queue = [dialog.start_node];
  visited.add(dialog.start_node);

  while (queue.length > 0) {
    const layer: string[] = [];
    const size = queue.length;
    for (let i = 0; i < size; i++) {
      const nodeId = queue.shift()!;
      layer.push(nodeId);
      nodeLayer[nodeId] = layers.length;
      for (const child of children[nodeId] || []) {
        if (!visited.has(child)) {
          visited.add(child);
          queue.push(child);
        }
      }
    }
    layers.push(layer);
  }

  const unvisited = nodes.filter(n => !visited.has(n));
  if (unvisited.length > 0) {
    for (const n of unvisited) nodeLayer[n] = layers.length;
    layers.push(unvisited);
  }

  for (let pass = 0; pass < 4; pass++) {
    for (let l = 1; l < layers.length; l++) {
      const layer = layers[l];
      const scores: Record<string, number> = {};
      for (const nodeId of layer) {
        let sum = 0, count = 0;
        for (let pl = 0; pl < l; pl++) {
          for (let pi = 0; pi < layers[pl].length; pi++) {
            if ((children[layers[pl][pi]] || []).includes(nodeId)) { sum += pi; count++; }
          }
        }
        scores[nodeId] = count > 0 ? sum / count : layers[l].indexOf(nodeId);
      }
      layers[l] = [...layer].sort((a, b) => scores[a] - scores[b]);
    }
    for (let l = layers.length - 2; l >= 0; l--) {
      const layer = layers[l];
      const scores: Record<string, number> = {};
      for (const nodeId of layer) {
        let sum = 0, count = 0;
        for (const child of children[nodeId] || []) {
          const cl = nodeLayer[child];
          if (cl !== undefined && cl > l) {
            const ci = layers[cl].indexOf(child);
            if (ci >= 0) { sum += ci; count++; }
          }
        }
        scores[nodeId] = count > 0 ? sum / count : layers[l].indexOf(nodeId);
      }
      layers[l] = [...layer].sort((a, b) => scores[a] - scores[b]);
    }
  }

  const pos: Record<string, NodePosition> = {};
  const maxLayerSize = Math.max(...layers.map(l => l.length));

  for (let col = 0; col < layers.length; col++) {
    const layer = layers[col];
    const totalHeight = layer.length * LAYOUT_NODE_H + (layer.length - 1) * V_GAP;
    const maxTotalHeight = maxLayerSize * LAYOUT_NODE_H + (maxLayerSize - 1) * V_GAP;
    const startY = 30 + (maxTotalHeight - totalHeight) / 2;
    for (let row = 0; row < layer.length; row++) {
      pos[layer[row]] = {
        x: 30 + col * (NODE_W + H_GAP),
        y: startY + row * (LAYOUT_NODE_H + V_GAP)
      };
    }
  }
  return pos;
}

export default function DialogEditor() {
  const [searchParams] = useSearchParams();
  const [ids, setIds] = useState<string[]>([]);
  const [selected, setSelected] = useState<string | null>(null);
  const [data, setData] = useState<any>(null);
  const [newId, setNewId] = useState('');

  // Multi-selection + editing
  const [selectedNodes, setSelectedNodes] = useState<Set<string>>(new Set());
  const [editingNode, setEditingNode] = useState<string | null>(null);

  // Positions
  const [positions, setPositions] = useState<Record<string, NodePosition>>({});

  // Drag state (refs to avoid stale closures)
  const [dragging, setDragging] = useState<string | null>(null);
  const dragStartRef = useRef<{ x: number; y: number } | null>(null);
  const dragInitPosRef = useRef<Record<string, NodePosition>>({});
  const didDragRef = useRef(false);
  const selectedAtDragStart = useRef<Set<string>>(new Set());

  // Marquee selection
  const [selRect, setSelRect] = useState<{ x1: number; y1: number; x2: number; y2: number } | null>(null);
  const selectingRef = useRef(false);

  // Refs
  const canvasRef = useRef<HTMLDivElement>(null);
  const nodeRefs = useRef<Record<string, HTMLDivElement | null>>({});

  useEffect(() => { loadList(); }, []);
  const loadList = async () => { setIds(await api.listDialogs()); };

  useEffect(() => {
    const idParam = searchParams.get('id');
    if (idParam && ids.length > 0 && !selected) loadDialog(idParam);
  }, [ids]);

  const loadDialog = async (id: string) => {
    const dialog = await api.getDialog(id);
    setSelected(id);
    setData(dialog);
    setEditingNode(null);
    setSelectedNodes(new Set());

    const saved = loadPositions(id);
    if (saved && Object.keys(saved).length > 0) {
      const layout = computeLayout(dialog);
      setPositions({ ...layout, ...saved });
    } else {
      setPositions(computeLayout(dialog));
    }
  };

  const handleAutoArrange = () => {
    if (!data || !selected) return;
    const layout = computeLayout(data);
    setPositions(layout);
    savePositions(selected, layout);
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
    localStorage.removeItem(storageKey(selected));
    setSelected(null); setData(null);
    loadList();
  };

  const getCanvasPos = (e: React.MouseEvent): NodePosition => {
    const canvas = canvasRef.current!;
    const rect = canvas.getBoundingClientRect();
    return { x: e.clientX - rect.left + canvas.scrollLeft, y: e.clientY - rect.top + canvas.scrollTop };
  };

  const handleNodeMouseDown = (e: React.MouseEvent, nodeId: string) => {
    e.stopPropagation();
    didDragRef.current = false;

    let newSel: Set<string>;
    if (e.ctrlKey || e.metaKey) {
      newSel = new Set(selectedNodes);
      if (newSel.has(nodeId)) newSel.delete(nodeId); else newSel.add(nodeId);
    } else if (selectedNodes.has(nodeId)) {
      newSel = selectedNodes;
    } else {
      newSel = new Set([nodeId]);
    }
    setSelectedNodes(newSel);
    selectedAtDragStart.current = newSel;
    setEditingNode(nodeId);

    // Prepare group drag
    setDragging(nodeId);
    dragStartRef.current = { x: e.clientX, y: e.clientY };
    dragInitPosRef.current = {};
    for (const nid of newSel) {
      if (positions[nid]) dragInitPosRef.current[nid] = { ...positions[nid] };
    }
  };

  const handleCanvasMouseDown = (e: React.MouseEvent) => {
    if (!(e.ctrlKey || e.metaKey)) {
      setSelectedNodes(new Set());
    }
    didDragRef.current = false;

    const pos = getCanvasPos(e);
    selectingRef.current = true;
    setSelRect({ x1: pos.x, y1: pos.y, x2: pos.x, y2: pos.y });
  };

  const handleMouseMove = useCallback((e: React.MouseEvent) => {
    if (dragging && dragStartRef.current) {
      const dx = e.clientX - dragStartRef.current.x;
      const dy = e.clientY - dragStartRef.current.y;
      if (!didDragRef.current && (Math.abs(dx) > 3 || Math.abs(dy) > 3)) {
        didDragRef.current = true;
      }
      if (didDragRef.current) {
        setPositions(prev => {
          const next = { ...prev };
          for (const [nid, initPos] of Object.entries(dragInitPosRef.current)) {
            next[nid] = { x: initPos.x + dx, y: initPos.y + dy };
          }
          return next;
        });
      }
    } else if (selectingRef.current && canvasRef.current) {
      const pos = getCanvasPos(e);
      setSelRect(prev => prev ? { ...prev, x2: pos.x, y2: pos.y } : null);
    }
  }, [dragging]);

  const handleMouseUp = useCallback(() => {
    if (dragging) {
      if (didDragRef.current && selected) {
        setPositions(prev => { savePositions(selected, prev); return prev; });
      }
      setDragging(null);
      dragStartRef.current = null;
    }

    if (selectingRef.current && selRect && canvasRef.current) {
      const canvas = canvasRef.current;
      const canvasRect = canvas.getBoundingClientRect();
      const left = Math.min(selRect.x1, selRect.x2);
      const top = Math.min(selRect.y1, selRect.y2);
      const right = Math.max(selRect.x1, selRect.x2);
      const bottom = Math.max(selRect.y1, selRect.y2);

      // Only select if the rect is big enough (avoid accidental clicks)
      if (right - left > 5 || bottom - top > 5) {
        const newSel = new Set<string>();
        for (const nodeId of Object.keys(positions)) {
          const el = nodeRefs.current[nodeId];
          if (!el) continue;
          const nr = el.getBoundingClientRect();
          const nx = nr.left - canvasRect.left + canvas.scrollLeft;
          const ny = nr.top - canvasRect.top + canvas.scrollTop;
          if (nx < right && nx + nr.width > left && ny < bottom && ny + nr.height > top) {
            newSel.add(nodeId);
          }
        }
        if (newSel.size > 0) setSelectedNodes(newSel);
      }
      selectingRef.current = false;
      setSelRect(null);
    }
  }, [dragging, selected, selRect, positions]);

  const updateNodeField = (nodeId: string, field: string, value: any) => {
    const copy = JSON.parse(JSON.stringify(data));
    copy.nodes[nodeId][field] = value;
    setData(copy);
  };

  const nodeEntries = data ? Object.entries(data.nodes || {}) as [string, any][] : [];

  // Build connection lines
  const lines: { from: string; to: string; respIdx: number; respTotal: number }[] = [];
  if (data) {
    for (const [nodeId, node] of nodeEntries) {
      const responses = (node as any).responses || [];
      responses.forEach((resp: any, idx: number) => {
        if (resp.next_node && positions[resp.next_node]) {
          lines.push({ from: nodeId, to: resp.next_node, respIdx: idx, respTotal: responses.length });
        }
      });
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
            <div style={{ marginBottom: 8 }}>
              <button className="secondary" onClick={handleAutoArrange}>Auto Arrange</button>
              {selectedNodes.size > 1 && (
                <span style={{ marginLeft: 10, color: '#aaa', fontSize: 12 }}>{selectedNodes.size} nodes selected</span>
              )}
            </div>
            <div
              className="dialog-canvas"
              ref={canvasRef}
              onMouseDown={handleCanvasMouseDown}
              onMouseMove={handleMouseMove}
              onMouseUp={handleMouseUp}
              onMouseLeave={handleMouseUp}
              style={{ userSelect: (dragging || selectingRef.current) ? 'none' : undefined }}
            >
              <svg style={{ position: 'absolute', top: 0, left: 0, width: '100%', height: '100%', pointerEvents: 'none' }}>
                <defs>
                  <marker id="arrow" markerWidth="10" markerHeight="7" refX="10" refY="3.5" orient="auto">
                    <polygon points="0 0, 10 3.5, 0 7" fill="#0f3460" />
                  </marker>
                </defs>
                {lines.map((line, i) => {
                  const from = positions[line.from];
                  const to = positions[line.to];
                  if (!from || !to) return null;
                  const spreadY = line.respTotal > 1
                    ? (line.respIdx / (line.respTotal - 1) - 0.5) * 30
                    : 0;
                  const x1 = from.x + NODE_W;
                  const y1 = from.y + EDGE_ANCHOR_Y + spreadY;
                  const x2 = to.x;
                  const y2 = to.y + EDGE_ANCHOR_Y;
                  const dx = Math.abs(x2 - x1);
                  const cpOffset = Math.max(40, dx * 0.4);
                  const path = `M ${x1} ${y1} C ${x1 + cpOffset} ${y1}, ${x2 - cpOffset} ${y2}, ${x2} ${y2}`;
                  return <path key={i} d={path} stroke="#0f3460" strokeWidth={2} fill="none" markerEnd="url(#arrow)" />;
                })}
              </svg>

              {/* Selection rectangle */}
              {selRect && (
                <div className="selection-rect" style={{
                  left: Math.min(selRect.x1, selRect.x2),
                  top: Math.min(selRect.y1, selRect.y2),
                  width: Math.abs(selRect.x2 - selRect.x1),
                  height: Math.abs(selRect.y2 - selRect.y1),
                }} />
              )}

              {nodeEntries.map(([nodeId, node]) => {
                const pos = positions[nodeId] || { x: 0, y: 0 };
                const isGroupSelected = selectedNodes.has(nodeId);
                const isEditing = editingNode === nodeId;
                return (
                  <div
                    key={nodeId}
                    ref={el => { nodeRefs.current[nodeId] = el; }}
                    className={`dialog-node${isEditing ? ' selected' : ''}${isGroupSelected ? ' group-selected' : ''}`}
                    style={{ left: pos.x, top: pos.y, width: NODE_W }}
                    onMouseDown={e => handleNodeMouseDown(e, nodeId)}
                  >
                    <div className="node-header">{nodeId}</div>
                    <div className="node-text">
                      {(node as any).speaker && <strong>{(node as any).speaker}: </strong>}
                      {(node as any).text}
                    </div>
                    <div style={{ fontSize: 10, color: '#666', marginTop: 4 }}>
                      {((node as any).responses || []).length} response(s)
                    </div>
                  </div>
                );
              })}
            </div>

            {editingNode && data.nodes[editingNode] && (
              <div className="panel" style={{ marginTop: 10 }}>
                <h3>Edit Node: {editingNode}</h3>
                <div className="form-group"><label>Speaker</label>
                  <input value={data.nodes[editingNode].speaker || ''} onChange={e => updateNodeField(editingNode, 'speaker', e.target.value)} />
                </div>
                <div className="form-group"><label>Text</label>
                  <textarea value={data.nodes[editingNode].text || ''} onChange={e => updateNodeField(editingNode, 'text', e.target.value)} />
                </div>
                <h4>Responses</h4>
                {(data.nodes[editingNode].responses || []).map((resp: any, i: number) => (
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
