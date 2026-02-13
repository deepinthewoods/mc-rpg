package ninja.trek.rpg.world;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import ninja.trek.rpg.Mcrpg;

public class ModChunkGenerators {

    public static void initialize() {
        Mcrpg.LOGGER.info("Registering chunk generators for " + Mcrpg.MOD_ID);
        Registry.register(
            BuiltInRegistries.CHUNK_GENERATOR,
            Identifier.fromNamespaceAndPath(Mcrpg.MOD_ID, "rpg_terrain"),
            RpgChunkGenerator.CODEC
        );
    }
}
