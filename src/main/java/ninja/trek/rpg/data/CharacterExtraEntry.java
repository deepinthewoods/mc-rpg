package ninja.trek.rpg.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record CharacterExtraEntry(
    String character,
    String text
) {
    public static final Codec<CharacterExtraEntry> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.STRING.fieldOf("character").forGetter(CharacterExtraEntry::character),
            Codec.STRING.fieldOf("text").forGetter(CharacterExtraEntry::text)
        ).apply(instance, CharacterExtraEntry::new)
    );
}
