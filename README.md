# MC-RPG

A Minecraft Fabric mod that adds a full RPG quest system with custom NPCs, branching dialog, party management, faction reputation, and a dedicated RPG dimension.

**Minecraft 1.21.11 | Fabric | Java 21 | GeckoLib**

## Features

### Custom RPG Dimension
- Dedicated RPG world with a custom terrain generator (rolling hills, no hostile spawns)
- Teleport in and out with commands
- Separate from the Overworld for focused RPG gameplay

### NPC System
- GeckoLib-powered animated NPCs with idle, walk, run, and attack animations
- **4 races**: Human, Dwarf, Elf, Halfling - each with unique default sizing
- Customizable appearance: body, legs, arms, and head variants per race
- Support for fully custom model and texture paths
- Adjustable size scale (0.5x - 3.0x)
- Character ID linking NPCs to dialog trees and quest data
- Right-click interaction opens dialog when an NPC has a character ID assigned

### Quest System
- Data-driven quests loaded from JSON files
- **Branching outcomes** - quests can have multiple resolution paths with different requirements and rewards
- **Quest states**: Available, Active, Completed, Failed, Blocked
- **Requirement checking**: prerequisite quests, faction reputation thresholds, global variables
- **Outcome effects**: faction stat changes, global variable updates, location state changes, character movement, item rewards, quest unlocks/blocks
- **Auto-resolve**: active quests can resolve over time via configurable strategies (weighted random, predetermined, random, fail)
- Quest availability automatically re-evaluated as world state changes

### Dialog System
- Branching dialog trees with conditional responses
- **Conditions**: check completed quests, faction reputation, inventory items, global variables
- **Outcomes**: select quest branches, modify faction stats, give/take items, set global variables
- Client-side typewriter text effect with click-to-reveal
- Dialog sessions tracked per-player on the server

### Party System
- Form parties with other players
- Leader-based management with invite/join/kick/leave
- Party data persists across sessions
- Real-time party state sync to all members

### Faction & Reputation
- Factions with trackable stats (reputation, wealth, etc.)
- Factions can be locked/unlocked based on quest progress
- Reputation requirements gate quest availability

### World State Persistence
- All quest progress, party data, faction stats, character locations, and global variables saved with the world
- Survives server restarts

### Client GUI
- **Dialog Screen** - typewriter text animation, speaker name display, clickable response buttons, word wrapping
- **Quest Journal** (press `J`) - three tabs (Active, Available, Completed), left panel quest list, right panel quest details with status, level, faction, location, and auto-resolve timer info
- **Character Creation Screen** (press `G`) - NPC appearance customization

### Web Quest Editor
A standalone web application for creating and editing quest content outside of Minecraft.

- React + TypeScript + Vite frontend
- Express + TypeScript backend
- CRUD editors for characters, locations, factions, quests, and dialog trees
- Visual dialog node graph editor with drag-and-drop and auto-layout
- Cross-reference validation (checks that referenced characters, locations, factions, and dialogs exist)
- Reads/writes directly to the mod's data files

## Data Types

All game data is defined as JSON and loaded via Mojang Codecs:

| Type | Description |
|------|-------------|
| **Character** | Name, appearance (race, gender, age), personality, speech patterns, backstory, home location |
| **Location** | Named location with tags, connections, and multiple states (e.g. normal, burned, rebuilt) |
| **Faction** | Named faction with stats (reputation, wealth), member list, lock state |
| **Quest** | Faction-linked quest with level, requirements, branching outcomes, and auto-resolve config |
| **Dialog Tree** | Node graph with speaker text, conditional responses, and outcome actions |

## Commands

| Command | Description |
|---------|-------------|
| `/rpg enter` | Teleport to the RPG dimension |
| `/rpg leave` | Return to the Overworld |
| `/party info` | Show current party members |
| `/party invite <player>` | Invite a player to your party |
| `/party join` | Request to join a party |
| `/party leave` | Leave your current party |
| `/party kick <player>` | Remove a player (leader only) |
| `/party accept <player>` | Accept a join request (leader only) |
| `/npc create <race>` | Spawn an NPC of the given race |
| `/npc create <race> <name>` | Spawn a named NPC |

## Keybindings

| Key | Action |
|-----|--------|
| `J` | Open Quest Journal |
| `G` | Open Character Creation Screen |

## Example Data

The mod ships with example data in `src/main/resources/data/mc-rpg/rpg/`:

- `characters/marcus.json` - Marcus the Barman, a tavern NPC
- `locations/brightmug_tavern.json` - The Brightmug Tavern with normal/burned/rebuilt states
- `factions/tavern.json` - The Brightmug Tavern faction
- `quests/tavern_l1_rats.json` - Level 1 rat quest with kill and poison branches
- `dialogs/tavern_l1_rats.json` - Full dialog tree for the rat quest

## Building

```bash
./gradlew build
```

The built mod JAR is output to `build/libs/`.

## Quest Editor

```bash
cd quest-editor
npm install
npm run dev
```

This starts the Express API server and Vite dev server concurrently. The editor reads and writes quest data files directly in `src/main/resources/data/mc-rpg/rpg/`.

## Project Structure

```
src/main/java/ninja/trek/rpg/
  data/           # Data records (Character, Location, Faction, Quest, Dialog) with Codecs
  data/loader/    # RpgDataRegistry + RpgDataLoader (resource reload listener)
  dialog/         # DialogManager, DialogSession, condition evaluator, outcome handler
  entity/         # NpcEntity with GeckoLib animation
  network/        # ModNetworking + all payload types
  party/          # PartyManager + PartyCommands
  quest/          # QuestManager, RequirementChecker, OutcomeApplicator, AutoResolveHandler
  state/          # RpgWorldState (SavedData), PartyData, QuestState, CharacterExtra
  world/          # Custom dimension, chunk generator, DimensionHelper, DimensionCommands

src/client/java/ninja/trek/rpg/
  client/gui/     # DialogScreen, QuestJournalScreen, CharacterCreationScreen
  client/state/   # ClientRpgState (client-side cache)

quest-editor/     # Web-based quest editor (Express + React + TypeScript)
```
