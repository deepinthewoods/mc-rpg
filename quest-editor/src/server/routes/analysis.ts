import { Router } from 'express';
import { buildQuestGraph, getFactionQuestChain, validateQuestChain } from '../services/questGraphService.js';
import { simulateWorldState } from '../services/worldStateSimulator.js';
import { getCharacterArc } from '../services/characterArcService.js';
import { getFactionConnections } from '../services/factionAnalysisService.js';

const router = Router();

router.get('/quest-graph', (req, res) => {
  const factionId = req.query.faction_id as string | undefined;
  res.json(buildQuestGraph(factionId));
});

router.get('/faction-quest-chain/:factionId', (req, res) => {
  res.json(getFactionQuestChain(req.params.factionId));
});

router.post('/world-state', (req, res) => {
  const { completed_quests } = req.body;
  res.json(simulateWorldState(completed_quests || []));
});

router.get('/character-arc/:id', (req, res) => {
  res.json(getCharacterArc(req.params.id));
});

router.get('/faction-connections/:a/:b', (req, res) => {
  res.json(getFactionConnections(req.params.a, req.params.b));
});

router.get('/validate-quest-chain', (req, res) => {
  const factionId = req.query.faction_id as string | undefined;
  res.json(validateQuestChain(factionId));
});

export default router;
