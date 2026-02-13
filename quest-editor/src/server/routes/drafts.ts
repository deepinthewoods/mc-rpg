import { Router } from 'express';
import { listDrafts, getDraft, createDraft, updateDraft, deleteDraft, promoteDraft } from '../services/draftService.js';

const router = Router();

router.get('/', (req, res) => {
  const type = req.query.type as string | undefined;
  res.json(listDrafts(type));
});

router.get('/:type/:id', (req, res) => {
  const draft = getDraft(req.params.type, req.params.id);
  if (!draft) return res.status(404).json({ error: 'Not found' });
  res.json(draft);
});

router.put('/:type/:id', (req, res) => {
  const existing = getDraft(req.params.type, req.params.id);
  if (existing) {
    const updated = updateDraft(req.params.type, req.params.id, req.body);
    if (!updated) return res.status(500).json({ error: 'Failed to update draft' });
    res.json(updated);
  } else {
    const created = createDraft(req.params.type, req.params.id, req.body);
    res.json(created);
  }
});

router.delete('/:type/:id', (req, res) => {
  const deleted = deleteDraft(req.params.type, req.params.id);
  res.json({ success: deleted });
});

router.post('/:type/:id/promote', (req, res) => {
  const promoted = promoteDraft(req.params.type, req.params.id);
  if (!promoted) return res.status(404).json({ error: 'Draft not found' });
  res.json({ success: true, promoted });
});

export default router;
