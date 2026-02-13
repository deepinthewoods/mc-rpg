package ninja.trek.rpg.network.payloads;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import ninja.trek.rpg.Mcrpg;

import java.util.List;

public record OpenDialogPayload(
    String speakerName,
    String text,
    List<String> responses,
    String questFullId
) implements CustomPacketPayload {

    public static final Type<OpenDialogPayload> TYPE =
        new Type<>(Identifier.fromNamespaceAndPath(Mcrpg.MOD_ID, "open_dialog"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenDialogPayload> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, OpenDialogPayload::speakerName,
            ByteBufCodecs.STRING_UTF8, OpenDialogPayload::text,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), OpenDialogPayload::responses,
            ByteBufCodecs.STRING_UTF8, OpenDialogPayload::questFullId,
            OpenDialogPayload::new
        );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
