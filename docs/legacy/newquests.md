# New Quest System Architecture

This document describes the refactored quest system with separate character, location, and faction entities.

## Overview

The new architecture separates game entities into distinct JSON files:
- **Characters** - NPCs with appearance, personality, speech patterns, backstory
- **Locations** - Places with states/variants, atmosphere, connections
- **Factions** - Groups with stats, members, and associated quests
- **Quests** - Story content referencing characters and locations

This separation enables:
- Consistent character personalities across all quests (for AI quest generation)
- Location state changes (e.g., tavern burns down → swap to burned variant)
- Better content validation and tracking of missing assets
- Cleaner data management and editor tooling

---

## File Structure

```
data/
├── characters/
│   ├── marcus.json
│   ├── mayor_thornwick.json
│   ├── shadow.json
│   └── bandit_leader.json
├── locations/
│   ├── brightmug_tavern.json
│   ├── mayor_office.json
│   ├── town_square.json
│   └── thieves_hideout.json
├── factions/
│   ├── tavern.json
│   ├── mayor.json
│   └── thieves.json
└── quests/
    ├── tavern.json      # Quests for tavern faction
    ├── mayor.json       # Quests for mayor faction
    └── thieves.json     # Quests for thieves faction
```

---

## Character Data Format

Characters are NPCs that appear in quests. They have a home location and can belong to multiple factions.

### Schema

```jsonc
{
  "id": "marcus",
  "name": "Marcus the Barman",

  // Minimal appearance info
  "appearance": {
    "race": "human",
    "gender": "male",
    "age_range": "middle-aged",
    "notable_features": "thick mustache, barrel-chested, ale-stained apron"
  },

  // Free-form personality description
  "personality": "Jovial and welcoming to regulars, but sharp-eyed and protective of his establishment. Business-minded but fair. Treats the tavern like family.",

  // Speech patterns for AI dialog writing
  "speech": {
    "formality": "casual",
    "vocabulary": "simple, uses food and drink metaphors",
    "verbal_tics": ["'ey now", "if you catch my drift", "what'll it be"],
    "avoids": ["profanity", "complex words", "political opinions"],
    "sample_lines": [
      "What can I get ya? The stew's fresh today.",
      "Trouble's brewin', and I don't mean the ale.",
      "You've got the look of someone who needs a drink and a story."
    ]
  },

  // Free-form backstory for AI context
  "backstory": "Marcus inherited the Brightmug Tavern from his father twenty years ago. In his youth, he ran with a rough crowd and has connections he'd rather forget. Now he's focused on running an honest business and keeping his regulars safe. He knows everyone's secrets but keeps them close.",

  // Where this character lives/defaults to
  "home_location": "brightmug_tavern",

  // Runtime extras added by quest outcomes (not in base file, managed by game)
  // Stored in save data, not in character JSON
  "extras": []
}
```

### Character Extras

Extras are modifiers added to characters by quest outcomes. They append to the character's personality/state description.

```jsonc
// Runtime extras (stored in save data, not character file)
"extras": [
  {"text": "has become cynical after the betrayal", "source": "tavern.L3_competition.betrayed"},
  {"text": "walks with a limp from the ambush", "source": "mayor.L2_ambush.injured"}
]
```

Extras can be:
- **Added** by quest outcomes: `"add_character_extra"`
- **Removed** by quest outcomes: `"remove_character_extra"`

When generating AI content, the full character context is: `personality + " " + joined(extras)`

---

## Location Data Format

Locations are places where quests occur. They have multiple states (variants) that can be swapped based on quest outcomes.

### Schema

