package ninja.trek.rpg.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import ninja.trek.rpg.Mcrpg;
import ninja.trek.rpg.network.payloads.*;

public class ModNetworking {

    public static void initialize() {
        Mcrpg.LOGGER.info("Registering network payloads...");

        // Server-to-Client payloads
        PayloadTypeRegistry.playS2C().register(SyncNpcDataPayload.TYPE, SyncNpcDataPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(SyncPartyPayload.TYPE, SyncPartyPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(PartyInvitePayload.TYPE, PartyInvitePayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(SyncQuestStatePayload.TYPE, SyncQuestStatePayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(OpenDialogPayload.TYPE, OpenDialogPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(DialogUpdatePayload.TYPE, DialogUpdatePayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(CloseDialogPayload.TYPE, CloseDialogPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(SyncJournalPayload.TYPE, SyncJournalPayload.STREAM_CODEC);

        // Client-to-Server payloads
        PayloadTypeRegistry.playC2S().register(CreateNpcPayload.TYPE, CreateNpcPayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(PartyActionPayload.TYPE, PartyActionPayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(QuestActionPayload.TYPE, QuestActionPayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(DialogResponsePayload.TYPE, DialogResponsePayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(RequestJournalPayload.TYPE, RequestJournalPayload.STREAM_CODEC);

        Mcrpg.LOGGER.info("Network payloads registered successfully!");
    }
}
