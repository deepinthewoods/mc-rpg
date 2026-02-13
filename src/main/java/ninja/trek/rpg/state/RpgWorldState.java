package ninja.trek.rpg.state;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.DimensionDataStorage;
import ninja.trek.rpg.Mcrpg;

import java.util.*;

public class RpgWorldState extends SavedData {

    private static final String DATA_NAME = "mc_rpg_world_state";

    private final PartyData party = new PartyData();
    private final Map<String, QuestState> questStates = new HashMap<>();
    private final Map<String, String> completedBranches = new HashMap<>();
    private final Map<String, Long> activeQuestTimers = new HashMap<>();
    private final Map<String, Map<String, Integer>> factionStats = new HashMap<>();
    private final Map<String, String> locationStates = new HashMap<>();
    private final Map<String, Boolean> globalVars = new HashMap<>();
    private final Map<String, List<CharacterExtra>> characterExtras = new HashMap<>();
    private final Map<String, String> characterLocations = new HashMap<>();

    public static final Codec<RpgWorldState> CODEC = CompoundTag.CODEC.xmap(
        RpgWorldState::load,
        state -> {
            CompoundTag tag = new CompoundTag();
            return state.saveToTag(tag);
        }
    );

    public static final SavedDataType<RpgWorldState> TYPE = new SavedDataType<>(
        DATA_NAME, RpgWorldState::new, CODEC, null
    );

    public static RpgWorldState get(MinecraftServer server) {
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        DimensionDataStorage storage = overworld.getDataStorage();
        return storage.computeIfAbsent(TYPE);
    }

    public RpgWorldState() {}

    // Party
    public PartyData getParty() {
        return party;
    }

    // Quest States
    public QuestState getQuestState(String fullId) {
        return questStates.getOrDefault(fullId, QuestState.BLOCKED);
    }

    public void setQuestState(String fullId, QuestState state) {
        questStates.put(fullId, state);
        setDirty();
    }

    public Map<String, QuestState> getAllQuestStates() {
        return Collections.unmodifiableMap(questStates);
    }

    // Completed Branches
    public String getCompletedBranch(String fullId) {
        return completedBranches.get(fullId);
    }

    public void setCompletedBranch(String fullId, String branchId) {
        completedBranches.put(fullId, branchId);
        setDirty();
    }

    // Quest Timers
    public Long getQuestTimer(String fullId) {
        return activeQuestTimers.get(fullId);
    }

    public void setQuestTimer(String fullId, long tick) {
        activeQuestTimers.put(fullId, tick);
        setDirty();
    }

    public void removeQuestTimer(String fullId) {
        activeQuestTimers.remove(fullId);
        setDirty();
    }

    public Map<String, Long> getAllQuestTimers() {
        return Collections.unmodifiableMap(activeQuestTimers);
    }

    // Faction Stats
    public int getFactionStat(String factionId, String stat) {
        return factionStats.getOrDefault(factionId, Map.of()).getOrDefault(stat, 0);
    }

    public void setFactionStat(String factionId, String stat, int value) {
        factionStats.computeIfAbsent(factionId, k -> new HashMap<>()).put(stat, value);
        setDirty();
    }

    public void addFactionStat(String factionId, String stat, int delta) {
        int current = getFactionStat(factionId, stat);
        setFactionStat(factionId, stat, current + delta);
    }

    public Map<String, Map<String, Integer>> getAllFactionStats() {
        return Collections.unmodifiableMap(factionStats);
    }

    // Location States
    public String getLocationState(String locationId) {
        return locationStates.get(locationId);
    }

    public void setLocationState(String locationId, String state) {
        locationStates.put(locationId, state);
        setDirty();
    }

    // Global Vars
    public boolean getGlobalVar(String key) {
        return globalVars.getOrDefault(key, false);
    }

    public void setGlobalVar(String key, boolean value) {
        globalVars.put(key, value);
        setDirty();
    }

    public Map<String, Boolean> getAllGlobalVars() {
        return Collections.unmodifiableMap(globalVars);
    }

    // Character Extras
    public List<CharacterExtra> getCharacterExtras(String characterId) {
        return characterExtras.getOrDefault(characterId, List.of());
    }

    public void addCharacterExtra(String characterId, CharacterExtra extra) {
        characterExtras.computeIfAbsent(characterId, k -> new ArrayList<>()).add(extra);
        setDirty();
    }

    public void removeCharacterExtrasBySource(String characterId, String source) {
        List<CharacterExtra> extras = characterExtras.get(characterId);
        if (extras != null) {
            extras.removeIf(e -> e.source().equals(source));
            setDirty();
        }
    }

    // Character Locations
    public String getCharacterLocation(String characterId) {
        return characterLocations.get(characterId);
    }

    public void setCharacterLocation(String characterId, String locationId) {
        characterLocations.put(characterId, locationId);
        setDirty();
    }

