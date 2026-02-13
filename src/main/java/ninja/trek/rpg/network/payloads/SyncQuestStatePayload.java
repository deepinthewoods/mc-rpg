package ninja.trek.rpg.network.payloads;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import ninja.trek.rpg.Mcrpg;

import java.util.List;

public record SyncQuestStatePayload(List<QuestStateEntry> entries) implements CustomPacketPayload {

    public record QuestStateEntry(String questId, String state) {}

    public static final Type<SyncQuestStatePayload> TYPE =
        new Type<>(Identifier.fromNamespaceAndPath(Mcrpg.MOD_ID, "sync_quest_state"));

    private static final StreamCodec<RegistryFriendlyByteBuf, QuestStateEntry> ENTRY_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, QuestStateEntry::questId,
            ByteBufCodecs.STRING_UTF8, QuestStateEntry::state,
            QuestStateEntry::new
        );

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncQuestStatePayload> STREAM_CODEC =
        StreamCodec.composite(
            ENTRY_CODEC.apply(ByteBufCodecs.list()), SyncQuestStatePayload::entries,
            SyncQuestStatePayload::new
        );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
