import { Router } from 'express';
import { listFiles, readFile, writeFile, deleteFile } from '../services/fileService.js';

const router = Router();
const SUBDIR = 'quests';

router.get('/', (_, res) => {
  res.json(listFiles(SUBDIR));
});

router.get('/:id', (req, res) => {
  const data = readFile(SUBDIR, req.params.id);
  if (!data) return res.status(404).json({ error: 'Not found' });
  res.json(data);
});

router.put('/:id', (req, res) => {
  writeFile(SUBDIR, req.params.id, req.body);
  res.json({ success: true });
});

router.delete('/:id', (req, res) => {
  const deleted = deleteFile(SUBDIR, req.params.id);
  res.json({ success: deleted });
});

export default router;
