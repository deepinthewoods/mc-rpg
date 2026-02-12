# Quest Data Format

Quests are organized by faction and stored in JSON files in `data/quests/`. Each file contains a faction definition with its embedded quests.

## File Structure

```
data/quests/
  tavern.json     # Tavern faction and quests
  mayor.json      # Mayor's Office faction and quests
  thieves.json    # Thieves Guild faction and quests
```

## Faction Schema

```json
{
  "faction": {
    "id": "string",                    // Unique faction identifier
    "name": "string",                  // Display name
    "character": "string",             // DEPRECATED: Use members instead
    "personality": ["string"],         // Faction personality traits
    "members": ["string"],             // Character IDs belonging to this faction
    "stats": {                         // Faction-specific statistics
      "reputation": 0,
      "wealth": 0
    },
    "quests": [Quest]                  // Array of quest definitions
  }
}
```

## Quest Schema

```json
{
  "id": "string",                      // Unique quest ID within faction
  "level": 1,                          // Quest level (1-10)
  "consequential": false,              // Whether choices have lasting impact
  "summary": "string",                 // Brief quest description
  "dialog_file": "string",             // Path to .ink dialog file
  "location": "string",                // Location ID where quest takes place
  "characters": ["string"],            // Character IDs involved in quest
  "requirements": Requirements,         // Prerequisites (optional)
  "branches": [Branch],                // Possible outcomes
  "auto_resolve": AutoResolve          // How quest resolves if ignored
}
```

## Requirements

```json
{
  "quests_completed": ["faction.quest_id"],  // Required completed quests
  "faction_stats": {                          // Required faction stats
    "faction.stat": ">= value"
  },
  "global_vars": {                            // Required global variables
    "var_name": true
  }
}
```

## Branch Schema

```json
{
  "id": "string",                      // Unique branch ID within quest
  "summary": "string",                 // Branch description
  "requirements": {                    // Branch-specific requirements (optional)
    "skill_check": {
      "type": "persuasion",            // Skill type
      "dc": 14                         // Difficulty class
    }
  },
  "outcomes": Outcomes,                // What happens when this branch completes
  "unlocks": ["faction.quest_id"],     // Quests to unlock (optional)
  "triggers": ["faction.quest_id"],    // Quests to immediately trigger (optional)
  "blocks": ["faction.quest_id"]       // Quests to permanently block (optional)
}
```

## Outcomes

```json
{
  "faction_stats": {                   // Modify faction statistics
    "faction.stat": 2                  // Add/subtract from stat
  },
  "global_vars": {                     // Set global variables
    "var_name": true
  },
  "personality_modifiers": {           // Add character extras
    "character_id": ["trait"]
  },
  "location_states": {                 // Change location states
    "location_id": "new_state"
  },
  "move_characters": {                 // Move characters
    "character_id": "location_id"
  }
}
```

## Auto-Resolve Types

When a quest is ignored, it resolves automatically:

### weighted_random
```json
{
  "type": "weighted_random",
  "weights": {
    "branch_id": 0.6,
    "other_branch": 0.4
  }
}
```

### predetermined
```json
{
  "type": "predetermined",
  "fallback": "branch_id"
}
```

### random
```json
{
  "type": "random"
}
```

### fail
```json
{
  "type": "fail"
}
```

## Complete Example

```json
{
  "id": "L2_missing_ale",
  "level": 2,
  "consequential": true,
  "summary": "Ale shipment ambushed by bandits on road",
  "dialog_file": "tavern/L2_missing_ale.ink",
  "location": "trade_road",
  "characters": ["marcus"],
  "requirements": {
    "quests_completed": ["tavern.L1_rats"],
    "faction_stats": {
      "tavern.reputation": ">= 0"
    }
  },
  "branches": [
    {
      "id": "kill_bandits",
      "summary": "Slaughter the bandits",
      "outcomes": {
        "faction_stats": {
          "tavern.reputation": 2,
          "tavern.wealth": 1
        },
        "global_vars": {
          "bandits_defeated": true
        }
      }
    },
    {
      "id": "spare_bandits",
      "summary": "Insight reveals bandits under duress, spare them",
      "requirements": {
        "skill_check": {
          "type": "insight",
          "dc": 15
        }
      },
      "outcomes": {
        "faction_stats": {
          "tavern.wealth": 3
        },
        "global_vars": {
          "bandits_spared": true
        },
        "personality_modifiers": {
          "marcus": ["trusting"]
        }
      },
      "triggers": ["mayor.L3_mind_controlled"]
    }
  ],
  "auto_resolve": {
    "type": "weighted_random",
    "weights": {
      "kill_bandits": 0.8,
      "spare_bandits": 0.2
    }
  }
}
```

## Quest ID Format

Full quest IDs use the format `faction.quest_id`:
- `tavern.L1_rats`
- `mayor.L2_ledger`
- `thieves.L3_blackmail`

## QuestManager API

- `get_available_quests()` - Get quests player can accept
- `accept_quest(quest_id)` - Start a quest
- `complete_quest(quest_id, branch_id)` - Complete with chosen branch
- `get_quest_state(quest_id)` - Get current state
- `is_quest_completed(quest_id)` - Check if completed
- `get_completed_branch(quest_id)` - Get which branch was chosen

## Character/Location Integration

When a quest is accepted:
1. Characters listed in `characters` are moved to `location`
2. Player can interact with them at that location

When a quest is completed:
1. Outcomes are applied (stats, vars, location states)
2. Characters return to their `home_location`
3. Unlocked quests become available
4. Triggered quests begin immediately
