package ninja.trek.rpg.dialog;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import ninja.trek.rpg.Mcrpg;
import ninja.trek.rpg.data.*;
import ninja.trek.rpg.data.loader.RpgDataRegistry;
import ninja.trek.rpg.entity.NpcEntity;
import ninja.trek.rpg.network.payloads.CloseDialogPayload;
import ninja.trek.rpg.network.payloads.DialogUpdatePayload;
import ninja.trek.rpg.network.payloads.OpenDialogPayload;
import ninja.trek.rpg.quest.QuestManager;
import ninja.trek.rpg.state.QuestState;
import ninja.trek.rpg.state.RpgWorldState;

import java.util.*;

public class DialogManager {

    private static final Map<UUID, DialogSession> activeSessions = new HashMap<>();

    public static void handleNpcInteraction(ServerPlayer player, String characterId, NpcEntity npc) {
        RpgDataRegistry registry = RpgDataRegistry.getInstance();
        RpgWorldState state = RpgWorldState.get(player.level().getServer());

        // Find an active quest for this character
        QuestData activeQuest = null;
        for (QuestData quest : registry.getAllQuests()) {
            if (quest.characters().contains(characterId) && state.getQuestState(quest.fullId()) == QuestState.ACTIVE) {
                activeQuest = quest;
                break;
            }
        }

        // If no active quest, check for available quests
        if (activeQuest == null) {
            for (QuestData quest : registry.getAllQuests()) {
                if (quest.characters().contains(characterId) && state.getQuestState(quest.fullId()) == QuestState.AVAILABLE) {
                    activeQuest = quest;
                    break;
                }
            }
        }

        if (activeQuest == null || activeQuest.dialogId().isEmpty()) {
            // No quest dialog available for this character
            CharacterData charData = registry.getCharacter(characterId);
            String charName = charData != null ? charData.name() : characterId;
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(charName + " has nothing to say right now."));
            return;
        }

        DialogTreeData dialogTree = registry.getDialog(activeQuest.dialogId());
        if (dialogTree == null) {
            Mcrpg.LOGGER.error("Dialog tree not found: {}", activeQuest.dialogId());
            return;
        }

        // Auto-accept available quest
        if (state.getQuestState(activeQuest.fullId()) == QuestState.AVAILABLE) {
            QuestManager.acceptQuest(player.level().getServer(), activeQuest.fullId());
        }

        // Start dialog session
        DialogSession session = new DialogSession(dialogTree, activeQuest.fullId());
        activeSessions.put(player.getUUID(), session);

        sendDialogNode(player, session);
    }

    public static void handleDialogResponse(ServerPlayer player, int responseIndex) {
        DialogSession session = activeSessions.get(player.getUUID());
        if (session == null) {
            Mcrpg.LOGGER.warn("No active dialog session for player {}", player.getName().getString());
            return;
        }

        List<DialogResponse> visibleResponses = session.getVisibleResponses();
        if (visibleResponses == null || responseIndex < 0 || responseIndex >= visibleResponses.size()) {
            Mcrpg.LOGGER.warn("Invalid response index {} for player {}", responseIndex, player.getName().getString());
            return;
        }

        DialogResponse response = visibleResponses.get(responseIndex);
        String nextNodeId = response.nextNode();

        DialogNode nextNode = session.getTree().nodes().get(nextNodeId);
        if (nextNode == null) {
            Mcrpg.LOGGER.error("Next node not found: {}", nextNodeId);
            endDialog(player);
            return;
        }

        session.setCurrentNodeId(nextNodeId);

        // Apply outcome if present
        if (nextNode.outcome().isPresent()) {
            DialogOutcomeHandler.apply(nextNode.outcome().get(), player, session);
        }

        // If no responses, this is an end node
        if (nextNode.responses().isEmpty()) {
            endDialog(player);
            return;
        }

        sendDialogNode(player, session);
    }

    public static void endDialog(ServerPlayer player) {
        DialogSession session = activeSessions.remove(player.getUUID());
        if (session != null && session.getSelectedBranch() != null) {
            QuestManager.completeQuest(player.level().getServer(), session.getQuestFullId(), session.getSelectedBranch());
        }
        ServerPlayNetworking.send(player, new CloseDialogPayload());
    }

    private static void sendDialogNode(ServerPlayer player, DialogSession session) {
        DialogNode node = session.getTree().nodes().get(session.getCurrentNodeId());
        if (node == null) {
            endDialog(player);
            return;
        }

        // Evaluate conditions for responses
        RpgDataRegistry registry = RpgDataRegistry.getInstance();
        List<DialogResponse> visible = new ArrayList<>();
        List<String> responseTexts = new ArrayList<>();

        for (DialogResponse response : node.responses()) {
            if (response.condition().isPresent()) {
                if (!DialogConditionEvaluator.evaluate(response.condition().get(), player)) {
                    continue;
                }
            }
            visible.add(response);
            responseTexts.add(response.text());
        }

        session.setVisibleResponses(visible);

        // Get speaker name
        CharacterData charData = registry.getCharacter(node.speaker());
        String speakerName = charData != null ? charData.name() : node.speaker();

        // Apply outcome if present on entry node
        if (node.outcome().isPresent()) {
            DialogOutcomeHandler.apply(node.outcome().get(), player, session);
        }

        // Check if this is the start of dialog (open) or continuation (update)
        if (session.getCurrentNodeId().equals(session.getTree().startNode())) {
            ServerPlayNetworking.send(player, new OpenDialogPayload(
                speakerName, node.text(), responseTexts, session.getQuestFullId()
            ));
        } else {
            ServerPlayNetworking.send(player, new DialogUpdatePayload(
                speakerName, node.text(), responseTexts
            ));
        }
    }
}
