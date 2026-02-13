import React from 'react';
import { Routes, Route, NavLink } from 'react-router-dom';
import CharacterEditor from './editors/CharacterEditor';
import LocationEditor from './editors/LocationEditor';
import FactionEditor from './editors/FactionEditor';
import QuestEditor from './editors/QuestEditor';
import DialogEditor from './editors/DialogEditor';
import ValidationPanel from './editors/ValidationPanel';
import LoreEditor from './editors/LoreEditor';
import WritingStyleEditor from './editors/WritingStyleEditor';
import DraftReviewPanel from './editors/DraftReviewPanel';
import QuestGraphView from './editors/QuestGraphView';

export default function App() {
  return (
    <div className="app">
      <nav className="sidebar">
        <h2>Quest Editor</h2>
        <NavLink to="/characters">Characters</NavLink>
        <NavLink to="/locations">Locations</NavLink>
        <NavLink to="/factions">Factions</NavLink>
        <NavLink to="/quests">Quests</NavLink>
        <NavLink to="/dialogs">Dialogs</NavLink>
        <NavLink to="/lore">Lore</NavLink>
        <NavLink to="/writing-styles">Writing Styles</NavLink>
        <NavLink to="/validation">Validation</NavLink>
        <NavLink to="/drafts">Drafts</NavLink>
        <NavLink to="/quest-graph">Quest Graph</NavLink>
      </nav>
      <main className="main">
        <Routes>
          <Route path="/" element={<h2>Welcome to the MC-RPG Quest Editor</h2>} />
          <Route path="/characters" element={<CharacterEditor />} />
          <Route path="/locations" element={<LocationEditor />} />
          <Route path="/factions" element={<FactionEditor />} />
          <Route path="/quests" element={<QuestEditor />} />
          <Route path="/dialogs" element={<DialogEditor />} />
          <Route path="/validation" element={<ValidationPanel />} />
          <Route path="/lore" element={<LoreEditor />} />
          <Route path="/writing-styles" element={<WritingStyleEditor />} />
          <Route path="/drafts" element={<DraftReviewPanel />} />
          <Route path="/quest-graph" element={<QuestGraphView />} />
        </Routes>
      </main>
    </div>
  );
}
