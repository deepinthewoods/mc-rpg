package ninja.trek.rpg.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

public record DialogResponse(
    String text,
    String nextNode,
    Optional<DialogCondition> condition
) {
    public static final Codec<DialogResponse> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.STRING.fieldOf("text").forGetter(DialogResponse::text),
            Codec.STRING.fieldOf("next_node").forGetter(DialogResponse::nextNode),
            DialogCondition.CODEC.optionalFieldOf("condition").forGetter(DialogResponse::condition)
        ).apply(instance, DialogResponse::new)
    );
}
