package ninja.trek.rpg.network.payloads;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import ninja.trek.rpg.Mcrpg;

public record CloseDialogPayload() implements CustomPacketPayload {

    public static final Type<CloseDialogPayload> TYPE =
        new Type<>(Identifier.fromNamespaceAndPath(Mcrpg.MOD_ID, "close_dialog"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CloseDialogPayload> STREAM_CODEC =
        StreamCodec.unit(new CloseDialogPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
