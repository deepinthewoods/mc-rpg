import React, { useState, useEffect, useCallback } from 'react';
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

const FACTION_COLORS: Record<string, string> = {};
const COLOR_PALETTE = ['#e94560', '#0f3460', '#28a745', '#ffc107', '#007bff', '#6f42c1', '#fd7e14', '#20c997'];

function getFactionColor(factionId: string): string {
  if (!FACTION_COLORS[factionId]) {
    FACTION_COLORS[factionId] = COLOR_PALETTE[Object.keys(FACTION_COLORS).length % COLOR_PALETTE.length];
  }
  return FACTION_COLORS[factionId];
}

export default function QuestGraphView() {
  const [graph, setGraph] = useState<{ nodes: GraphNode[]; edges: GraphEdge[] } | null>(null);
  const [factions, setFactions] = useState<string[]>([]);
  const [filterFaction, setFilterFaction] = useState<string>('');
  const [selectedNode, setSelectedNode] = useState<GraphNode | null>(null);
  const [positions, setPositions] = useState<Record<string, { x: number; y: number }>>({});
  const [dragging, setDragging] = useState<string | null>(null);
  const [dragOffset, setDragOffset] = useState({ x: 0, y: 0 });

  useEffect(() => {
    api.listFactions().then((ids: string[]) => setFactions(ids));
  }, []);

  useEffect(() => { loadGraph(); }, [filterFaction]);

  const loadGraph = async () => {
    const data = await api.getQuestGraph(filterFaction || undefined);
    setGraph(data);
    layoutNodes(data.nodes);
  };

  const layoutNodes = (nodes: GraphNode[]) => {
    const factionCols: Record<string, number> = {};
    let colIdx = 0;
    // Assign faction columns
    for (const node of nodes) {
      if (!(node.faction_id in factionCols)) {
        factionCols[node.faction_id] = colIdx++;
      }
    }

    const pos: Record<string, { x: number; y: number }> = {};
    // Group by faction and level for row positioning
    const factionLevelCount: Record<string, Record<number, number>> = {};
    for (const node of nodes) {
      const fid = node.faction_id;
      if (!factionLevelCount[fid]) factionLevelCount[fid] = {};
      if (!factionLevelCount[fid][node.level]) factionLevelCount[fid][node.level] = 0;
      const row = factionLevelCount[fid][node.level]++;
      pos[node.id] = {
        x: 30 + factionCols[node.faction_id] * 280,
        y: 30 + (node.level - 1) * 150 + row * 80
      };
    }
    setPositions(pos);
  };

  const handleMouseDown = (e: React.MouseEvent, nodeId: string) => {
    e.stopPropagation();
    const pos = positions[nodeId];
    if (!pos) return;
    setDragging(nodeId);
    setDragOffset({ x: e.clientX - pos.x, y: e.clientY - pos.y });
  };

  const handleMouseMove = useCallback((e: React.MouseEvent) => {
    if (!dragging) return;
    setPositions(prev => ({
      ...prev,
      [dragging]: { x: e.clientX - dragOffset.x, y: e.clientY - dragOffset.y }
    }));
  }, [dragging, dragOffset]);

  const handleMouseUp = () => setDragging(null);

  if (!graph) return <div><h2>Quest Graph</h2><p>Loading...</p></div>;

  const edgeStyle = (edge: GraphEdge) => {
    if (edge.type === 'unlock') return { stroke: '#28a745', strokeDasharray: 'none' };
    if (edge.type === 'trigger') return { stroke: '#007bff', strokeDasharray: '6 3' };
    return { stroke: '#dc3545', strokeDasharray: 'none' };
  };

  return (
    <div>
      <h2>Quest Graph</h2>
      <div style={{ marginBottom: 15, display: 'flex', gap: 8, alignItems: 'center' }}>
        <label style={{ color: '#aaa', fontSize: 12 }}>Filter by faction:</label>
        <select value={filterFaction} onChange={e => setFilterFaction(e.target.value)}
          style={{ padding: '6px 10px', background: '#1a1a2e', border: '1px solid #0f3460', color: '#e0e0e0', borderRadius: 4 }}>
          <option value="">All Factions</option>
          {factions.map(f => <option key={f} value={f}>{f}</option>)}
        </select>
        <div style={{ marginLeft: 20, display: 'flex', gap: 15, fontSize: 12 }}>
          <span><span style={{ color: '#28a745' }}>---</span> Unlock</span>
          <span><span style={{ color: '#007bff' }}>- - -</span> Trigger</span>
          <span><span style={{ color: '#dc3545' }}>---</span> Block</span>
        </div>
      </div>
      <div style={{ display: 'flex', gap: 20 }}>
        <div className="quest-graph-canvas" onMouseMove={handleMouseMove} onMouseUp={handleMouseUp} onMouseLeave={handleMouseUp}>
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
              return <line key={i} x1={from.x + 110} y1={from.y + 35} x2={to.x + 110} y2={to.y}
                stroke={style.stroke} strokeWidth={edge.cross_faction ? 3 : 2}
                strokeDasharray={style.strokeDasharray} markerEnd={`url(#arrow-${edge.type})`}
                opacity={edge.cross_faction ? 1 : 0.7} />;
            })}
          </svg>
          {graph.nodes.map(node => {
            const pos = positions[node.id] || { x: 0, y: 0 };
            return (
              <div key={node.id} className={`quest-graph-node ${selectedNode?.id === node.id ? 'selected' : ''}`}
                style={{ left: pos.x, top: pos.y, borderColor: getFactionColor(node.faction_id) }}
                onMouseDown={e => handleMouseDown(e, node.id)}
                onClick={() => setSelectedNode(node)}>
                <div style={{ fontSize: 11, color: getFactionColor(node.faction_id), marginBottom: 2 }}>{node.faction_id} L{node.level}</div>
                <div style={{ fontWeight: 'bold', fontSize: 13 }}>{node.id}</div>
                <div style={{ fontSize: 11, color: '#888', marginTop: 3, overflow: 'hidden', maxHeight: 32 }}>{node.summary?.substring(0, 60)}</div>
              </div>
            );
          })}
        </div>
        {selectedNode && (
          <div className="panel" style={{ minWidth: 280, maxWidth: 350 }}>
            <h3>{selectedNode.id}</h3>
            <div className="form-group"><label>Faction</label><div>{selectedNode.faction_id}</div></div>
            <div className="form-group"><label>Level</label><div>{selectedNode.level}</div></div>
            <div className="form-group"><label>Location</label><div>{selectedNode.location}</div></div>
            <div className="form-group"><label>Characters</label><div>{selectedNode.characters.join(', ') || 'None'}</div></div>
            <div className="form-group"><label>Summary</label><div style={{ fontSize: 13 }}>{selectedNode.summary}</div></div>
            <h4 style={{ marginTop: 12, color: '#e94560' }}>Connections</h4>
            {graph.edges.filter(e => e.from === selectedNode.id || e.to === selectedNode.id).map((e, i) => (
              <div key={i} style={{ fontSize: 12, padding: '3px 0' }}>
                <span style={{ color: e.type === 'unlock' ? '#28a745' : e.type === 'trigger' ? '#007bff' : '#dc3545' }}>
                  {e.type}
                </span>{' '}
                {e.from === selectedNode.id ? `-> ${e.to}` : `<- ${e.from}`}
                {e.cross_faction && <span className="badge" style={{ background: '#6f42c1', marginLeft: 5 }}>cross-faction</span>}
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
