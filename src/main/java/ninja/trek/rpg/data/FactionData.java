package ninja.trek.rpg.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Map;

public record FactionData(
    String id,
    String name,
    boolean unlocked,
    Map<String, Integer> stats,
    List<String> members
) {
    public static final Codec<FactionData> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.STRING.fieldOf("id").forGetter(FactionData::id),
            Codec.STRING.fieldOf("name").forGetter(FactionData::name),
            Codec.BOOL.optionalFieldOf("unlocked", true).forGetter(FactionData::unlocked),
            Codec.unboundedMap(Codec.STRING, Codec.INT).optionalFieldOf("stats", Map.of()).forGetter(FactionData::stats),
            Codec.STRING.listOf().optionalFieldOf("members", List.of()).forGetter(FactionData::members)
        ).apply(instance, FactionData::new)
    );
}
