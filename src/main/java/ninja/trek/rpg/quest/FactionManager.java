package ninja.trek.rpg.quest;

import net.minecraft.server.MinecraftServer;
import ninja.trek.rpg.Mcrpg;
import ninja.trek.rpg.data.FactionData;
import ninja.trek.rpg.data.loader.RpgDataRegistry;
import ninja.trek.rpg.state.RpgWorldState;

import java.util.Map;

public class FactionManager {

    public static void initializeFactionStats(MinecraftServer server) {
        RpgWorldState state = RpgWorldState.get(server);
        RpgDataRegistry registry = RpgDataRegistry.getInstance();

        for (FactionData faction : registry.getAllFactions()) {
            for (Map.Entry<String, Integer> entry : faction.stats().entrySet()) {
                // Only set if not already initialized (preserve existing save data)
                if (state.getFactionStat(faction.id(), entry.getKey()) == 0 && entry.getValue() != 0) {
                    state.setFactionStat(faction.id(), entry.getKey(), entry.getValue());
                }
            }
        }

        Mcrpg.LOGGER.info("Initialized faction stats for {} factions", registry.getAllFactions().size());
    }

    public static boolean isFactionUnlocked(MinecraftServer server, String factionId) {
        FactionData faction = RpgDataRegistry.getInstance().getFaction(factionId);
        return faction != null && faction.unlocked();
    }

    public static int getFactionStat(MinecraftServer server, String factionId, String stat) {
        return RpgWorldState.get(server).getFactionStat(factionId, stat);
    }
}
