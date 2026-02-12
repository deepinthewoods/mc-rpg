package ninja.trek.rpg.entity.skeleton;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines bone lengths and proportions for a specific race.
 * Used for animation retargeting to adapt animations across different body proportions.
 */
public class SkeletonProfile {

    private final String name;
    private final Map<String, Float> boneLengths;
    private final float totalHeight;
    private final float legLength;

    public static final SkeletonProfile HUMAN = createHuman();
    public static final SkeletonProfile DWARF = createDwarf();
    public static final SkeletonProfile ELF = createElf();
    public static final SkeletonProfile HALFLING = createHalfling();

    public static final SkeletonProfile BASE = HUMAN;

    public SkeletonProfile(String name, Map<String, Float> boneLengths, float totalHeight) {
        this.name = name;
        this.boneLengths = new HashMap<>(boneLengths);
        this.totalHeight = totalHeight;
        this.legLength = boneLengths.getOrDefault(HumanoidBones.LEG_RIGHT, 0.5f)
                + boneLengths.getOrDefault(HumanoidBones.SHIN_RIGHT, 0.45f);
    }

    public float getBoneLength(String boneName) {
        return boneLengths.getOrDefault(boneName, 0.0f);
    }

    public float getBoneLengthRatio(String boneName) {
        float thisLength = getBoneLength(boneName);
        float baseLength = BASE.getBoneLength(boneName);
        if (baseLength == 0.0f) {
            return 1.0f;
        }
        return thisLength / baseLength;
    }

    public float getTotalHeight() { return totalHeight; }
    public float getLegLength() { return legLength; }

    public float getLegLengthRatio() {
        return legLength / BASE.getLegLength();
    }

    public String getName() { return name; }

    private static SkeletonProfile createHuman() {
        Map<String, Float> bones = new HashMap<>();
        bones.put(HumanoidBones.TORSO_UPPER, 0.6f);
        bones.put(HumanoidBones.TORSO_LOWER, 0.4f);
        bones.put(HumanoidBones.HEAD, 0.3f);
        bones.put(HumanoidBones.ARM_RIGHT, 0.4f);
        bones.put(HumanoidBones.FOREARM_RIGHT, 0.35f);
        bones.put(HumanoidBones.ARM_LEFT, 0.4f);
        bones.put(HumanoidBones.FOREARM_LEFT, 0.35f);
        bones.put(HumanoidBones.LEG_RIGHT, 0.5f);
        bones.put(HumanoidBones.SHIN_RIGHT, 0.45f);
        bones.put(HumanoidBones.LEG_LEFT, 0.5f);
        bones.put(HumanoidBones.SHIN_LEFT, 0.45f);
        return new SkeletonProfile("human", bones, 1.8f);
    }

    private static SkeletonProfile createDwarf() {
        Map<String, Float> bones = new HashMap<>();
        bones.put(HumanoidBones.TORSO_UPPER, 0.5f);
        bones.put(HumanoidBones.TORSO_LOWER, 0.35f);
        bones.put(HumanoidBones.HEAD, 0.32f);
        bones.put(HumanoidBones.ARM_RIGHT, 0.35f);
        bones.put(HumanoidBones.FOREARM_RIGHT, 0.3f);
        bones.put(HumanoidBones.ARM_LEFT, 0.35f);
        bones.put(HumanoidBones.FOREARM_LEFT, 0.3f);
        bones.put(HumanoidBones.LEG_RIGHT, 0.35f);
        bones.put(HumanoidBones.SHIN_RIGHT, 0.3f);
        bones.put(HumanoidBones.LEG_LEFT, 0.35f);
        bones.put(HumanoidBones.SHIN_LEFT, 0.3f);
        return new SkeletonProfile("dwarf", bones, 1.3f);
    }

    private static SkeletonProfile createElf() {
        Map<String, Float> bones = new HashMap<>();
        bones.put(HumanoidBones.TORSO_UPPER, 0.65f);
        bones.put(HumanoidBones.TORSO_LOWER, 0.45f);
        bones.put(HumanoidBones.HEAD, 0.28f);
        bones.put(HumanoidBones.ARM_RIGHT, 0.45f);
        bones.put(HumanoidBones.FOREARM_RIGHT, 0.4f);
        bones.put(HumanoidBones.ARM_LEFT, 0.45f);
        bones.put(HumanoidBones.FOREARM_LEFT, 0.4f);
        bones.put(HumanoidBones.LEG_RIGHT, 0.6f);
        bones.put(HumanoidBones.SHIN_RIGHT, 0.55f);
        bones.put(HumanoidBones.LEG_LEFT, 0.6f);
        bones.put(HumanoidBones.SHIN_LEFT, 0.55f);
        return new SkeletonProfile("elf", bones, 2.0f);
    }

    private static SkeletonProfile createHalfling() {
        Map<String, Float> bones = new HashMap<>();
        bones.put(HumanoidBones.TORSO_UPPER, 0.4f);
        bones.put(HumanoidBones.TORSO_LOWER, 0.3f);
        bones.put(HumanoidBones.HEAD, 0.3f);
        bones.put(HumanoidBones.ARM_RIGHT, 0.3f);
        bones.put(HumanoidBones.FOREARM_RIGHT, 0.25f);
        bones.put(HumanoidBones.ARM_LEFT, 0.3f);
        bones.put(HumanoidBones.FOREARM_LEFT, 0.25f);
        bones.put(HumanoidBones.LEG_RIGHT, 0.3f);
        bones.put(HumanoidBones.SHIN_RIGHT, 0.25f);
        bones.put(HumanoidBones.LEG_LEFT, 0.3f);
        bones.put(HumanoidBones.SHIN_LEFT, 0.25f);
        return new SkeletonProfile("halfling", bones, 1.0f);
    }

    public static SkeletonProfile getByRaceName(String raceName) {
        return switch (raceName.toLowerCase()) {
            case "human" -> HUMAN;
            case "dwarf" -> DWARF;
            case "elf" -> ELF;
            case "halfling" -> HALFLING;
            default -> HUMAN;
        };
    }
}
