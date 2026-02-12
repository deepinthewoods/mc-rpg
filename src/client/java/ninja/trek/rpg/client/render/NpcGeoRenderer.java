package ninja.trek.rpg.client.render;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import ninja.trek.rpg.client.model.NpcGeoModel;
import ninja.trek.rpg.entity.NpcEntity;
import ninja.trek.rpg.entity.skeleton.SkeletonProfile;
import ninja.trek.rpg.client.render.animation.BoneRetargetingController;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.base.RenderPassInfo;

/**
 * GeckoLib renderer for NpcEntity.
 * Handles rendering of NPC models with proper scaling and bone retargeting.
 */
public class NpcGeoRenderer extends GeoEntityRenderer<NpcEntity, NpcGeoRenderState> {

    private final BoneRetargetingController retargeting = new BoneRetargetingController(SkeletonProfile.BASE);

    public NpcGeoRenderer(EntityRendererProvider.Context context) {
        super(context, new NpcGeoModel());
        this.shadowRadius = 0.5f;
    }

    @Override
    public NpcGeoRenderState createRenderState(NpcEntity entity, Void relatedObject) {
        return new NpcGeoRenderState();
    }

    @Override
    @SuppressWarnings("unchecked")
    public GeoModel<NpcEntity> getGeoModel() {
        return (GeoModel<NpcEntity>) super.getGeoModel();
    }

    @Override
    public void captureDefaultRenderState(NpcEntity entity, Void relatedObject, NpcGeoRenderState state, float partialTick) {
        super.captureDefaultRenderState(entity, relatedObject, state, partialTick);

        // Copy entity data to render state
        state.race = entity.getRace();
        state.appearance = entity.getAppearance();
        state.layerConfiguration = entity.getLayerConfiguration();
        state.sizeScale = entity.getSizeScale();
        state.customModelPath = entity.getCustomModelPath();
        state.customTexturePath = entity.getCustomTexturePath();

        // Animation data
        state.isMoving = entity.walkAnimation.isMoving();
        state.isSprinting = entity.isSprinting();
        state.isAttacking = entity.isAttacking();

        // Held items
        state.mainHandStack = entity.getMainHandItem();
        state.offHandStack = entity.getOffhandItem();
    }

    @Override
    public void scaleModelForRender(RenderPassInfo<NpcGeoRenderState> renderPassInfo, float widthScale, float heightScale) {
        super.scaleModelForRender(renderPassInfo, widthScale, heightScale);

        float scale = renderPassInfo.renderState().sizeScale;
        renderPassInfo.poseStack().scale(scale, scale, scale);
    }

    // TODO: Bone retargeting is disabled pending GeckoLib 5.4+ GeoBone API update.
    // The old getModelPosition(), getName(), updatePosition(), getChildBones() methods
    // have been replaced with name(), children(), pivotX/Y/Z() etc.
}
