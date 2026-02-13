import { readAll } from './fileService.js';

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

interface QuestGraph {
  nodes: GraphNode[];
  edges: GraphEdge[];
}

export function buildQuestGraph(factionId?: string): QuestGraph {
  const allQuests = readAll('quests') as Record<string, any>;
  const nodes: GraphNode[] = [];
  const edges: GraphEdge[] = [];

  for (const [qId, quest] of Object.entries(allQuests)) {
    if (factionId && quest.faction_id !== factionId) continue;
    nodes.push({
      id: quest.id || qId,
      faction_id: quest.faction_id,
      level: quest.level,
      summary: quest.summary,
      location: quest.location,
      characters: quest.characters || []
    });

    for (const branch of quest.branches || []) {
      const outcomes = branch.outcomes || {};
      for (const type of ['unlocks', 'triggers', 'blocks'] as const) {
        for (const targetId of outcomes[type] || []) {
          // Look up target quest to check cross-faction
          const targetQuest = allQuests[targetId] as any;
          edges.push({
            from: quest.id || qId,
            to: targetId,
            type: type === 'unlocks' ? 'unlock' : type === 'triggers' ? 'trigger' : 'block',
            branch_id: branch.id,
            cross_faction: targetQuest ? targetQuest.faction_id !== quest.faction_id : false
          });
        }
      }
    }
  }

  return { nodes, edges };
}

export function getFactionQuestChain(factionId: string) {
  const graph = buildQuestGraph(factionId);
  // Sort by level
  const sorted = [...graph.nodes].sort((a, b) => a.level - b.level);
  return {
    faction_id: factionId,
    quests: sorted,
    edges: graph.edges
  };
}

export function validateQuestChain(factionId?: string) {
  // Build the full graph (unfiltered) so cross-faction edges work
  const fullGraph = buildQuestGraph();
  const filteredGraph = buildQuestGraph(factionId);
  const issues: { type: string; message: string; quest_id?: string }[] = [];

  const nodeIds = new Set(filteredGraph.nodes.map(n => n.id));
  const allNodeIds = new Set(fullGraph.nodes.map(n => n.id));

  // Check for edges pointing to non-existent quests
  for (const edge of fullGraph.edges) {
    if (nodeIds.has(edge.from) && !allNodeIds.has(edge.to)) {
      issues.push({ type: 'error', message: `Quest "${edge.from}" branch "${edge.branch_id}" ${edge.type}s non-existent quest "${edge.to}"`, quest_id: edge.from });
    }
  }

  // Check for dead ends (quests with no outgoing edges and no other quest references them)
  const hasOutgoing = new Set(filteredGraph.edges.map(e => e.from));
  const hasIncoming = new Set(filteredGraph.edges.map(e => e.to));
  for (const node of filteredGraph.nodes) {
    if (!hasOutgoing.has(node.id) && !hasIncoming.has(node.id) && filteredGraph.nodes.length > 1) {
      issues.push({ type: 'warning', message: `Quest "${node.id}" is isolated (no connections)`, quest_id: node.id });
    }
  }

  // Check for level gaps
  const levels = [...new Set(filteredGraph.nodes.map(n => n.level))].sort((a, b) => a - b);
  for (let i = 1; i < levels.length; i++) {
    if (levels[i] - levels[i - 1] > 1) {
      issues.push({ type: 'warning', message: `Level gap between ${levels[i - 1]} and ${levels[i]}` });
    }
  }

  return { valid: issues.filter(i => i.type === 'error').length === 0, issues };
}
