package ninja.trek.rpg.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Optional;

public record DialogNode(
    String id,
    String speaker,
    String text,
    List<DialogResponse> responses,
    Optional<DialogOutcome> outcome
) {
    public static final Codec<DialogNode> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.STRING.fieldOf("id").forGetter(DialogNode::id),
            Codec.STRING.fieldOf("speaker").forGetter(DialogNode::speaker),
            Codec.STRING.fieldOf("text").forGetter(DialogNode::text),
            DialogResponse.CODEC.listOf().optionalFieldOf("responses", List.of()).forGetter(DialogNode::responses),
            DialogOutcome.CODEC.optionalFieldOf("outcome").forGetter(DialogNode::outcome)
        ).apply(instance, DialogNode::new)
    );
}
