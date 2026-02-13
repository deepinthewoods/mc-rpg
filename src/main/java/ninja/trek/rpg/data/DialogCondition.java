package ninja.trek.rpg.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

public record DialogCondition(
    Optional<String> questCompleted,
    Optional<String> factionStat,
    Optional<String> hasItem,
    Optional<Integer> hasItemCount,
    Optional<String> globalVar
) {
    public static final Codec<DialogCondition> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.STRING.optionalFieldOf("quest_completed").forGetter(DialogCondition::questCompleted),
            Codec.STRING.optionalFieldOf("faction_stat").forGetter(DialogCondition::factionStat),
            Codec.STRING.optionalFieldOf("has_item").forGetter(DialogCondition::hasItem),
            Codec.INT.optionalFieldOf("has_item_count").forGetter(DialogCondition::hasItemCount),
            Codec.STRING.optionalFieldOf("global_var").forGetter(DialogCondition::globalVar)
        ).apply(instance, DialogCondition::new)
    );
}
