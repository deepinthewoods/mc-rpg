package ninja.trek.rpg.network.payloads;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import ninja.trek.rpg.Mcrpg;

public record QuestActionPayload(String questFullId) implements CustomPacketPayload {

    public static final Type<QuestActionPayload> TYPE =
        new Type<>(Identifier.fromNamespaceAndPath(Mcrpg.MOD_ID, "quest_action"));

    public static final StreamCodec<RegistryFriendlyByteBuf, QuestActionPayload> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, QuestActionPayload::questFullId,
            QuestActionPayload::new
        );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
