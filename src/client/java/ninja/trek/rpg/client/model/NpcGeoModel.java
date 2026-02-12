package ninja.trek.rpg.client.model;

import net.minecraft.resources.Identifier;
import ninja.trek.rpg.Mcrpg;
import ninja.trek.rpg.client.render.DynamicTextureComposer;
import ninja.trek.rpg.client.render.NpcGeoRenderState;
import ninja.trek.rpg.entity.NpcEntity;
import ninja.trek.rpg.entity.layer.LayerConfiguration;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

import java.util.ArrayList;
import java.util.List;

/**
 * GeckoLib model for NpcEntity.
 * Handles dynamic model, texture, and animation resource loading based on race and configuration.
 * Supports custom model/texture path overrides.
 */
public class NpcGeoModel extends GeoModel<NpcEntity> {

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        if (renderState instanceof NpcGeoRenderState state) {
            // Check for custom model path first
            if (state.customModelPath != null && !state.customModelPath.isEmpty()) {
                return Identifier.parse(state.customModelPath);
            }

            // Fall back to race-based model
            String raceName = state.race.getName().toLowerCase();
            return Identifier.fromNamespaceAndPath(Mcrpg.MOD_ID,
                "geckolib/models/entity/character/" + raceName + "/" + raceName + "_body");
        }
        return Identifier.fromNamespaceAndPath(Mcrpg.MOD_ID,
            "geckolib/models/entity/character/human/human_body");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        if (renderState instanceof NpcGeoRenderState state) {
            // Check for custom texture path first
            if (state.customTexturePath != null && !state.customTexturePath.isEmpty()) {
                return Identifier.parse(state.customTexturePath);
            }

            // Fall back to race-based texture with dynamic compositing
            String raceName = state.race.getName().toLowerCase();
            LayerConfiguration config = state.layerConfiguration;

            String skinTexture = config != null ? config.getSkinTexture() : "default";
            Identifier baseTexture = Identifier.fromNamespaceAndPath(Mcrpg.MOD_ID,
                "textures/entity/character/base/" + raceName + "_" + skinTexture + ".png");

            if (config != null) {
                List<Identifier> clothingLayers = new ArrayList<>();
                String clothingTexture = config.getClothingTexture();
                if (clothingTexture != null && !clothingTexture.equals("default")) {
                    clothingLayers.add(Identifier.fromNamespaceAndPath(Mcrpg.MOD_ID,
                        "textures/entity/character/clothing/" + clothingTexture + ".png"));
                }

                if (!clothingLayers.isEmpty()) {
                    return DynamicTextureComposer.getInstance().composeTexture(baseTexture, clothingLayers);
                }
            }

            return baseTexture;
        }

        return Identifier.fromNamespaceAndPath(Mcrpg.MOD_ID,
            "textures/entity/character/base/human_default.png");
    }

    @Override
    public Identifier getAnimationResource(NpcEntity entity) {
        return Identifier.fromNamespaceAndPath(Mcrpg.MOD_ID,
            "geckolib/animations/entity/character/locomotion");
    }

    // TODO: Body part variants and visibility rules require GeckoLib 5.4+ bone visibility API
    // The old setHidden() method no longer exists on GeoBone.
    // These features will need to be reimplemented when a visibility mechanism is available.
}
