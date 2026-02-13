package ninja.trek.rpg.network.payloads;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import ninja.trek.rpg.Mcrpg;

import java.util.List;

public record DialogUpdatePayload(
    String speakerName,
    String text,
    List<String> responses
) implements CustomPacketPayload {

    public static final Type<DialogUpdatePayload> TYPE =
        new Type<>(Identifier.fromNamespaceAndPath(Mcrpg.MOD_ID, "dialog_update"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DialogUpdatePayload> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, DialogUpdatePayload::speakerName,
            ByteBufCodecs.STRING_UTF8, DialogUpdatePayload::text,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), DialogUpdatePayload::responses,
            DialogUpdatePayload::new
        );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
