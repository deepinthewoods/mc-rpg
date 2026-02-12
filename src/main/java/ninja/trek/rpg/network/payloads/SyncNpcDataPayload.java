package ninja.trek.rpg.network.payloads;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import ninja.trek.rpg.Mcrpg;

/**
 * Server-to-Client packet syncing NPC data (race, appearance, custom paths, size).
 */
public record SyncNpcDataPayload(
    int entityId,
    String raceName,
    int bodyIndex,
    int legsIndex,
    int armsIndex,
    int headIndex,
    String customModelPath,
    String customTexturePath,
    float sizeScale
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SyncNpcDataPayload> TYPE =
        new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Mcrpg.MOD_ID, "sync_npc_data"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncNpcDataPayload> STREAM_CODEC =
        new StreamCodec<>() {
            @Override
            public SyncNpcDataPayload decode(RegistryFriendlyByteBuf buf) {
                return new SyncNpcDataPayload(
                    buf.readVarInt(),
                    buf.readUtf(),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readUtf(),
                    buf.readUtf(),
                    buf.readFloat()
                );
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buf, SyncNpcDataPayload payload) {
                buf.writeVarInt(payload.entityId);
                buf.writeUtf(payload.raceName);
                buf.writeVarInt(payload.bodyIndex);
                buf.writeVarInt(payload.legsIndex);
                buf.writeVarInt(payload.armsIndex);
                buf.writeVarInt(payload.headIndex);
                buf.writeUtf(payload.customModelPath);
                buf.writeUtf(payload.customTexturePath);
                buf.writeFloat(payload.sizeScale);
            }
        };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
