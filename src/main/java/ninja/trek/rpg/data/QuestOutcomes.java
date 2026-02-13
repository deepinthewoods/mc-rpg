package ninja.trek.rpg.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Map;

public record QuestOutcomes(
    Map<String, Integer> factionStats,
    Map<String, Boolean> globalVars,
    Map<String, String> locationStates,
    List<CharacterExtraEntry> characterExtras,
    Map<String, String> moveCharacters,
    List<String> unlocks,
    List<String> triggers,
    List<String> blocks,
    List<String> unlockFactions
) {
    public static final Codec<QuestOutcomes> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.unboundedMap(Codec.STRING, Codec.INT).optionalFieldOf("faction_stats", Map.of()).forGetter(QuestOutcomes::factionStats),
            Codec.unboundedMap(Codec.STRING, Codec.BOOL).optionalFieldOf("global_vars", Map.of()).forGetter(QuestOutcomes::globalVars),
            Codec.unboundedMap(Codec.STRING, Codec.STRING).optionalFieldOf("location_states", Map.of()).forGetter(QuestOutcomes::locationStates),
            CharacterExtraEntry.CODEC.listOf().optionalFieldOf("character_extras", List.of()).forGetter(QuestOutcomes::characterExtras),
            Codec.unboundedMap(Codec.STRING, Codec.STRING).optionalFieldOf("move_characters", Map.of()).forGetter(QuestOutcomes::moveCharacters),
            Codec.STRING.listOf().optionalFieldOf("unlocks", List.of()).forGetter(QuestOutcomes::unlocks),
            Codec.STRING.listOf().optionalFieldOf("triggers", List.of()).forGetter(QuestOutcomes::triggers),
            Codec.STRING.listOf().optionalFieldOf("blocks", List.of()).forGetter(QuestOutcomes::blocks),
            Codec.STRING.listOf().optionalFieldOf("unlock_factions", List.of()).forGetter(QuestOutcomes::unlockFactions)
        ).apply(instance, QuestOutcomes::new)
    );
}
