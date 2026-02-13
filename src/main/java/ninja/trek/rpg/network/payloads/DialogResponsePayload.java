package ninja.trek.rpg.network.payloads;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import ninja.trek.rpg.Mcrpg;

public record DialogResponsePayload(int responseIndex) implements CustomPacketPayload {

    public static final Type<DialogResponsePayload> TYPE =
        new Type<>(Identifier.fromNamespaceAndPath(Mcrpg.MOD_ID, "dialog_response"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DialogResponsePayload> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.VAR_INT, DialogResponsePayload::responseIndex,
            DialogResponsePayload::new
        );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
