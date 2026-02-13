package ninja.trek.rpg.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.level.Level;
import ninja.trek.rpg.Mcrpg;

import java.util.Set;

public class DimensionHelper {

    public static void teleportToRpgWorld(ServerPlayer player) {
        ServerLevel rpgWorld = player.level().getServer().getLevel(ModDimensions.RPG_WORLD_KEY);
        if (rpgWorld == null) {
            Mcrpg.LOGGER.error("RPG world not found!");
            return;
        }

        BlockPos spawnPos = findSafeSpawnPos(rpgWorld, player.blockPosition());
        player.teleportTo(rpgWorld, spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5,
            Set.of(), player.getYRot(), player.getXRot(), false);
    }

    public static void teleportToOverworld(ServerPlayer player) {
        ServerLevel overworld = player.level().getServer().getLevel(Level.OVERWORLD);
        if (overworld == null) {
            Mcrpg.LOGGER.error("Overworld not found!");
            return;
        }

        BlockPos spawnPos = new BlockPos(0, 64, 0);
        player.teleportTo(overworld, spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5,
            Set.of(), player.getYRot(), player.getXRot(), false);
    }

    public static boolean isInRpgWorld(ServerPlayer player) {
        return player.level().dimension().equals(ModDimensions.RPG_WORLD_KEY);
    }

    private static BlockPos findSafeSpawnPos(ServerLevel level, BlockPos preferred) {
        // Find the surface at x=0, z=0 for now (or preferred coords)
        int x = preferred.getX();
        int z = preferred.getZ();
        int y = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, x, z);
        return new BlockPos(x, y, z);
    }
}
