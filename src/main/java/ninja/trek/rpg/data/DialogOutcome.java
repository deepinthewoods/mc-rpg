package ninja.trek.rpg.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Map;
import java.util.Optional;

public record DialogOutcome(
    Optional<String> selectBranch,
    Map<String, Integer> factionStats,
    Map<String, Boolean> globalVars,
    Map<String, Integer> giveItems,
    Map<String, Integer> takeItems
) {
    public static final Codec<DialogOutcome> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.STRING.optionalFieldOf("select_branch").forGetter(DialogOutcome::selectBranch),
            Codec.unboundedMap(Codec.STRING, Codec.INT).optionalFieldOf("faction_stats", Map.of()).forGetter(DialogOutcome::factionStats),
            Codec.unboundedMap(Codec.STRING, Codec.BOOL).optionalFieldOf("global_vars", Map.of()).forGetter(DialogOutcome::globalVars),
            Codec.unboundedMap(Codec.STRING, Codec.INT).optionalFieldOf("give_items", Map.of()).forGetter(DialogOutcome::giveItems),
            Codec.unboundedMap(Codec.STRING, Codec.INT).optionalFieldOf("take_items", Map.of()).forGetter(DialogOutcome::takeItems)
        ).apply(instance, DialogOutcome::new)
    );
}
