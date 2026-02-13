import React, { useState, useEffect, useCallback, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../api';

interface GraphNode {
  id: string;
  faction_id: string;
  level: number;
  summary: string;
  location: string;
  characters: string[];
}

interface GraphEdge {
  from: string;
  to: string;
  type: 'unlock' | 'trigger' | 'block';
  branch_id: string;
  cross_faction: boolean;
}

interface Pos { x: number; y: number; }

const FACTION_COLORS: Record<string, string> = {};
const COLOR_PALETTE = ['#e94560', '#0f3460', '#28a745', '#ffc107', '#007bff', '#6f42c1', '#fd7e14', '#20c997'];
const NODE_W = 240;
const NODE_H = 70;
const COL_GAP = 60;
const ROW_GAP = 40;

function getFactionColor(factionId: string): string {
  if (!FACTION_COLORS[factionId]) {
    FACTION_COLORS[factionId] = COLOR_PALETTE[Object.keys(FACTION_COLORS).length % COLOR_PALETTE.length];
  }
  return FACTION_COLORS[factionId];
}

function storageKey(factionFilter: string) {
  return `quest-graph-positions-${factionFilter || '__all__'}`;
}

function savePositions(factionFilter: string, positions: Record<string, Pos>) {
  try {
    localStorage.setItem(storageKey(factionFilter), JSON.stringify(positions));
  } catch {}
}

function loadSavedPositions(factionFilter: string): Record<string, Pos> | null {
  try {
    const raw = localStorage.getItem(storageKey(factionFilter));
    if (raw) return JSON.parse(raw);
  } catch {}
  return null;
}

function computeLayout(nodes: GraphNode[], edges: GraphEdge[]): Record<string, Pos> {
  if (nodes.length === 0) return {};

  const factionNodes: Record<string, GraphNode[]> = {};
  for (const node of nodes) {
    if (!factionNodes[node.faction_id]) factionNodes[node.faction_id] = [];
    factionNodes[node.faction_id].push(node);
  }

  const outEdges: Record<string, string[]> = {};
  const inEdges: Record<string, string[]> = {};
  for (const node of nodes) {
    outEdges[node.id] = [];
    inEdges[node.id] = [];
  }
  for (const edge of edges) {
    if (outEdges[edge.from]) outEdges[edge.from].push(edge.to);
    if (inEdges[edge.to]) inEdges[edge.to].push(edge.from);
  }

  const factionIds = Object.keys(factionNodes);
  const pos: Record<string, Pos> = {};
  let colOffset = 0;

  for (const fid of factionIds) {
    const fNodes = factionNodes[fid];
    fNodes.sort((a, b) => {
      if (a.level !== b.level) return a.level - b.level;
      return (inEdges[a.id]?.length || 0) - (inEdges[b.id]?.length || 0);
    });

    const levelGroups: Record<number, GraphNode[]> = {};
    for (const node of fNodes) {
      if (!levelGroups[node.level]) levelGroups[node.level] = [];
      levelGroups[node.level].push(node);
    }

    const levels = Object.keys(levelGroups).map(Number).sort((a, b) => a - b);

    for (let pass = 0; pass < 3; pass++) {
      for (let li = 1; li < levels.length; li++) {
        const level = levels[li];
        const group = levelGroups[level];
        const prevGroup = levelGroups[levels[li - 1]];
        const scores: Record<string, number> = {};
        for (const node of group) {
          let sum = 0, count = 0;
          for (const parentId of inEdges[node.id] || []) {
            const pIdx = prevGroup.findIndex(n => n.id === parentId);
            if (pIdx >= 0) { sum += pIdx; count++; }
          }
          scores[node.id] = count > 0 ? sum / count : group.indexOf(node);
        }
        levelGroups[level] = [...group].sort((a, b) => scores[a.id] - scores[b.id]);
      }
    }

    let maxNodesInLevel = 0;
    for (const level of levels) {
      maxNodesInLevel = Math.max(maxNodesInLevel, levelGroups[level].length);
    }

    let rowY = 30;
    for (const level of levels) {
      const group = levelGroups[level];
      for (let i = 0; i < group.length; i++) {
        pos[group[i].id] = {
          x: 30 + colOffset + i * (NODE_W + COL_GAP),
          y: rowY
        };
      }
      rowY += NODE_H + ROW_GAP;
    }
    colOffset += maxNodesInLevel * (NODE_W + COL_GAP);
  }
  return pos;
}

export default function QuestGraphView() {
  const navigate = useNavigate();
  const [graph, setGraph] = useState<{ nodes: GraphNode[]; edges: GraphEdge[] } | null>(null);
  const [factions, setFactions] = useState<string[]>([]);
  const [filterFaction, setFilterFaction] = useState<string>('');

  // Multi-selection + detail panel
  const [selectedNodes, setSelectedNodes] = useState<Set<string>>(new Set());
  const [detailNode, setDetailNode] = useState<GraphNode | null>(null);

  // Positions
  const [positions, setPositions] = useState<Record<string, Pos>>({});

  // Drag state
  const [dragging, setDragging] = useState<string | null>(null);
  const dragStartRef = useRef<Pos | null>(null);
  const dragInitPosRef = useRef<Record<string, Pos>>({});
  const didDragRef = useRef(false);

  // Marquee selection
  const [selRect, setSelRect] = useState<{ x1: number; y1: number; x2: number; y2: number } | null>(null);
  const selectingRef = useRef(false);

  // Refs
  const canvasRef = useRef<HTMLDivElement>(null);
  const nodeRefs = useRef<Record<string, HTMLDivElement | null>>({});

  useEffect(() => {
    api.listFactions().then((ids: string[]) => setFactions(ids));
  }, []);

  useEffect(() => { loadGraph(); }, [filterFaction]);

  const loadGraph = async () => {
    const data = await api.getQuestGraph(filterFaction || undefined);
    setGraph(data);
    setSelectedNodes(new Set());
    setDetailNode(null);

    const saved = loadSavedPositions(filterFaction);
    if (saved && Object.keys(saved).length > 0) {
      const layout = computeLayout(data.nodes, data.edges);
      setPositions({ ...layout, ...saved });
    } else {
      setPositions(computeLayout(data.nodes, data.edges));
    }
  };

  const handleAutoArrange = () => {
    if (!graph) return;
    const layout = computeLayout(graph.nodes, graph.edges);
    setPositions(layout);
    savePositions(filterFaction, layout);
  };

  const getCanvasPos = (e: React.MouseEvent): Pos => {
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

    const node = graph?.nodes.find(n => n.id === nodeId) || null;
    setDetailNode(node);

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
      if (didDragRef.current) {
        setPositions(prev => { savePositions(filterFaction, prev); return prev; });
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
  }, [dragging, filterFaction, selRect, positions]);

  const handleNodeDoubleClick = (e: React.MouseEvent, nodeId: string) => {
    if (!didDragRef.current) {
      navigate(`/quests?id=${encodeURIComponent(nodeId)}&from=graph`);
    }
  };

  if (!graph) return <div><h2>Quest Graph</h2><p>Loading...</p></div>;

  const edgeStyle = (edge: GraphEdge) => {
    if (edge.type === 'unlock') return { stroke: '#28a745', dash: 'none' };
    if (edge.type === 'trigger') return { stroke: '#007bff', dash: '6 3' };
    return { stroke: '#dc3545', dash: 'none' };
  };

  return (
    <div>
      <h2>Quest Graph</h2>
      <div style={{ marginBottom: 15, display: 'flex', gap: 8, alignItems: 'center', flexWrap: 'wrap' }}>
        <label style={{ color: '#aaa', fontSize: 12 }}>Filter by faction:</label>
        <select value={filterFaction} onChange={e => setFilterFaction(e.target.value)}
          style={{ padding: '6px 10px', background: '#1a1a2e', border: '1px solid #0f3460', color: '#e0e0e0', borderRadius: 4 }}>
          <option value="">All Factions</option>
          {factions.map(f => <option key={f} value={f}>{f}</option>)}
        </select>
        <button className="secondary" onClick={handleAutoArrange}>Auto Arrange</button>
        {selectedNodes.size > 1 && (
          <span style={{ color: '#aaa', fontSize: 12 }}>{selectedNodes.size} nodes selected</span>
        )}
        <div style={{ marginLeft: 20, display: 'flex', gap: 15, fontSize: 12 }}>
          <span><span style={{ color: '#28a745' }}>---</span> Unlock</span>
          <span><span style={{ color: '#007bff' }}>- - -</span> Trigger</span>
          <span><span style={{ color: '#dc3545' }}>---</span> Block</span>
        </div>
      </div>
      <div style={{ display: 'flex', gap: 20 }}>
        <div
          className="quest-graph-canvas"
          ref={canvasRef}
          onMouseDown={handleCanvasMouseDown}
          onMouseMove={handleMouseMove}
          onMouseUp={handleMouseUp}
          onMouseLeave={handleMouseUp}
          style={{ userSelect: (dragging || selectingRef.current) ? 'none' : undefined }}
        >
          <svg style={{ position: 'absolute', top: 0, left: 0, width: '100%', height: '100%', pointerEvents: 'none' }}>
            <defs>
              <marker id="arrow-unlock" markerWidth="10" markerHeight="7" refX="10" refY="3.5" orient="auto">
                <polygon points="0 0, 10 3.5, 0 7" fill="#28a745" />
              </marker>
              <marker id="arrow-trigger" markerWidth="10" markerHeight="7" refX="10" refY="3.5" orient="auto">
                <polygon points="0 0, 10 3.5, 0 7" fill="#007bff" />
              </marker>
              <marker id="arrow-block" markerWidth="10" markerHeight="7" refX="10" refY="3.5" orient="auto">
                <polygon points="0 0, 10 3.5, 0 7" fill="#dc3545" />
              </marker>
            </defs>
            {graph.edges.map((edge, i) => {
              const from = positions[edge.from];
              const to = positions[edge.to];
              if (!from || !to) return null;
              const style = edgeStyle(edge);
              const x1 = from.x + NODE_W / 2;
              const y1 = from.y + NODE_H;
              const x2 = to.x + NODE_W / 2;
              const y2 = to.y;
              const dy = Math.abs(y2 - y1);
              const cpOffset = Math.max(30, dy * 0.4);
              const path = `M ${x1} ${y1} C ${x1} ${y1 + cpOffset}, ${x2} ${y2 - cpOffset}, ${x2} ${y2}`;
              return <path key={i} d={path} stroke={style.stroke} strokeWidth={edge.cross_faction ? 3 : 2}
                strokeDasharray={style.dash} fill="none" markerEnd={`url(#arrow-${edge.type})`}
                opacity={edge.cross_faction ? 1 : 0.7} />;
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

          {graph.nodes.map(node => {
            const pos = positions[node.id] || { x: 0, y: 0 };
            const isGroupSelected = selectedNodes.has(node.id);
            const isDetail = detailNode?.id === node.id;
            return (
              <div
                key={node.id}
                ref={el => { nodeRefs.current[node.id] = el; }}
                className={`quest-graph-node${isDetail ? ' selected' : ''}${isGroupSelected ? ' group-selected' : ''}`}
                style={{ left: pos.x, top: pos.y, borderColor: getFactionColor(node.faction_id) }}
                onMouseDown={e => handleNodeMouseDown(e, node.id)}
                onDoubleClick={e => handleNodeDoubleClick(e, node.id)}
              >
                <div style={{ fontSize: 11, color: getFactionColor(node.faction_id), marginBottom: 2 }}>{node.faction_id} L{node.level}</div>
                <div style={{ fontWeight: 'bold', fontSize: 13 }}>{node.id}</div>
                <div style={{ fontSize: 11, color: '#888', marginTop: 3, overflow: 'hidden', maxHeight: 32 }}>{node.summary?.substring(0, 60)}</div>
              </div>
            );
          })}
        </div>
        {detailNode && (
          <div className="panel" style={{ minWidth: 280, maxWidth: 350 }}>
            <h3>{detailNode.id}</h3>
            <div className="form-group"><label>Faction</label><div>{detailNode.faction_id}</div></div>
            <div className="form-group"><label>Level</label><div>{detailNode.level}</div></div>
            <div className="form-group"><label>Location</label><div>{detailNode.location}</div></div>
            <div className="form-group"><label>Characters</label><div>{detailNode.characters.join(', ') || 'None'}</div></div>
            <div className="form-group"><label>Summary</label><div style={{ fontSize: 13 }}>{detailNode.summary}</div></div>
            <h4 style={{ marginTop: 12, color: '#e94560' }}>Connections</h4>
            {graph.edges.filter(e => e.from === detailNode.id || e.to === detailNode.id).map((e, i) => (
              <div key={i} style={{ fontSize: 12, padding: '3px 0' }}>
                <span style={{ color: e.type === 'unlock' ? '#28a745' : e.type === 'trigger' ? '#007bff' : '#dc3545' }}>
                  {e.type}
                </span>{' '}
                {e.from === detailNode.id ? `-> ${e.to}` : `<- ${e.from}`}
                {e.cross_faction && <span className="badge" style={{ background: '#6f42c1', marginLeft: 5 }}>cross-faction</span>}
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
