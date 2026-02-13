package ninja.trek.rpg.quest;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import ninja.trek.rpg.Mcrpg;
import ninja.trek.rpg.data.AutoResolveConfig;
import ninja.trek.rpg.data.QuestBranch;
import ninja.trek.rpg.data.QuestData;
import ninja.trek.rpg.data.loader.RpgDataRegistry;
import ninja.trek.rpg.state.QuestState;
import ninja.trek.rpg.state.RpgWorldState;

import java.util.*;

public class AutoResolveHandler {

    private static final int CHECK_INTERVAL = 100; // 5 seconds
    private static final int TICKS_PER_LEVEL = 24000; // 1 MC day per quest level

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(AutoResolveHandler::onServerTick);
    }

    private static void onServerTick(MinecraftServer server) {
        long gametime = server.getLevel(Level.OVERWORLD).getGameTime();
        if (gametime % CHECK_INTERVAL != 0) return;

        RpgWorldState state = RpgWorldState.get(server);
        Map<String, Long> timers = new HashMap<>(state.getAllQuestTimers());

        for (Map.Entry<String, Long> entry : timers.entrySet()) {
            String fullId = entry.getKey();
            long activationTick = entry.getValue();

            if (state.getQuestState(fullId) != QuestState.ACTIVE) continue;

            QuestData quest = RpgDataRegistry.getInstance().getQuest(fullId);
            if (quest == null) continue;

            long timerDuration = (long) quest.level() * TICKS_PER_LEVEL;
            if (gametime - activationTick >= timerDuration) {
                autoResolve(server, quest);
            }
        }
    }

    private static void autoResolve(MinecraftServer server, QuestData quest) {
        if (quest.autoResolve().isEmpty()) {
            Mcrpg.LOGGER.info("Quest {} has no auto-resolve config, failing", quest.fullId());
            QuestManager.failQuest(server, quest.fullId());
            return;
        }

        AutoResolveConfig config = quest.autoResolve().get();

        switch (config.type()) {
            case FAIL -> {
                Mcrpg.LOGGER.info("Auto-resolving quest {} as FAIL", quest.fullId());
                QuestManager.failQuest(server, quest.fullId());
            }
            case PREDETERMINED -> {
                String branchId = config.fallback().orElse(null);
                if (branchId == null && !quest.branches().isEmpty()) {
                    branchId = quest.branches().getFirst().id();
                }
                if (branchId != null) {
                    Mcrpg.LOGGER.info("Auto-resolving quest {} with predetermined branch: {}", quest.fullId(), branchId);
                    QuestManager.completeQuest(server, quest.fullId(), branchId);
                } else {
                    QuestManager.failQuest(server, quest.fullId());
                }
            }
            case RANDOM -> {
                if (!quest.branches().isEmpty()) {
                    Random rand = new Random();
                    QuestBranch branch = quest.branches().get(rand.nextInt(quest.branches().size()));
                    Mcrpg.LOGGER.info("Auto-resolving quest {} with random branch: {}", quest.fullId(), branch.id());
                    QuestManager.completeQuest(server, quest.fullId(), branch.id());
                } else {
                    QuestManager.failQuest(server, quest.fullId());
                }
            }
            case WEIGHTED_RANDOM -> {
                String selectedBranch = selectWeightedRandom(config.weights(), quest);
                if (selectedBranch != null) {
                    Mcrpg.LOGGER.info("Auto-resolving quest {} with weighted random branch: {}", quest.fullId(), selectedBranch);
                    QuestManager.completeQuest(server, quest.fullId(), selectedBranch);
                } else {
                    // Fallback
                    String fallback = config.fallback().orElse(null);
                    if (fallback != null) {
                        QuestManager.completeQuest(server, quest.fullId(), fallback);
                    } else {
                        QuestManager.failQuest(server, quest.fullId());
                    }
                }
            }
        }
    }

    private static String selectWeightedRandom(Map<String, Integer> weights, QuestData quest) {
        if (weights.isEmpty()) return null;

        int totalWeight = weights.values().stream().mapToInt(Integer::intValue).sum();
        if (totalWeight <= 0) return null;

        Random rand = new Random();
        int roll = rand.nextInt(totalWeight);
        int cumulative = 0;

        for (Map.Entry<String, Integer> entry : weights.entrySet()) {
            cumulative += entry.getValue();
            if (roll < cumulative) {
                return entry.getKey();
            }
        }

        return weights.keySet().iterator().next();
    }
}
