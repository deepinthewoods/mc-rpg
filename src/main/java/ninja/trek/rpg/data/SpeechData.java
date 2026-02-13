package ninja.trek.rpg.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record SpeechData(
    String formality,
    String vocabulary,
    List<String> verbalTics,
    List<String> avoids,
    List<String> sampleLines
) {
    public static final Codec<SpeechData> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.STRING.fieldOf("formality").forGetter(SpeechData::formality),
            Codec.STRING.fieldOf("vocabulary").forGetter(SpeechData::vocabulary),
            Codec.STRING.listOf().optionalFieldOf("verbal_tics", List.of()).forGetter(SpeechData::verbalTics),
            Codec.STRING.listOf().optionalFieldOf("avoids", List.of()).forGetter(SpeechData::avoids),
            Codec.STRING.listOf().optionalFieldOf("sample_lines", List.of()).forGetter(SpeechData::sampleLines)
        ).apply(instance, SpeechData::new)
    );
}
