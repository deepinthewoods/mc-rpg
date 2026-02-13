import { Router } from 'express';
import { validateAll } from '../services/validationService.js';

const router = Router();

router.get('/', (_, res) => {
  const result = validateAll();
  res.json(result);
});

export default router;
