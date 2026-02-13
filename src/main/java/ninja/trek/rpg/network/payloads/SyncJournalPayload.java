package ninja.trek.rpg.network.payloads;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import ninja.trek.rpg.Mcrpg;

import java.util.List;

public record SyncJournalPayload(List<JournalEntry> entries) implements CustomPacketPayload {

    public record JournalEntry(
        String questFullId,
        String summary,
        String factionId,
        int level,
        String state,
        String location,
        long timer
    ) {}

    public static final Type<SyncJournalPayload> TYPE =
        new Type<>(Identifier.fromNamespaceAndPath(Mcrpg.MOD_ID, "sync_journal"));

    private static final StreamCodec<RegistryFriendlyByteBuf, JournalEntry> ENTRY_CODEC =
        new StreamCodec<>() {
            @Override
            public JournalEntry decode(RegistryFriendlyByteBuf buf) {
                return new JournalEntry(
                    buf.readUtf(),
                    buf.readUtf(),
                    buf.readUtf(),
                    buf.readVarInt(),
                    buf.readUtf(),
                    buf.readUtf(),
                    buf.readLong()
                );
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buf, JournalEntry entry) {
                buf.writeUtf(entry.questFullId());
                buf.writeUtf(entry.summary());
                buf.writeUtf(entry.factionId());
                buf.writeVarInt(entry.level());
                buf.writeUtf(entry.state());
                buf.writeUtf(entry.location());
                buf.writeLong(entry.timer());
            }
        };

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncJournalPayload> STREAM_CODEC =
        StreamCodec.composite(
            ENTRY_CODEC.apply(ByteBufCodecs.list()), SyncJournalPayload::entries,
            SyncJournalPayload::new
        );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
