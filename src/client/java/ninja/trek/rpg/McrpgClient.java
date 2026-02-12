package ninja.trek.rpg;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import ninja.trek.rpg.client.gui.CharacterCreationScreen;
import ninja.trek.rpg.client.render.NpcGeoRenderer;
import ninja.trek.rpg.entity.NpcEntity;
import ninja.trek.rpg.entity.data.CharacterAppearance;
import ninja.trek.rpg.entity.data.Race;
import ninja.trek.rpg.network.payloads.SyncNpcDataPayload;
import ninja.trek.rpg.registry.ModEntities;
import org.lwjgl.glfw.GLFW;

public class McrpgClient implements ClientModInitializer {

	private static KeyMapping openCreationScreenKey;

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

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (openCreationScreenKey.consumeClick()) {
				if (client.screen == null) {
					client.setScreen(new CharacterCreationScreen());
				}
			}
		});

		Mcrpg.LOGGER.info("mc-rpg client initialized successfully!");
	}

	private void registerClientPacketHandlers() {
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
	}
}
