import express from 'express';
import cors from 'cors';
import { PORT, DATA_DIR } from './config.js';
import charactersRouter from './routes/characters.js';
import locationsRouter from './routes/locations.js';
import factionsRouter from './routes/factions.js';
import questsRouter from './routes/quests.js';
import dialogsRouter from './routes/dialogs.js';
import validationRouter from './routes/validation.js';
import loreRouter from './routes/lore.js';
import writingStylesRouter from './routes/writingStyles.js';
import factionRelationshipsRouter from './routes/factionRelationships.js';
import draftsRouter from './routes/drafts.js';
import analysisRouter from './routes/analysis.js';

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
app.use('/api/lore', loreRouter);
app.use('/api/writing-styles', writingStylesRouter);
app.use('/api/faction-relationships', factionRelationshipsRouter);
app.use('/api/drafts', draftsRouter);
app.use('/api/analysis', analysisRouter);

app.listen(PORT, () => {
  console.log(`Quest Editor API running on http://localhost:${PORT}`);
  console.log(`Data directory: ${DATA_DIR}`);
});
