package ninja.trek.rpg.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record CharacterData(
    String id,
    String name,
    CharacterAppearanceData appearance,
    String personality,
    SpeechData speech,
    String backstory,
    String homeLocation
) {
    public static final Codec<CharacterData> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.STRING.fieldOf("id").forGetter(CharacterData::id),
            Codec.STRING.fieldOf("name").forGetter(CharacterData::name),
            CharacterAppearanceData.CODEC.fieldOf("appearance").forGetter(CharacterData::appearance),
            Codec.STRING.optionalFieldOf("personality", "").forGetter(CharacterData::personality),
            SpeechData.CODEC.fieldOf("speech").forGetter(CharacterData::speech),
            Codec.STRING.optionalFieldOf("backstory", "").forGetter(CharacterData::backstory),
            Codec.STRING.fieldOf("home_location").forGetter(CharacterData::homeLocation)
        ).apply(instance, CharacterData::new)
    );
}
