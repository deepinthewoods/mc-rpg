package ninja.trek.rpg.network.payloads;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import ninja.trek.rpg.Mcrpg;

public record RequestJournalPayload() implements CustomPacketPayload {

    public static final Type<RequestJournalPayload> TYPE =
        new Type<>(Identifier.fromNamespaceAndPath(Mcrpg.MOD_ID, "request_journal"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RequestJournalPayload> STREAM_CODEC =
        StreamCodec.unit(new RequestJournalPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
