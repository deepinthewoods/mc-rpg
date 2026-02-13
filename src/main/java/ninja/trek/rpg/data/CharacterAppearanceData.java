package ninja.trek.rpg.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record CharacterAppearanceData(
    String race,
    String gender,
    String ageRange,
    List<String> notableFeatures
) {
    public static final Codec<CharacterAppearanceData> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.STRING.fieldOf("race").forGetter(CharacterAppearanceData::race),
            Codec.STRING.fieldOf("gender").forGetter(CharacterAppearanceData::gender),
            Codec.STRING.fieldOf("age_range").forGetter(CharacterAppearanceData::ageRange),
            Codec.STRING.listOf().optionalFieldOf("notable_features", List.of()).forGetter(CharacterAppearanceData::notableFeatures)
        ).apply(instance, CharacterAppearanceData::new)
    );
}
