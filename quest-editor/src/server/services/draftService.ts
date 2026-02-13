import fs from 'fs';
import path from 'path';
import { DRAFTS_DIR } from '../config.js';
import { writeFile as writeLiveFile } from './fileService.js';

export interface DraftMeta {
  draft_id: string;
  type: string;
  id: string;
  created_at: string;
  updated_at: string;
  status: 'draft' | 'promoted';
  data: unknown;
}

function ensureDir(dir: string): void {
  if (!fs.existsSync(dir)) {
    fs.mkdirSync(dir, { recursive: true });
  }
}

function draftPath(type: string, id: string): string {
  return path.join(DRAFTS_DIR, type, `${id}.json`);
}

function draftDir(type: string): string {
  return path.join(DRAFTS_DIR, type);
}

export function listDrafts(type?: string): DraftMeta[] {
  const results: DraftMeta[] = [];

  if (type) {
    const dir = draftDir(type);
    ensureDir(dir);
    const files = fs.readdirSync(dir).filter(f => f.endsWith('.json'));
    for (const file of files) {
      const content = fs.readFileSync(path.join(dir, file), 'utf-8');
      results.push(JSON.parse(content) as DraftMeta);
    }
  } else {
    ensureDir(DRAFTS_DIR);
    const types = fs.readdirSync(DRAFTS_DIR, { withFileTypes: true })
      .filter(d => d.isDirectory())
      .map(d => d.name);
    for (const t of types) {
      results.push(...listDrafts(t));
    }
  }

  return results;
}

export function getDraft(type: string, id: string): DraftMeta | null {
  const filePath = draftPath(type, id);
  if (!fs.existsSync(filePath)) return null;
  const content = fs.readFileSync(filePath, 'utf-8');
  return JSON.parse(content) as DraftMeta;
}

export function createDraft(type: string, id: string, data: unknown): DraftMeta {
  const now = new Date().toISOString();
  const draft: DraftMeta = {
    draft_id: `${type}/${id}`,
    type,
    id,
    created_at: now,
    updated_at: now,
    status: 'draft',
    data,
  };

  const dir = draftDir(type);
  ensureDir(dir);
  fs.writeFileSync(draftPath(type, id), JSON.stringify(draft, null, 2), 'utf-8');
  return draft;
}

export function updateDraft(type: string, id: string, data: unknown): DraftMeta | null {
  const existing = getDraft(type, id);
  if (!existing) return null;

  existing.data = data;
  existing.updated_at = new Date().toISOString();

  fs.writeFileSync(draftPath(type, id), JSON.stringify(existing, null, 2), 'utf-8');
  return existing;
}

export function deleteDraft(type: string, id: string): boolean {
  const filePath = draftPath(type, id);
  if (fs.existsSync(filePath)) {
    fs.unlinkSync(filePath);
    return true;
  }
  return false;
}

export function promoteDraft(type: string, id: string): DraftMeta | null {
  const draft = getDraft(type, id);
  if (!draft) return null;

  // Write the draft's data to the live data directory
  writeLiveFile(type, id, draft.data);

  // Delete the draft file
  deleteDraft(type, id);

  draft.status = 'promoted';
  return draft;
}
