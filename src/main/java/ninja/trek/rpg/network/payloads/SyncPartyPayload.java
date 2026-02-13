package ninja.trek.rpg.network.payloads;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import ninja.trek.rpg.Mcrpg;
import ninja.trek.rpg.state.PartyData;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record SyncPartyPayload(
    String leaderName,
    List<String> memberNames
) implements CustomPacketPayload {

    public static final Type<SyncPartyPayload> TYPE =
        new Type<>(Identifier.fromNamespaceAndPath(Mcrpg.MOD_ID, "sync_party"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncPartyPayload> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, SyncPartyPayload::leaderName,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), SyncPartyPayload::memberNames,
            SyncPartyPayload::new
        );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static SyncPartyPayload fromPartyData(PartyData party, MinecraftServer server) {
        String leaderName = "";
        if (party.getLeader() != null) {
            ServerPlayer leader = server.getPlayerList().getPlayer(party.getLeader());
            leaderName = leader != null ? leader.getName().getString() : party.getLeader().toString();
        }

        List<String> names = new ArrayList<>();
        for (UUID memberId : party.getMembers()) {
            ServerPlayer member = server.getPlayerList().getPlayer(memberId);
            names.add(member != null ? member.getName().getString() : memberId.toString());
        }

        return new SyncPartyPayload(leaderName, names);
    }
}
