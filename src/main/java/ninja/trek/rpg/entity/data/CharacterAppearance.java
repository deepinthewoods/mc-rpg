package ninja.trek.rpg.entity.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

/**
 * Defines which mesh parts to use for rendering a character.
 * Each index corresponds to a specific mesh variant in the model file.
 */
public record CharacterAppearance(
    int bodyIndex,
    int legsIndex,
    int armsIndex,
    int headIndex
) {
    public static final Codec<CharacterAppearance> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.INT.fieldOf("body").forGetter(CharacterAppearance::bodyIndex),
            Codec.INT.fieldOf("legs").forGetter(CharacterAppearance::legsIndex),
            Codec.INT.fieldOf("arms").forGetter(CharacterAppearance::armsIndex),
            Codec.INT.fieldOf("head").forGetter(CharacterAppearance::headIndex)
        ).apply(instance, CharacterAppearance::new)
    );

    /**
     * Create appearance from race defaults.
     */
    public static CharacterAppearance fromRace(Race race) {
        return new CharacterAppearance(
            race.getDefaultBodyIndex(),
            race.getDefaultLegsIndex(),
            race.getDefaultArmsIndex(),
            race.getDefaultHeadIndex()
        );
    }

    public String getBodyMesh() {
        return "body_" + bodyIndex;
    }

    public String getLegsMesh() {
        return "legs_" + legsIndex;
    }

    public String getArmsMesh() {
        return "arms_" + armsIndex;
    }

    public String getHeadMesh() {
        return "head_" + headIndex;
    }

    public List<String> getAllMeshNames() {
        return List.of(getBodyMesh(), getLegsMesh(), getArmsMesh(), getHeadMesh());
    }
}
