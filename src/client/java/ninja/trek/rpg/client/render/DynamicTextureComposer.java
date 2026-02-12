package ninja.trek.rpg.client.render;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import ninja.trek.rpg.Mcrpg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Dynamic texture composer for the GeckoLib animation system.
 * Blends skin and clothing textures at runtime with caching for performance.
 */
public class DynamicTextureComposer {
    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicTextureComposer.class);

    private static DynamicTextureComposer instance;

    private static final int MAX_CACHE_SIZE = 256;
    private static final int CACHE_EXPIRE_MINUTES = 10;

    private final Cache<TextureCompositionKey, Identifier> textureCache;
    private int textureIdCounter = 0;

    private DynamicTextureComposer() {
        this.textureCache = CacheBuilder.newBuilder()
            .maximumSize(MAX_CACHE_SIZE)
            .expireAfterAccess(CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES)
            .removalListener(notification -> {
                if (notification.getValue() instanceof Identifier id) {
                    cleanupTexture(id);
                }
            })
            .build();
    }

    public static DynamicTextureComposer getInstance() {
        if (instance == null) {
            instance = new DynamicTextureComposer();
        }
        return instance;
    }

    public Identifier composeTexture(Identifier baseTexture, List<Identifier> clothingLayers) {
        TextureCompositionKey key = new TextureCompositionKey(baseTexture, clothingLayers);

        Identifier cached = textureCache.getIfPresent(key);
        if (cached != null) {
            return cached;
        }

        Identifier composed = composeTextureInternal(baseTexture, clothingLayers);
        textureCache.put(key, composed);
        return composed;
    }

    private Identifier composeTextureInternal(Identifier baseTexture, List<Identifier> clothingLayers) {
        Minecraft client = Minecraft.getInstance();
        ResourceManager resourceManager = client.getResourceManager();
        TextureManager textureManager = client.getTextureManager();

        try {
            NativeImage baseImage = loadTextureImage(resourceManager, baseTexture);
            if (baseImage == null) {
                LOGGER.warn("Failed to load base texture: {}", baseTexture);
                return baseTexture;
            }

            NativeImage compositeImage = new NativeImage(
                baseImage.getWidth(),
                baseImage.getHeight(),
                true
            );
            compositeImage.copyFrom(baseImage);
            baseImage.close();

            for (Identifier layerTexture : clothingLayers) {
                NativeImage layerImage = loadTextureImage(resourceManager, layerTexture);
                if (layerImage != null) {
                    blendLayer(compositeImage, layerImage);
                    layerImage.close();
                } else {
                    LOGGER.warn("Failed to load layer texture: {}", layerTexture);
                }
            }

            Identifier composedId = generateComposedTextureId();
            DynamicTexture texture = new DynamicTexture(() -> "mc-rpg-composed", compositeImage);
            textureManager.register(composedId, texture);

            return composedId;

        } catch (Exception e) {
            LOGGER.error("Error composing texture", e);
            return baseTexture;
        }
    }

    private NativeImage loadTextureImage(ResourceManager resourceManager, Identifier textureId) {
        try {
            InputStream stream = resourceManager.getResource(textureId)
                .orElseThrow()
                .open();

            return NativeImage.read(stream);

        } catch (IOException e) {
            LOGGER.warn("Failed to load texture image: {}", textureId, e);
            return null;
        }
    }

    private void blendLayer(NativeImage base, NativeImage layer) {
        int width = Math.min(base.getWidth(), layer.getWidth());
        int height = Math.min(base.getHeight(), layer.getHeight());

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int layerColor = layer.getPixel(x, y);
                int layerAlpha = (layerColor >> 24) & 0xFF;

                if (layerAlpha == 0) {
                    continue;
                }

                int baseColor = base.getPixel(x, y);

                if (layerAlpha == 255) {
                    base.setPixel(x, y, layerColor);
                } else {
                    int blended = alphaBlend(baseColor, layerColor);
                    base.setPixel(x, y, blended);
                }
            }
        }
    }

    private int alphaBlend(int bottom, int top) {
        int topA = (top >> 24) & 0xFF;
        int topR = (top >> 16) & 0xFF;
        int topG = (top >> 8) & 0xFF;
        int topB = top & 0xFF;

        int bottomR = (bottom >> 16) & 0xFF;
        int bottomG = (bottom >> 8) & 0xFF;
        int bottomB = bottom & 0xFF;
        int bottomA = (bottom >> 24) & 0xFF;

        float alpha = topA / 255.0f;
        float invAlpha = 1.0f - alpha;

        int outR = Math.min(255, Math.max(0, (int) (topR * alpha + bottomR * invAlpha)));
        int outG = Math.min(255, Math.max(0, (int) (topG * alpha + bottomG * invAlpha)));
        int outB = Math.min(255, Math.max(0, (int) (topB * alpha + bottomB * invAlpha)));
        int outA = Math.min(255, Math.max(0, (int) (topA + bottomA * invAlpha)));

        return (outA << 24) | (outR << 16) | (outG << 8) | outB;
    }

    private Identifier generateComposedTextureId() {
        return Identifier.fromNamespaceAndPath(Mcrpg.MOD_ID,
            "dynamic/composed_texture_" + (textureIdCounter++));
    }

    private void cleanupTexture(Identifier textureId) {
        try {
            Minecraft client = Minecraft.getInstance();
            if (client != null) {
                client.getTextureManager().release(textureId);
            }
        } catch (Exception e) {
            LOGGER.warn("Error cleaning up texture: {}", textureId, e);
        }
    }

    public void invalidateCache() {
        textureCache.invalidateAll();
    }

    private static class TextureCompositionKey {
        private final Identifier baseTexture;
        private final List<Identifier> layers;
        private final int hashCode;

        public TextureCompositionKey(Identifier baseTexture, List<Identifier> layers) {
            this.baseTexture = baseTexture;
            this.layers = new ArrayList<>(layers);
            this.hashCode = Objects.hash(baseTexture, layers);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof TextureCompositionKey other)) return false;
            return Objects.equals(baseTexture, other.baseTexture)
                && Objects.equals(layers, other.layers);
        }

        @Override
        public int hashCode() {
            return hashCode;
        }
    }
}
