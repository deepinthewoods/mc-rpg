package ninja.trek.rpg.quest;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import ninja.trek.rpg.Mcrpg;
import ninja.trek.rpg.data.QuestBranch;
import ninja.trek.rpg.data.QuestData;
import ninja.trek.rpg.data.QuestOutcomes;
import ninja.trek.rpg.data.loader.RpgDataRegistry;
import ninja.trek.rpg.network.payloads.SyncJournalPayload;
import ninja.trek.rpg.network.payloads.SyncQuestStatePayload;
import ninja.trek.rpg.state.QuestState;
import ninja.trek.rpg.state.RpgWorldState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class QuestManager {

    public static void initializeQuestStates(MinecraftServer server) {
        RpgWorldState state = RpgWorldState.get(server);
        RpgDataRegistry registry = RpgDataRegistry.getInstance();

        int initialized = 0;
        for (QuestData quest : registry.getAllQuests()) {
            String fullId = quest.fullId();
            // Skip already-initialized quests
            QuestState current = state.getQuestState(fullId);
            if (current != QuestState.BLOCKED) continue;

            // Only set to BLOCKED if this is first-time init (not in save yet)
            if (!state.getAllQuestStates().containsKey(fullId)) {
                if (RequirementChecker.checkRequirements(quest.requirements(), state)) {
                    state.setQuestState(fullId, QuestState.AVAILABLE);
                } else {
                    state.setQuestState(fullId, QuestState.BLOCKED);
                }
                initialized++;
            }
        }

        Mcrpg.LOGGER.info("Initialized quest states: {} quests processed", initialized);
    }

    public static void reevaluateAvailability(MinecraftServer server) {
        RpgWorldState state = RpgWorldState.get(server);
        RpgDataRegistry registry = RpgDataRegistry.getInstance();

        for (QuestData quest : registry.getAllQuests()) {
            String fullId = quest.fullId();
            if (state.getQuestState(fullId) == QuestState.BLOCKED) {
                if (RequirementChecker.checkRequirements(quest.requirements(), state)) {
                    state.setQuestState(fullId, QuestState.AVAILABLE);
                    Mcrpg.LOGGER.info("Quest {} is now available", fullId);
                }
            }
        }
    }

    public static List<QuestData> getAvailableQuests(MinecraftServer server) {
        RpgWorldState state = RpgWorldState.get(server);
        RpgDataRegistry registry = RpgDataRegistry.getInstance();
        List<QuestData> available = new ArrayList<>();

        for (QuestData quest : registry.getAllQuests()) {
            if (state.getQuestState(quest.fullId()) == QuestState.AVAILABLE) {
                available.add(quest);
            }
        }

        return available;
    }

    public static void acceptQuest(MinecraftServer server, String fullId) {
        RpgWorldState state = RpgWorldState.get(server);
        RpgDataRegistry registry = RpgDataRegistry.getInstance();

        if (state.getQuestState(fullId) != QuestState.AVAILABLE) {
            Mcrpg.LOGGER.warn("Cannot accept quest {}: not available (state={})", fullId, state.getQuestState(fullId));
            return;
        }

        QuestData quest = registry.getQuest(fullId);
        if (quest == null) {
            Mcrpg.LOGGER.error("Cannot accept quest {}: not found in registry", fullId);
            return;
        }

        state.setQuestState(fullId, QuestState.ACTIVE);

        // Start timer
        long currentTick = server.getLevel(net.minecraft.world.level.Level.OVERWORLD).getGameTime();
        state.setQuestTimer(fullId, currentTick);

        // Move characters to quest location
        CharacterManager.moveCharactersForQuest(server, quest);

        Mcrpg.LOGGER.info("Quest {} accepted", fullId);

        syncQuestStateToAll(server);
    }

    public static void completeQuest(MinecraftServer server, String fullId, String branchId) {
        RpgWorldState state = RpgWorldState.get(server);
        RpgDataRegistry registry = RpgDataRegistry.getInstance();

        QuestData quest = registry.getQuest(fullId);
        if (quest == null) {
            Mcrpg.LOGGER.error("Cannot complete quest {}: not found", fullId);
            return;
        }

        QuestBranch branch = null;
        for (QuestBranch b : quest.branches()) {
            if (b.id().equals(branchId)) {
                branch = b;
                break;
            }
        }

        if (branch == null) {
            Mcrpg.LOGGER.error("Cannot complete quest {}: branch {} not found", fullId, branchId);
            return;
        }

        state.setQuestState(fullId, QuestState.COMPLETED);
        state.setCompletedBranch(fullId, branchId);
        state.removeQuestTimer(fullId);

        // Apply branch outcomes
        String source = fullId + "." + branchId;
        OutcomeApplicator.apply(server, branch.outcomes(), source);

        // Return characters home
        CharacterManager.returnCharactersHome(server, quest);

        // Handle triggers (immediately activate)
        for (String triggeredId : branch.outcomes().triggers()) {
            QuestData triggered = registry.getQuest(triggeredId);
            if (triggered != null) {
                state.setQuestState(triggeredId, QuestState.AVAILABLE);
                Mcrpg.LOGGER.info("Quest {} triggered by {}", triggeredId, fullId);
            }
        }

        // Handle blocks
        for (String blockedId : branch.outcomes().blocks()) {
            if (state.getQuestState(blockedId) == QuestState.AVAILABLE) {
                state.setQuestState(blockedId, QuestState.BLOCKED);
                Mcrpg.LOGGER.info("Quest {} blocked by {}", blockedId, fullId);
            }
        }

        // Reevaluate availability
        reevaluateAvailability(server);

        Mcrpg.LOGGER.info("Quest {} completed via branch {}", fullId, branchId);

        syncQuestStateToAll(server);
    }

    public static void failQuest(MinecraftServer server, String fullId) {
        RpgWorldState state = RpgWorldState.get(server);
        RpgDataRegistry registry = RpgDataRegistry.getInstance();

        QuestData quest = registry.getQuest(fullId);
        if (quest == null) return;

        state.setQuestState(fullId, QuestState.FAILED);
        state.removeQuestTimer(fullId);

        CharacterManager.returnCharactersHome(server, quest);

        Mcrpg.LOGGER.info("Quest {} failed", fullId);

        reevaluateAvailability(server);
        syncQuestStateToAll(server);
    }

    public static void sendJournalData(ServerPlayer player) {
        RpgWorldState state = RpgWorldState.get(player.level().getServer());
        RpgDataRegistry registry = RpgDataRegistry.getInstance();

        List<SyncJournalPayload.JournalEntry> entries = new ArrayList<>();
        for (QuestData quest : registry.getAllQuests()) {
            QuestState questState = state.getQuestState(quest.fullId());
            long timer = 0;
            Long timerVal = state.getQuestTimer(quest.fullId());
            if (timerVal != null) {
                timer = timerVal;
            }
            entries.add(new SyncJournalPayload.JournalEntry(
                quest.fullId(),
                quest.summary(),
                quest.factionId(),
                quest.level(),
                questState.name(),
                quest.location(),
                timer
            ));
        }

        ServerPlayNetworking.send(player, new SyncJournalPayload(entries));
    }

    public static void syncQuestStateToAll(MinecraftServer server) {
        RpgWorldState state = RpgWorldState.get(server);
        Map<String, QuestState> allStates = state.getAllQuestStates();

        List<SyncQuestStatePayload.QuestStateEntry> entries = new ArrayList<>();
        allStates.forEach((id, qs) -> entries.add(new SyncQuestStatePayload.QuestStateEntry(id, qs.name())));

        SyncQuestStatePayload payload = new SyncQuestStatePayload(entries);

        for (UUID memberId : state.getParty().getMembers()) {
            ServerPlayer member = server.getPlayerList().getPlayer(memberId);
            if (member != null) {
                ServerPlayNetworking.send(member, payload);
            }
        }
    }
}