    public Map<String, String> getAllCharacterLocations() {
        return Collections.unmodifiableMap(characterLocations);
    }

    // NBT Serialization

    public CompoundTag saveToTag(CompoundTag root) {
        root.put("Party", party.toNbt());

        // Quest states
        CompoundTag questTag = new CompoundTag();
        questStates.forEach((k, v) -> questTag.putString(k, v.name()));
        root.put("QuestStates", questTag);

        // Completed branches
        CompoundTag branchTag = new CompoundTag();
        completedBranches.forEach(branchTag::putString);
        root.put("CompletedBranches", branchTag);

        // Quest timers
        CompoundTag timerTag = new CompoundTag();
        activeQuestTimers.forEach(timerTag::putLong);
        root.put("QuestTimers", timerTag);

        // Faction stats
        CompoundTag factionTag = new CompoundTag();
        factionStats.forEach((factionId, stats) -> {
            CompoundTag statsTag = new CompoundTag();
            stats.forEach(statsTag::putInt);
            factionTag.put(factionId, statsTag);
        });
        root.put("FactionStats", factionTag);

        // Location states
        CompoundTag locTag = new CompoundTag();
        locationStates.forEach(locTag::putString);
        root.put("LocationStates", locTag);

        // Global vars
        CompoundTag varTag = new CompoundTag();
        globalVars.forEach(varTag::putBoolean);
        root.put("GlobalVars", varTag);

        // Character extras
        CompoundTag extrasTag = new CompoundTag();
        characterExtras.forEach((charId, extras) -> {
            ListTag list = new ListTag();
            extras.forEach(e -> list.add(e.toNbt()));
            extrasTag.put(charId, list);
        });
        root.put("CharacterExtras", extrasTag);

        // Character locations
        CompoundTag charLocTag = new CompoundTag();
        characterLocations.forEach(charLocTag::putString);
        root.put("CharacterLocations", charLocTag);

        return root;
    }

    public static RpgWorldState load(CompoundTag root) {
        RpgWorldState state = new RpgWorldState();

        // Party
        if (root.contains("Party")) {
            CompoundTag partyTag = root.getCompoundOrEmpty("Party");
            PartyData loaded = PartyData.fromNbt(partyTag);
            state.party.setLeader(loaded.getLeader());
            loaded.getMembers().forEach(state.party::addMember);
        }

        // Quest states
        CompoundTag questTag = root.getCompoundOrEmpty("QuestStates");
        for (String key : questTag.keySet()) {
            try {
                state.questStates.put(key, QuestState.valueOf(questTag.getStringOr(key, "BLOCKED")));
            } catch (IllegalArgumentException ignored) {}
        }

        // Completed branches
        CompoundTag branchTag = root.getCompoundOrEmpty("CompletedBranches");
        for (String key : branchTag.keySet()) {
            state.completedBranches.put(key, branchTag.getStringOr(key, ""));
        }

        // Quest timers
        CompoundTag timerTag = root.getCompoundOrEmpty("QuestTimers");
        for (String key : timerTag.keySet()) {
            state.activeQuestTimers.put(key, timerTag.getLongOr(key, 0L));
        }

        // Faction stats
        CompoundTag factionTag = root.getCompoundOrEmpty("FactionStats");
        for (String factionId : factionTag.keySet()) {
            CompoundTag statsTag = factionTag.getCompoundOrEmpty(factionId);
            Map<String, Integer> stats = new HashMap<>();
            for (String stat : statsTag.keySet()) {
                stats.put(stat, statsTag.getIntOr(stat, 0));
            }
            state.factionStats.put(factionId, stats);
        }

        // Location states
        CompoundTag locTag = root.getCompoundOrEmpty("LocationStates");
        for (String key : locTag.keySet()) {
            state.locationStates.put(key, locTag.getStringOr(key, ""));
        }

        // Global vars
        CompoundTag varTag = root.getCompoundOrEmpty("GlobalVars");
        for (String key : varTag.keySet()) {
            state.globalVars.put(key, varTag.getBooleanOr(key, false));
        }

        // Character extras
        CompoundTag extrasTag = root.getCompoundOrEmpty("CharacterExtras");
        for (String charId : extrasTag.keySet()) {
            ListTag list = extrasTag.getListOrEmpty(charId);
            List<CharacterExtra> extras = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i) instanceof CompoundTag ct) {
                    extras.add(CharacterExtra.fromNbt(ct));
                }
            }
            state.characterExtras.put(charId, extras);
        }

        // Character locations
        CompoundTag charLocTag = root.getCompoundOrEmpty("CharacterLocations");
        for (String key : charLocTag.keySet()) {
            state.characterLocations.put(key, charLocTag.getStringOr(key, ""));
        }

        return state;
    }
}
