package ninja.trek.rpg.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Map;

public record BranchRequirements(
    Map<String, Integer> itemRequirements
) {
    public static final Codec<BranchRequirements> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.unboundedMap(Codec.STRING, Codec.INT).optionalFieldOf("item_requirements", Map.of()).forGetter(BranchRequirements::itemRequirements)
        ).apply(instance, BranchRequirements::new)
    );

    public static final BranchRequirements EMPTY = new BranchRequirements(Map.of());
}
