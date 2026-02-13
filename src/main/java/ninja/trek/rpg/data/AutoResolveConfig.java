package ninja.trek.rpg.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Map;
import java.util.Optional;

public record AutoResolveConfig(
    AutoResolveType type,
    Map<String, Integer> weights,
    Optional<String> fallback
) {
    public enum AutoResolveType {
        WEIGHTED_RANDOM,
        PREDETERMINED,
        RANDOM,
        FAIL;

        public static final Codec<AutoResolveType> CODEC = Codec.STRING.xmap(
            s -> AutoResolveType.valueOf(s.toUpperCase()),
            t -> t.name().toLowerCase()
        );
    }

    public static final Codec<AutoResolveConfig> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            AutoResolveType.CODEC.fieldOf("type").forGetter(AutoResolveConfig::type),
            Codec.unboundedMap(Codec.STRING, Codec.INT).optionalFieldOf("weights", Map.of()).forGetter(AutoResolveConfig::weights),
            Codec.STRING.optionalFieldOf("fallback").forGetter(AutoResolveConfig::fallback)
        ).apply(instance, AutoResolveConfig::new)
    );
}
