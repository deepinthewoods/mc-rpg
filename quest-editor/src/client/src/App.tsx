import React from 'react';
import { Routes, Route, NavLink } from 'react-router-dom';
import CharacterEditor from './editors/CharacterEditor';
import LocationEditor from './editors/LocationEditor';
import FactionEditor from './editors/FactionEditor';
import QuestEditor from './editors/QuestEditor';
import DialogEditor from './editors/DialogEditor';
import ValidationPanel from './editors/ValidationPanel';

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
        <NavLink to="/validation">Validation</NavLink>
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
        </Routes>
      </main>
    </div>
  );
}