```jsonc
{
  "id": "brightmug_tavern",
  "name": "The Brightmug Tavern",

  // Location tags for quest generation and categorization
  "tags": ["indoor", "public", "safe", "social", "commerce"],

  // Connected locations (can vary by state)
  "default_connections": ["town_square", "back_alley"],

  // State variants - each state has its own scene, description, atmosphere
  "states": {
    "normal": {
      "scene": "res://scenes/locations/brightmug_tavern.tscn",
      "description": "A warm, bustling tavern with a crackling hearth at its center. Wooden beams overhead are darkened by years of smoke, and the air smells of roasting meat and fresh ale. Regulars cluster at worn tables while Marcus tends the bar.",
      "atmosphere": {
        "lighting": "warm candlelight, hearth glow",
        "sounds": "murmuring patrons, clinking mugs, crackling fire",
        "smells": "roasting meat, ale, wood smoke",
        "mood": "welcoming, busy, safe haven"
      },
      "connections": null  // Uses default_connections
    },
    "burned": {
      "scene": "res://scenes/locations/brightmug_tavern_burned.tscn",
      "description": "Charred beams and ash mark where the Brightmug Tavern once stood. The hearth is cold rubble. A few scorched tables remain, monuments to what was lost. The smell of smoke still lingers.",
      "atmosphere": {
        "lighting": "harsh daylight through collapsed roof",
        "sounds": "wind through ruins, creaking timber",
        "smells": "old smoke, wet ash",
        "mood": "somber, loss, aftermath"
      },
      "connections": ["town_square"]  // Back alley blocked by debris
    },
    "rebuilt": {
      "scene": "res://scenes/locations/brightmug_tavern_rebuilt.tscn",
      "description": "The new Brightmug Tavern rises from the ashes, larger than before. Fresh timber and a new stone hearth. It lacks the character of the old place, but Marcus has filled it with familiar faces.",
      "atmosphere": {
        "lighting": "bright, new lanterns",
        "sounds": "construction sounds fading, new patrons",
        "smells": "fresh wood, paint, cooking",
        "mood": "hopeful, fresh start, rebuilding"
      },
      "connections": ["town_square", "back_alley", "new_cellar"]
    }
  },

  // Default state when game starts
  "default_state": "normal"
}
```

### Location State Changes

Location states are changed by the new `set_location_state` outcome type:

```jsonc
// In quest branch outcomes
"outcomes": {
  "set_location_state": {
    "brightmug_tavern": "burned"
  }
}
```

The game tracks current location states in WorldState. When a location's state changes:
1. The appropriate scene is loaded
2. Connections update based on state-specific connections (or default)
3. Characters at that location may need to relocate

---

## Faction Data Format

Factions are groups that have stats, quests, and member characters. Characters are assigned to factions in the faction file.

### Schema

```jsonc
{
  "id": "tavern",
  "name": "The Brightmug Tavern",

  // Whether faction is available from game start
  "unlocked": true,

  // Faction stats (modified by quest outcomes)
  "stats": {
    "reputation": 0,
    "wealth": 0
  },

  // Characters belonging to this faction
  // A character can appear in multiple faction member lists
  "members": ["marcus", "tavern_regular_1", "tavern_regular_2"],

  // Personality traits that accumulate from quest choices
  "personality_modifiers": []
}
```

### Character-Faction Relationship

- Characters are assigned to factions via the faction's `members` array
- A character can belong to multiple factions (e.g., a spy in two factions)
- The character file does NOT store faction membership (single source of truth in faction file)
- Quest journal shows the quest-giver character, not a faction "face"

---

## Quest Data Format (Updated)

Quests now reference characters and locations by ID instead of embedding data.

### Schema

```jsonc
{
  "id": "L2_missing_ale",
  "level": 2,
  "consequential": true,
  "summary": "Investigate the missing ale shipments and confront the bandits responsible.",
  "dialog_file": "res://data/dialog/tavern/L2_missing_ale.ink",

  // Location where quest dialog starts
  // Characters will be moved here when quest is active
  "location": "brightmug_tavern",

  // Characters involved in this quest (simple ID list)
  // These characters will be present at the quest location
  "characters": ["marcus", "bandit_leader", "caravan_driver"],

  // Requirements (unchanged from current system)
  "requirements": {
    "min_world_level": 0,
    "quests_completed": ["tavern.L1_rats"],
    "quests_excluded": [],
    "faction_stats": {},
    "global_vars": {}
  },

  // Branches with updated outcome types
  "branches": [
    {
      "id": "kill_bandits",
      "summary": "Exterminate the bandit gang completely",
      "requirements": {},
      "outcomes": {
        "faction_stats": {
          "tavern.reputation": 2,
          "tavern.wealth": 1
        },
        "global_vars": {
          "bandits_defeated": true
        },
        "unlocks": [],
        "triggers": [],
        "blocks": []
      }
    },
    {
      "id": "join_bandits",
      "summary": "Join the bandits and betray Marcus",
      "requirements": {},
      "outcomes": {
        "faction_stats": {
          "tavern.reputation": -3
        },
        "unlock_factions": ["thieves"],
        "add_character_extras": [
          {"character": "marcus", "text": "has become distrustful of strangers"}
        ]
      }
    },
    {
      "id": "burn_tavern",
      "summary": "The confrontation goes wrong and the tavern burns",
      "requirements": {},
      "outcomes": {
        "set_location_state": {
          "brightmug_tavern": "burned"
        },
        "add_character_extras": [
          {"character": "marcus", "text": "is devastated by the loss of his tavern"}
        ]
      }
    }
  ],

  // Auto-resolve config (unchanged)
  "auto_resolve": {
    "type": "weighted_random",
    "weights": {
      "kill_bandits": 0.6,
      "join_bandits": 0.3,
      "burn_tavern": 0.1
    }
  }
}
```

