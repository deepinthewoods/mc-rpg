package ninja.trek.rpg.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import ninja.trek.rpg.Mcrpg;
import ninja.trek.rpg.network.payloads.CreateNpcPayload;
import ninja.trek.rpg.network.payloads.SyncNpcDataPayload;

/**
 * Registers all custom network packets (payloads) for the mod.
 */
public class ModNetworking {

    public static void initialize() {
        Mcrpg.LOGGER.info("Registering network payloads...");

        // Server-to-Client payloads
        PayloadTypeRegistry.playS2C().register(
            SyncNpcDataPayload.TYPE,
            SyncNpcDataPayload.STREAM_CODEC
        );

        // Client-to-Server payloads
        PayloadTypeRegistry.playC2S().register(
            CreateNpcPayload.TYPE,
            CreateNpcPayload.STREAM_CODEC
        );

        Mcrpg.LOGGER.info("Network payloads registered successfully!");
    }
}
