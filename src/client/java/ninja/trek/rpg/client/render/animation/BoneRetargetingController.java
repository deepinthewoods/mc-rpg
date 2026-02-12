package ninja.trek.rpg.client.render.animation;

import ninja.trek.rpg.entity.skeleton.SkeletonProfile;
import org.joml.Vector3f;

/**
 * Handles bone retargeting for animations across different race skeletons.
 * Adapts animations created for the base skeleton to work with different proportions.
 */
public class BoneRetargetingController {

    private final SkeletonProfile sourceProfile;

    public BoneRetargetingController(SkeletonProfile sourceProfile) {
        this.sourceProfile = sourceProfile;
    }

    public Vector3f retargetPosition(String boneName, Vector3f originalPosition,
                                    SkeletonProfile targetProfile, float sizeScale) {
        float lengthRatio = targetProfile.getBoneLengthRatio(boneName);
        float totalScale = lengthRatio * sizeScale;

        return new Vector3f(
            originalPosition.x * totalScale,
            originalPosition.y * totalScale,
            originalPosition.z * totalScale
        );
    }

    public float calculateAnimationSpeed(SkeletonProfile targetProfile, float sizeScale, boolean isLocomotion) {
        if (!isLocomotion) {
            return 1.0f;
        }
        float legLengthRatio = targetProfile.getLegLengthRatio();
        return legLengthRatio * sizeScale;
    }

    public static boolean isLegBone(String boneName) {
        return boneName.contains("leg") || boneName.contains("shin") || boneName.contains("foot");
    }

    public static boolean isArmBone(String boneName) {
        return boneName.contains("arm") || boneName.contains("hand");
    }
}
