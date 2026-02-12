package ninja.trek.rpg.registry;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import ninja.trek.rpg.Mcrpg;
import ninja.trek.rpg.entity.NpcEntity;

/**
 * Registry for custom entity types.
 */
public class ModEntities {

    public static final EntityType<NpcEntity> NPC = register(
        "npc",
        EntityType.Builder.of(NpcEntity::new, MobCategory.CREATURE)
            .sized(0.6f, 1.8f)
    );

    private static <T extends Entity> EntityType<T> register(
        String name,
        EntityType.Builder<T> builder
    ) {
        ResourceKey<EntityType<?>> entityKey = ResourceKey.create(
            Registries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(Mcrpg.MOD_ID, name)
        );
        EntityType<T> entityType = builder.build(entityKey);
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, entityKey, entityType);
    }

    public static void initialize() {
        Mcrpg.LOGGER.info("Registering entities for " + Mcrpg.MOD_ID);
        FabricDefaultAttributeRegistry.register(NPC, NpcEntity.createAttributes());
    }
}
