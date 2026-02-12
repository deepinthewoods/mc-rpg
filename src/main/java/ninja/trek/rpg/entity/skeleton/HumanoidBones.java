package ninja.trek.rpg.entity.skeleton;

/**
 * Standard bone names for humanoid characters in the GeckoLib skeleton.
 * All character models must use these exact bone names for proper animation retargeting.
 */
public final class HumanoidBones {

    // Root bones
    public static final String ROOT = "root";
    public static final String BODY = "body";

    // Head
    public static final String HEAD = "head";
    public static final String HEAD_TOP = "head_top";
    public static final String JAW = "jaw";

    // Torso
    public static final String TORSO_UPPER = "torso_upper";
    public static final String TORSO_LOWER = "torso_lower";

    // Right arm
    public static final String ARM_RIGHT = "arm_right";
    public static final String FOREARM_RIGHT = "forearm_right";
    public static final String HAND_RIGHT = "hand_right";
    public static final String SHOULDER_ARMOR_RIGHT = "shoulder_armor_right";

    // Left arm
    public static final String ARM_LEFT = "arm_left";
    public static final String FOREARM_LEFT = "forearm_left";
    public static final String HAND_LEFT = "hand_left";
    public static final String SHOULDER_ARMOR_LEFT = "shoulder_armor_left";

    // Right leg
    public static final String LEG_RIGHT = "leg_right";
    public static final String SHIN_RIGHT = "shin_right";
    public static final String FOOT_RIGHT = "foot_right";
    public static final String THIGH_ARMOR_RIGHT = "thigh_armor_right";

    // Left leg
    public static final String LEG_LEFT = "leg_left";
    public static final String SHIN_LEFT = "shin_left";
    public static final String FOOT_LEFT = "foot_left";
    public static final String THIGH_ARMOR_LEFT = "thigh_armor_left";

    public static final String[] ALL_BONES = {
        ROOT, BODY,
        HEAD, HEAD_TOP, JAW,
        TORSO_UPPER, TORSO_LOWER,
        ARM_RIGHT, FOREARM_RIGHT, HAND_RIGHT, SHOULDER_ARMOR_RIGHT,
        ARM_LEFT, FOREARM_LEFT, HAND_LEFT, SHOULDER_ARMOR_LEFT,
        LEG_RIGHT, SHIN_RIGHT, FOOT_RIGHT, THIGH_ARMOR_RIGHT,
        LEG_LEFT, SHIN_LEFT, FOOT_LEFT, THIGH_ARMOR_LEFT
    };

    private HumanoidBones() {
    }
}
