package ninja.trek.rpg.world;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import ninja.trek.rpg.Mcrpg;

public class ModDimensions {

    public static final ResourceKey<Level> RPG_WORLD_KEY =
        ResourceKey.create(Registries.DIMENSION, Identifier.fromNamespaceAndPath(Mcrpg.MOD_ID, "rpg_world"));

    public static final ResourceKey<DimensionType> RPG_WORLD_TYPE_KEY =
        ResourceKey.create(Registries.DIMENSION_TYPE, Identifier.fromNamespaceAndPath(Mcrpg.MOD_ID, "rpg_world"));

    public static void initialize() {
        Mcrpg.LOGGER.info("Registering dimensions for " + Mcrpg.MOD_ID);
    }
}
