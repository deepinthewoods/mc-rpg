package ninja.trek.rpg.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Map;

public record DialogTreeData(
    String id,
    String startNode,
    Map<String, DialogNode> nodes
) {
    public static final Codec<DialogTreeData> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.STRING.fieldOf("id").forGetter(DialogTreeData::id),
            Codec.STRING.fieldOf("start_node").forGetter(DialogTreeData::startNode),
            Codec.unboundedMap(Codec.STRING, DialogNode.CODEC).fieldOf("nodes").forGetter(DialogTreeData::nodes)
        ).apply(instance, DialogTreeData::new)
    );
}
