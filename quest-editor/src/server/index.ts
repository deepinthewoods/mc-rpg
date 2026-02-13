import express from 'express';
import cors from 'cors';
import { PORT, DATA_DIR } from './config.js';
import charactersRouter from './routes/characters.js';
import locationsRouter from './routes/locations.js';
import factionsRouter from './routes/factions.js';
import questsRouter from './routes/quests.js';
import dialogsRouter from './routes/dialogs.js';
import validationRouter from './routes/validation.js';

const app = express();

app.use(cors());
app.use(express.json());

// API routes
app.use('/api/characters', charactersRouter);
app.use('/api/locations', locationsRouter);
app.use('/api/factions', factionsRouter);
app.use('/api/quests', questsRouter);
app.use('/api/dialogs', dialogsRouter);
app.use('/api/validation', validationRouter);

app.listen(PORT, () => {
  console.log(`Quest Editor API running on http://localhost:${PORT}`);
  console.log(`Data directory: ${DATA_DIR}`);
});
