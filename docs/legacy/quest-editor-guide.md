# Quest Editor Guide

The Quest Editor is a Godot plugin for creating and managing quest content. Access it through the Quest Editor dock in the Godot editor.

## Tabs Overview

### Editor Tab
The main quest editing interface with:
- **Quest Browser** (left panel): Tree view of all factions and quests
- **Quest Details** (right panel): Edit selected quest properties

### Overview Tab
Visual graph showing quest relationships and dependencies across all factions.

### Characters Tab
Create and edit character definitions. See [Character Data Format](character-data-format.md).

### Locations Tab
Create and edit location definitions. See [Location Data Format](location-data-format.md).

### Content Status Tab
Validation panel showing:
- Missing characters (referenced but no file exists)
- Missing locations (referenced but no file exists)
- Missing scenes (location states reference non-existent .tscn files)
- Missing dialog (quests reference non-existent .ink files)
- Incomplete characters (missing required fields)
- Incomplete locations (no states defined)
- Orphaned files (files that exist but are never referenced)

### Entity Graph Tab
Interactive relationship visualization:
- **Character nodes** (blue): Characters and their connections
- **Location nodes** (green): Locations and connections
- **Faction nodes** (purple): Factions and their members
- **Quest nodes** (orange): Quests and their requirements

Filter by entity type and click nodes to select, double-click to edit.

### Timeline Tab
Shows location state changes over quest progression:
- Horizontal timeline per location
- Markers showing which quests change states
- Warnings for impossible state transitions

## Quest Browser

The quest browser shows all factions and their quests in a tree:

```
Quests
├── The Brightmug Tavern
│   ├── L1: L1_rats [tavern_cellar, 1 char]
│   ├── L2: L2_missing_ale * [trade_road, 1 char]
│   └── L3: L3_competition [brightmug_tavern, 1 char]
├── Mayor's Office
│   └── ...
```

- **Level prefix**: `L1:`, `L2:`, etc.
- **Asterisk (*)**: Consequential quest (choices have lasting impact)
- **Brackets**: Location and character count

## Quest Details Panel

### Basic Properties
- **Faction**: The faction this quest belongs to (read-only)
- **Quest ID**: Unique identifier within the faction
- **Level**: Quest difficulty level (1-10)
- **Consequential**: Whether choices have permanent effects
- **Summary**: Brief description of the quest
- **Dialog File**: Path to the .ink dialog file (relative to `data/dialog/`)

### Location & Characters
- **Location**: Click to open in Locations tab
- **Characters**: Click character names to open in Characters tab

### Requirements
Prerequisites for the quest to become available:
- Required completed quests
- Required faction stats
- Required global variables

### Branches
Possible outcomes for the quest:
- Branch ID and summary
- Outcomes (stat changes, variable changes)
- Unlocks, triggers, and blocks

### Auto-Resolve
How the quest resolves if the player ignores it:
- Type (weighted_random, predetermined, random, fail)
- Weights or fallback branch

### References
Connected quests:
- **Requires**: Quests that must be completed first
- **Unlocks**: Quests that become available after completion
- **Triggers**: Quests that start immediately after completion
- **Required by**: Quests that require this quest

Click any quest reference to navigate to it.

## Creating New Content

### New Character
1. Go to **Characters** tab
2. Click **New Character**
3. Fill in required fields (id, name, home_location)
4. Click **Save**

### New Location
1. Go to **Locations** tab
2. Click **New Location**
3. Fill in required fields (id, name)
4. Add at least one state with a scene path
5. Click **Save**

### New Quest
Currently, quests must be added by editing the faction JSON file directly in `data/quests/`.

## Validation

The editor validates content automatically:
- **Green**: Valid, no issues
- **Yellow**: Warnings (non-critical issues)
- **Red**: Errors (must be fixed)

Common validation errors:
- Missing referenced characters or locations
- Missing dialog files
- Circular quest dependencies
- Invalid stat references

## Keyboard Shortcuts

- **Ctrl+S**: Save current quest
- **Ctrl+R**: Reload all data
- **Double-click** quest: Open in Editor tab
- **Double-click** entity in graph: Open appropriate editor

## Best Practices

1. **Use descriptive IDs**: `L2_missing_ale` not `quest2`
2. **Set location and characters**: Enables character movement
3. **Define auto-resolve**: All quests should have fallback behavior
4. **Check Content Status**: Run validation before committing
5. **Use references**: Click-to-navigate keeps you oriented
6. **Mark consequential quests**: Helps designers track impactful choices

## Troubleshooting

### Quest not showing up
- Check requirements are met
- Verify faction file is valid JSON
- Reload the editor (Reload button)

### Character not at location
- Verify `home_location` is set in character file
- Check CharacterManager is loaded (autoload order)

### Changes not saving
- Check for validation errors
- Ensure file permissions allow writing
- Try manual JSON edit as fallback
