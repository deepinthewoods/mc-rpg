import path from 'path';
import { fileURLToPath } from 'url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));

export const DATA_DIR = path.resolve(__dirname, '../../../src/main/resources/data/mc-rpg/rpg');
export const PORT = 3001;
