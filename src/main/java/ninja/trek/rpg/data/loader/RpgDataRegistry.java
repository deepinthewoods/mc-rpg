package ninja.trek.rpg.data.loader;

import ninja.trek.rpg.Mcrpg;
import ninja.trek.rpg.data.*;

import java.util.*;

public class RpgDataRegistry {

    private static final RpgDataRegistry INSTANCE = new RpgDataRegistry();

    private final Map<String, CharacterData> characters = new HashMap<>();
    private final Map<String, LocationData> locations = new HashMap<>();
    private final Map<String, FactionData> factions = new HashMap<>();
    private final Map<String, QuestData> quests = new HashMap<>(); // keyed by fullId
    private final Map<String, DialogTreeData> dialogs = new HashMap<>();

    private RpgDataRegistry() {}

    public static RpgDataRegistry getInstance() {
        return INSTANCE;
    }

    public void clear() {
        characters.clear();
        locations.clear();
        factions.clear();
        quests.clear();
        dialogs.clear();
    }

    public void registerCharacter(CharacterData data) {
        characters.put(data.id(), data);
    }

    public void registerLocation(LocationData data) {
        locations.put(data.id(), data);
    }

    public void registerFaction(FactionData data) {
        factions.put(data.id(), data);
    }

    public void registerQuest(QuestData data) {
        quests.put(data.fullId(), data);
    }

    public void registerDialog(DialogTreeData data) {
        dialogs.put(data.id(), data);
    }

    public CharacterData getCharacter(String id) {
        return characters.get(id);
    }

    public LocationData getLocation(String id) {
        return locations.get(id);
    }

    public FactionData getFaction(String id) {
        return factions.get(id);
    }

    public QuestData getQuest(String fullId) {
        return quests.get(fullId);
    }

    public DialogTreeData getDialog(String id) {
        return dialogs.get(id);
    }

    public Collection<CharacterData> getAllCharacters() {
        return Collections.unmodifiableCollection(characters.values());
    }

    public Collection<LocationData> getAllLocations() {
        return Collections.unmodifiableCollection(locations.values());
    }

    public Collection<FactionData> getAllFactions() {
        return Collections.unmodifiableCollection(factions.values());
    }

    public Collection<QuestData> getAllQuests() {
        return Collections.unmodifiableCollection(quests.values());
    }

    public Collection<DialogTreeData> getAllDialogs() {
        return Collections.unmodifiableCollection(dialogs.values());
    }

    public List<QuestData> getQuestsForFaction(String factionId) {
        return quests.values().stream()
            .filter(q -> q.factionId().equals(factionId))
            .toList();
    }

    public void validate() {
        Mcrpg.LOGGER.info("Validating RPG data cross-references...");
        int warnings = 0;

        for (QuestData quest : quests.values()) {
            // Check quest references valid location
            if (getLocation(quest.location()) == null) {
                Mcrpg.LOGGER.warn("Quest {} references unknown location: {}", quest.fullId(), quest.location());
                warnings++;
            }

            // Check quest references valid characters
            for (String charId : quest.characters()) {
                if (getCharacter(charId) == null) {
                    Mcrpg.LOGGER.warn("Quest {} references unknown character: {}", quest.fullId(), charId);
                    warnings++;
                }
            }

            // Check quest references valid faction
            if (getFaction(quest.factionId()) == null) {
                Mcrpg.LOGGER.warn("Quest {} references unknown faction: {}", quest.fullId(), quest.factionId());
                warnings++;
            }

            // Check dialog reference
            if (!quest.dialogId().isEmpty() && getDialog(quest.dialogId()) == null) {
                Mcrpg.LOGGER.warn("Quest {} references unknown dialog: {}", quest.fullId(), quest.dialogId());
                warnings++;
            }

            // Check branch unlocks/triggers/blocks reference valid quests
            for (QuestBranch branch : quest.branches()) {
                for (String ref : branch.outcomes().unlocks()) {
                    if (getQuest(ref) == null) {
                        Mcrpg.LOGGER.warn("Quest {} branch {} unlocks unknown quest: {}", quest.fullId(), branch.id(), ref);
                        warnings++;
                    }
                }
                for (String ref : branch.outcomes().triggers()) {
                    if (getQuest(ref) == null) {
                        Mcrpg.LOGGER.warn("Quest {} branch {} triggers unknown quest: {}", quest.fullId(), branch.id(), ref);
                        warnings++;
                    }
                }
                for (String ref : branch.outcomes().blocks()) {
                    if (getQuest(ref) == null) {
                        Mcrpg.LOGGER.warn("Quest {} branch {} blocks unknown quest: {}", quest.fullId(), branch.id(), ref);
                        warnings++;
                    }
                }
            }
        }

        // Check character home locations
        for (CharacterData character : characters.values()) {
            if (getLocation(character.homeLocation()) == null) {
                Mcrpg.LOGGER.warn("Character {} has unknown home_location: {}", character.id(), character.homeLocation());
                warnings++;
            }
        }

        // Check faction members
        for (FactionData faction : factions.values()) {
            for (String memberId : faction.members()) {
                if (getCharacter(memberId) == null) {
                    Mcrpg.LOGGER.warn("Faction {} references unknown member: {}", faction.id(), memberId);
                    warnings++;
                }
            }
        }

        if (warnings == 0) {
            Mcrpg.LOGGER.info("RPG data validation passed with no warnings.");
        } else {
            Mcrpg.LOGGER.warn("RPG data validation completed with {} warning(s).", warnings);
        }
    }
}
