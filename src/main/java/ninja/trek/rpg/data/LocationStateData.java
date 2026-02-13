package ninja.trek.rpg.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Optional;

public record LocationStateData(
    String description,
    AtmosphereData atmosphere,
    Optional<List<String>> connections
) {
    public static final Codec<LocationStateData> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.STRING.optionalFieldOf("description", "").forGetter(LocationStateData::description),
            AtmosphereData.CODEC.fieldOf("atmosphere").forGetter(LocationStateData::atmosphere),
            Codec.STRING.listOf().optionalFieldOf("connections").forGetter(LocationStateData::connections)
        ).apply(instance, LocationStateData::new)
    );
}
