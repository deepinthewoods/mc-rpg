package ninja.trek.rpg;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import ninja.trek.rpg.client.gui.CharacterCreationScreen;
import ninja.trek.rpg.client.gui.DialogScreen;
import ninja.trek.rpg.client.gui.QuestJournalScreen;
import ninja.trek.rpg.client.render.NpcGeoRenderer;
import ninja.trek.rpg.client.state.ClientRpgState;
import ninja.trek.rpg.entity.NpcEntity;
import ninja.trek.rpg.entity.data.CharacterAppearance;
import ninja.trek.rpg.entity.data.Race;
import ninja.trek.rpg.network.payloads.*;
import ninja.trek.rpg.registry.ModEntities;
import org.lwjgl.glfw.GLFW;

public class McrpgClient implements ClientModInitializer {

	private static KeyMapping openCreationScreenKey;
	private static KeyMapping openJournalKey;

	@Override
	public void onInitializeClient() {
		Mcrpg.LOGGER.info("Initializing mc-rpg client...");

		// Register entity renderer
		EntityRendererRegistry.register(ModEntities.NPC, NpcGeoRenderer::new);

		// Register client packet handlers
		registerClientPacketHandlers();

		// Register keybinding to open character creation screen
		openCreationScreenKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
			"key.mc-rpg.open_creation",
			GLFW.GLFW_KEY_G,
			KeyMapping.Category.MISC
		));

		// Register keybinding to open quest journal
		openJournalKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
			"key.mc-rpg.open_journal",
			GLFW.GLFW_KEY_J,
			KeyMapping.Category.MISC
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (openCreationScreenKey.consumeClick()) {
				if (client.screen == null) {
					client.setScreen(new CharacterCreationScreen());
				}
			}
			while (openJournalKey.consumeClick()) {
				if (client.screen == null) {
					ClientPlayNetworking.send(new RequestJournalPayload());
				}
			}
		});

		Mcrpg.LOGGER.info("mc-rpg client initialized successfully!");
	}

	private void registerClientPacketHandlers() {
		// NPC data sync
		ClientPlayNetworking.registerGlobalReceiver(SyncNpcDataPayload.TYPE, (payload, context) -> {
			context.client().execute(() -> {
				Minecraft client = context.client();
				if (client.level == null) return;

				net.minecraft.world.entity.Entity entity = client.level.getEntity(payload.entityId());
				if (entity instanceof NpcEntity npc) {
					try {
						Race race = Race.valueOf(payload.raceName().toUpperCase());
						npc.setRace(race);
					} catch (IllegalArgumentException ignored) {
					}

					npc.setAppearance(new CharacterAppearance(
						payload.bodyIndex(),
						payload.legsIndex(),
						payload.armsIndex(),
						payload.headIndex()
					));
					npc.setCustomModelPath(payload.customModelPath());
					npc.setCustomTexturePath(payload.customTexturePath());
					npc.setSizeScale(payload.sizeScale());
				}
			});
		});

		// Party sync
		ClientPlayNetworking.registerGlobalReceiver(SyncPartyPayload.TYPE, (payload, context) -> {
			context.client().execute(() -> {
				ClientRpgState.getInstance().updateParty(payload.leaderName(), payload.memberNames());
			});
		});

		// Party invite notification
		ClientPlayNetworking.registerGlobalReceiver(PartyInvitePayload.TYPE, (payload, context) -> {
			context.client().execute(() -> {
				Minecraft client = context.client();
				if (client.player != null) {
					client.player.displayClientMessage(
						net.minecraft.network.chat.Component.literal(
							payload.fromPlayer() + " invited you to their party! Use /party accept or press Accept."),
						false
					);
				}
			});
		});

		// Quest state sync
		ClientPlayNetworking.registerGlobalReceiver(SyncQuestStatePayload.TYPE, (payload, context) -> {
			context.client().execute(() -> {
				ClientRpgState.getInstance().updateQuestStates(payload.entries());
			});
		});

		// Open dialog
		ClientPlayNetworking.registerGlobalReceiver(OpenDialogPayload.TYPE, (payload, context) -> {
			context.client().execute(() -> {
				Minecraft client = context.client();
				client.setScreen(new DialogScreen(
					payload.speakerName(), payload.text(), payload.responses(), payload.questFullId()
				));
			});
		});

		// Dialog update
		ClientPlayNetworking.registerGlobalReceiver(DialogUpdatePayload.TYPE, (payload, context) -> {
			context.client().execute(() -> {
				Minecraft client = context.client();
				if (client.screen instanceof DialogScreen dialogScreen) {
					dialogScreen.updateDialog(payload.speakerName(), payload.text(), payload.responses());
				}
			});
		});

		// Close dialog
		ClientPlayNetworking.registerGlobalReceiver(CloseDialogPayload.TYPE, (payload, context) -> {
			context.client().execute(() -> {
				Minecraft client = context.client();
				if (client.screen instanceof DialogScreen) {
					client.setScreen(null);
				}
			});
		});

		// Journal sync
		ClientPlayNetworking.registerGlobalReceiver(SyncJournalPayload.TYPE, (payload, context) -> {
			context.client().execute(() -> {
				ClientRpgState.getInstance().updateJournal(payload.entries());
				Minecraft client = context.client();
				client.setScreen(new QuestJournalScreen());
			});
		});
	}
}
