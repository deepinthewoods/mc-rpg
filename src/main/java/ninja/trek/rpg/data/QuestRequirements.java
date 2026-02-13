package ninja.trek.rpg.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Map;

public record QuestRequirements(
    List<String> questsCompleted,
    Map<String, String> factionStats,
    Map<String, Boolean> globalVars
) {
    public static final Codec<QuestRequirements> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.STRING.listOf().optionalFieldOf("quests_completed", List.of()).forGetter(QuestRequirements::questsCompleted),
            Codec.unboundedMap(Codec.STRING, Codec.STRING).optionalFieldOf("faction_stats", Map.of()).forGetter(QuestRequirements::factionStats),
            Codec.unboundedMap(Codec.STRING, Codec.BOOL).optionalFieldOf("global_vars", Map.of()).forGetter(QuestRequirements::globalVars)
        ).apply(instance, QuestRequirements::new)
    );

    public static final QuestRequirements EMPTY = new QuestRequirements(List.of(), Map.of(), Map.of());
}
