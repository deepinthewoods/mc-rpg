import { Router } from 'express';
import fs from 'fs';
import path from 'path';
import { DATA_DIR } from '../config.js';

const router = Router();
const FILE_PATH = path.join(DATA_DIR, 'faction_relationships.json');

router.get('/', (_, res) => {
  if (!fs.existsSync(FILE_PATH)) {
    return res.json({ relationships: [] });
  }
  const content = fs.readFileSync(FILE_PATH, 'utf-8');
  res.json(JSON.parse(content));
});

router.put('/', (req, res) => {
  fs.writeFileSync(FILE_PATH, JSON.stringify(req.body, null, 2), 'utf-8');
  res.json({ success: true });
});

export default router;
