import fs from 'fs';
import path from 'path';
import { DATA_DIR } from '../config.js';

export function ensureDir(dir: string): void {
  if (!fs.existsSync(dir)) {
    fs.mkdirSync(dir, { recursive: true });
  }
}

export function listFiles(subdir: string): string[] {
  const dir = path.join(DATA_DIR, subdir);
  ensureDir(dir);
  return fs.readdirSync(dir)
    .filter(f => f.endsWith('.json'))
    .map(f => f.replace('.json', ''));
}

export function readFile(subdir: string, id: string): unknown | null {
  const filePath = path.join(DATA_DIR, subdir, `${id}.json`);
  if (!fs.existsSync(filePath)) return null;
  const content = fs.readFileSync(filePath, 'utf-8');
  return JSON.parse(content);
}

export function writeFile(subdir: string, id: string, data: unknown): void {
  const dir = path.join(DATA_DIR, subdir);
  ensureDir(dir);
  const filePath = path.join(dir, `${id}.json`);
  fs.writeFileSync(filePath, JSON.stringify(data, null, 2), 'utf-8');
}

export function deleteFile(subdir: string, id: string): boolean {
  const filePath = path.join(DATA_DIR, subdir, `${id}.json`);
  if (fs.existsSync(filePath)) {
    fs.unlinkSync(filePath);
    return true;
  }
  return false;
}

export function readAll(subdir: string): Record<string, unknown> {
  const ids = listFiles(subdir);
  const result: Record<string, unknown> = {};
  for (const id of ids) {
    result[id] = readFile(subdir, id);
  }
  return result;
}
