package ninja.trek.rpg.entity.data;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

/**
 * Represents playable races for NPC characters.
 * Stripped of D&D 5e-specific traits (movement speed in feet, darkvision).
 * Adds sizeScale for visual scaling.
 */
public enum Race implements StringRepresentable {
    HUMAN(
        "human",
        0,  // default body index
        0,  // default legs index
        0,  // default arms index
        0,  // default head index
        1.0f // size scale
    ),
    DWARF(
        "dwarf",
        1,  // dwarf body
        1,  // dwarf legs
        1,  // dwarf arms
        2,  // dwarf head
        0.8f // shorter
    ),
    ELF(
        "elf",
        2,  // elf body (tall, slender)
        2,  // elf legs
        2,  // elf arms
        4,  // elf head (pointed ears)
        1.1f // taller
    ),
    HALFLING(
        "halfling",
        3,  // halfling body (small)
        3,  // halfling legs (short)
        3,  // halfling arms
        6,  // halfling head (small, child-like features)
        0.6f // smallest
    );

    public static final Codec<Race> CODEC = StringRepresentable.fromEnum(Race::values);

    private final String name;
    private final int defaultBodyIndex;
    private final int defaultLegsIndex;
    private final int defaultArmsIndex;
    private final int defaultHeadIndex;
    private final float sizeScale;

    Race(String name, int defaultBodyIndex, int defaultLegsIndex,
         int defaultArmsIndex, int defaultHeadIndex, float sizeScale) {
        this.name = name;
        this.defaultBodyIndex = defaultBodyIndex;
        this.defaultLegsIndex = defaultLegsIndex;
        this.defaultArmsIndex = defaultArmsIndex;
        this.defaultHeadIndex = defaultHeadIndex;
        this.sizeScale = sizeScale;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public String getName() {
        return name;
    }

    public int getDefaultBodyIndex() {
        return defaultBodyIndex;
    }

    public int getDefaultLegsIndex() {
        return defaultLegsIndex;
    }

    public int getDefaultArmsIndex() {
        return defaultArmsIndex;
    }

    public int getDefaultHeadIndex() {
        return defaultHeadIndex;
    }

    public float getDefaultSizeScale() {
        return sizeScale;
    }
}
