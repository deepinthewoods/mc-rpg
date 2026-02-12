package ninja.trek.rpg.network.payloads;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import ninja.trek.rpg.Mcrpg;
import ninja.trek.rpg.entity.data.CharacterAppearance;
import ninja.trek.rpg.entity.data.Race;

/**
 * Client-to-Server packet requesting to create a new NPC entity.
 */
public record CreateNpcPayload(
    String name,
    Race race,
    CharacterAppearance appearance
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<CreateNpcPayload> TYPE =
        new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Mcrpg.MOD_ID, "create_npc"));

    public static final StreamCodec<RegistryFriendlyByteBuf, Race> RACE_CODEC =
        ByteBufCodecs.idMapper(i -> Race.values()[i], Race::ordinal).cast();

    public static final StreamCodec<RegistryFriendlyByteBuf, CharacterAppearance> APPEARANCE_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.VAR_INT, CharacterAppearance::bodyIndex,
            ByteBufCodecs.VAR_INT, CharacterAppearance::legsIndex,
            ByteBufCodecs.VAR_INT, CharacterAppearance::armsIndex,
            ByteBufCodecs.VAR_INT, CharacterAppearance::headIndex,
            CharacterAppearance::new
        );

    public static final StreamCodec<RegistryFriendlyByteBuf, CreateNpcPayload> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, CreateNpcPayload::name,
            RACE_CODEC, CreateNpcPayload::race,
            APPEARANCE_CODEC, CreateNpcPayload::appearance,
            CreateNpcPayload::new
        );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
