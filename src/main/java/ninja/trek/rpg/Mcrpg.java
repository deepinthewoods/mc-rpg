package ninja.trek.rpg;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.phys.Vec3;
import ninja.trek.rpg.entity.NpcEntity;
import ninja.trek.rpg.entity.data.CharacterAppearance;
import ninja.trek.rpg.entity.data.Race;
import ninja.trek.rpg.network.ModNetworking;
import ninja.trek.rpg.network.payloads.CreateNpcPayload;
import ninja.trek.rpg.registry.ModEntities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Mcrpg implements ModInitializer {
	public static final String MOD_ID = "mc-rpg";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing mc-rpg...");

		// Register entities
		ModEntities.initialize();

		// Register networking
		ModNetworking.initialize();

		// Register server packet handlers
		registerServerPacketHandlers();

		// Register commands
		registerCommands();

		LOGGER.info("mc-rpg initialized successfully!");
	}

	private void registerServerPacketHandlers() {
		ServerPlayNetworking.registerGlobalReceiver(CreateNpcPayload.TYPE, (payload, context) -> {
			context.server().execute(() -> {
				ServerLevel level = context.player().level();
				Vec3 pos = context.player().position();

				NpcEntity npc = new NpcEntity(ModEntities.NPC, level);
				npc.snapTo(pos.x + 2, pos.y, pos.z, 0.0f, 0.0f);
				npc.initializeNpc(payload.race(), payload.appearance());
				npc.setCustomName(Component.literal(payload.name()));

				level.addFreshEntity(npc);
				LOGGER.info("Created NPC '{}' with race {} at {}", payload.name(), payload.race().getName(), pos);
			});
		});
	}

	private void registerCommands() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(Commands.literal("npc")
				.requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
				.then(Commands.literal("create")
					.then(Commands.argument("race", StringArgumentType.word())
						.suggests((ctx, builder) -> {
							for (Race race : Race.values()) {
								builder.suggest(race.getName());
							}
							return builder.buildFuture();
						})
						.executes(ctx -> {
							String raceName = StringArgumentType.getString(ctx, "race");
							return spawnNpc(ctx.getSource(), raceName, "NPC");
						})
						.then(Commands.argument("name", StringArgumentType.greedyString())
							.executes(ctx -> {
								String raceName = StringArgumentType.getString(ctx, "race");
								String name = StringArgumentType.getString(ctx, "name");
								return spawnNpc(ctx.getSource(), raceName, name);
							})
						)
					)
				)
			);
		});
	}

	private int spawnNpc(net.minecraft.commands.CommandSourceStack source, String raceName, String name) {
		Race race;
		try {
			race = Race.valueOf(raceName.toUpperCase());
		} catch (IllegalArgumentException e) {
			source.sendFailure(Component.literal("Unknown race: " + raceName));
			return 0;
		}

		ServerLevel level = source.getLevel();
		Vec3 pos = source.getPosition();

		NpcEntity npc = new NpcEntity(ModEntities.NPC, level);
		npc.snapTo(pos.x, pos.y, pos.z, 0.0f, 0.0f);
		npc.initializeNpc(race, CharacterAppearance.fromRace(race));
		npc.setCustomName(Component.literal(name));

		level.addFreshEntity(npc);
		source.sendSuccess(() -> Component.literal("Created NPC '" + name + "' (" + race.getName() + ")"), true);
		return 1;
	}
}
