package ninja.trek.rpg.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;

public record StructureReference(String id, BlockPos position, float orientation) {

    public static final Codec<StructureReference> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.STRING.fieldOf("id").forGetter(StructureReference::id),
            BlockPos.CODEC.fieldOf("position").forGetter(StructureReference::position),
            Codec.FLOAT.fieldOf("orientation").forGetter(StructureReference::orientation)
        ).apply(instance, StructureReference::new)
    );
}
