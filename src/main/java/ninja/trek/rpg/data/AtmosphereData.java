package ninja.trek.rpg.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record AtmosphereData(
    String lighting,
    String sounds,
    String smells,
    String mood
) {
    public static final Codec<AtmosphereData> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.STRING.optionalFieldOf("lighting", "").forGetter(AtmosphereData::lighting),
            Codec.STRING.optionalFieldOf("sounds", "").forGetter(AtmosphereData::sounds),
            Codec.STRING.optionalFieldOf("smells", "").forGetter(AtmosphereData::smells),
            Codec.STRING.optionalFieldOf("mood", "").forGetter(AtmosphereData::mood)
        ).apply(instance, AtmosphereData::new)
    );
}