### New Outcome Types

| Outcome Type | Description | Format |
|--------------|-------------|--------|
| `set_location_state` | Change a location's current state | `{"location_id": "new_state"}` |
| `add_character_extras` | Add modifier text to character | `[{"character": "id", "text": "description"}]` |
| `remove_character_extras` | Remove modifier from character | `[{"character": "id", "text": "description"}]` or `[{"character": "id", "source": "quest.branch"}]` |

### Character Movement

When a quest becomes active:
1. All characters in the quest's `characters` list are moved to the quest's `location`
2. Characters remain there during the quest
3. When quest completes, characters return to their `home_location`

---

## Quest Journal Changes

The quest journal UI no longer shows a faction "face" character. Instead:
- Shows the quest-giver (first character in quest's `characters` list, or explicitly marked)
- Displays quest location
- Shows faction name and stats as before

---

## Editor Features

### New "Content Status" Tab

A new tab in the quest editor showing missing/incomplete content:

#### Missing Content Report
- **Missing character files**: Character IDs referenced in quests but no JSON file exists
- **Missing location files**: Location IDs referenced but no JSON file exists
- **Missing scene files**: Location states reference .tscn files that don't exist
- **Missing dialog files**: Quests reference .ink files that don't exist
- **Incomplete characters**: Characters missing required fields (appearance, personality, etc.)
- **Incomplete locations**: Locations without any states defined
- **Orphaned files**: JSON/scene files that exist but are never referenced

#### Export Options
- View in editor panel
- Export as markdown file
- Export as CSV for tracking

### Reverse Reference Lookup

Click on any entity to see where it's used:
- Character → all quests they appear in, their faction(s)
- Location → all quests that use it, characters who live there
- Faction → all members, all quests

### Entity Relationship Graph

New editor tab showing visual web of:
- Characters ↔ Locations (home)
- Characters ↔ Factions (membership)
- Characters ↔ Quests (involvement)
- Locations ↔ Quests (setting)
- Quests ↔ Quests (requirements, unlocks, triggers, blocks)

### Variant Timeline View

For locations with multiple states:
- Visual timeline showing when states can change
- Which quest outcomes trigger which state changes
- Detection of impossible states (e.g., rebuilt before burned)

---

## Data Migration

Existing quest data will be auto-extracted:
1. Parse current quest JSON files
2. Extract character info into new character files
3. Create location files from implicit location references
4. Update quest files to use new reference format
5. Generate migration report showing what was extracted

---

## Runtime Systems

### WorldState Updates

WorldState needs to track:
- Current location states: `location_states: Dictionary` mapping location_id → current_state
- Character extras: `character_extras: Dictionary` mapping character_id → Array of extras
- Character positions: `character_locations: Dictionary` mapping character_id → current_location_id

### Character Manager (New)

New autoload singleton `CharacterManager`:
- Load character definitions from JSON
- Track character extras (runtime modifications)
- Track character current locations
- Move characters for quests
- Return characters home after quest completion

### Location Manager (New)

New autoload singleton `LocationManager`:
- Load location definitions from JSON
- Track current state per location
- Handle state transitions
- Provide current scene path for location
- Update connections based on state

---

## AI Quest Generation Context

When generating quest content, the AI receives:

### Character Context
```
Name: Marcus the Barman
Appearance: human, male, middle-aged, thick mustache, barrel-chested, ale-stained apron
Personality: Jovial and welcoming to regulars... [full personality]
Speech: casual, simple vocabulary, uses food metaphors, says "'ey now"...
Backstory: Marcus inherited the Brightmug Tavern... [full backstory]
Current State: has become distrustful of strangers (from tavern.L2_missing_ale.join_bandits)
```

### Location Context
```
Name: The Brightmug Tavern
Current State: burned
Description: Charred beams and ash mark where the Brightmug Tavern once stood...
Atmosphere: harsh daylight, wind through ruins, old smoke, somber mood
Tags: indoor, public (formerly safe)
```

This ensures consistent characterization and world state across all generated content.
