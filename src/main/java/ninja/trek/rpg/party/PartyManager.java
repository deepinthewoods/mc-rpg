package ninja.trek.rpg.party;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import ninja.trek.rpg.Mcrpg;
import ninja.trek.rpg.network.payloads.PartyActionPayload;
import ninja.trek.rpg.network.payloads.PartyInvitePayload;
import ninja.trek.rpg.network.payloads.SyncPartyPayload;
import ninja.trek.rpg.state.PartyData;
import ninja.trek.rpg.state.RpgWorldState;
import ninja.trek.rpg.world.DimensionHelper;

import java.util.UUID;

public class PartyManager {

    public static void onPlayerEnterRpgDimension(ServerPlayer player) {
        RpgWorldState state = RpgWorldState.get(player.level().getServer());
        PartyData party = state.getParty();

        if (party.getLeader() == null) {
            party.setLeader(player.getUUID());
            state.setDirty();
            player.sendSystemMessage(Component.literal("You are now the party leader!"));
            Mcrpg.LOGGER.info("Player {} set as party leader", player.getName().getString());
        }

        if (!party.isMember(player.getUUID())) {
            party.addMember(player.getUUID());
            state.setDirty();
        }

        syncPartyToAll(player.level().getServer());
    }

    public static void invitePlayer(ServerPlayer leader, ServerPlayer target) {
        RpgWorldState state = RpgWorldState.get(leader.level().getServer());
        PartyData party = state.getParty();

        if (!party.isLeader(leader.getUUID())) {
            leader.sendSystemMessage(Component.literal("Only the party leader can invite players."));
            return;
        }

        if (party.isMember(target.getUUID())) {
            leader.sendSystemMessage(Component.literal(target.getName().getString() + " is already in the party."));
            return;
        }

        party.addInvite(target.getUUID());
        state.setDirty();

        leader.sendSystemMessage(Component.literal("Invited " + target.getName().getString() + " to the party."));
        ServerPlayNetworking.send(target, new PartyInvitePayload(leader.getName().getString()));
    }

    public static void acceptInvite(ServerPlayer player) {
        RpgWorldState state = RpgWorldState.get(player.level().getServer());
        PartyData party = state.getParty();

        if (!party.getPendingInvites().contains(player.getUUID())) {
            player.sendSystemMessage(Component.literal("You have no pending party invite."));
            return;
        }

        party.removeInvite(player.getUUID());
        party.addMember(player.getUUID());
        state.setDirty();

        player.sendSystemMessage(Component.literal("You joined the party!"));
        syncPartyToAll(player.level().getServer());
    }

    public static void requestJoin(ServerPlayer player) {
        RpgWorldState state = RpgWorldState.get(player.level().getServer());
        PartyData party = state.getParty();

        if (party.isMember(player.getUUID())) {
            player.sendSystemMessage(Component.literal("You are already in the party."));
            return;
        }

        party.addRequest(player.getUUID());
        state.setDirty();
        player.sendSystemMessage(Component.literal("Join request sent to the party leader."));

        // Notify leader
        if (party.getLeader() != null) {
            ServerPlayer leader = player.level().getServer().getPlayerList().getPlayer(party.getLeader());
            if (leader != null) {
                leader.sendSystemMessage(Component.literal(player.getName().getString() + " wants to join the party. Use /party accept <name>"));
            }
        }
    }

    public static void acceptRequest(ServerPlayer leader, ServerPlayer requester) {
        RpgWorldState state = RpgWorldState.get(leader.level().getServer());
        PartyData party = state.getParty();

        if (!party.isLeader(leader.getUUID())) {
            leader.sendSystemMessage(Component.literal("Only the party leader can accept requests."));
            return;
        }

        if (!party.getPendingRequests().contains(requester.getUUID())) {
            leader.sendSystemMessage(Component.literal("No pending request from that player."));
            return;
        }

        party.removeRequest(requester.getUUID());
        party.addMember(requester.getUUID());
        state.setDirty();

        requester.sendSystemMessage(Component.literal("You joined the party!"));
        syncPartyToAll(leader.level().getServer());
    }

    public static void kickPlayer(ServerPlayer leader, ServerPlayer target) {
        RpgWorldState state = RpgWorldState.get(leader.level().getServer());
        PartyData party = state.getParty();

        if (!party.isLeader(leader.getUUID())) {
            leader.sendSystemMessage(Component.literal("Only the party leader can kick players."));
            return;
        }

        if (leader.getUUID().equals(target.getUUID())) {
            leader.sendSystemMessage(Component.literal("You cannot kick yourself."));
            return;
        }

        party.removeMember(target.getUUID());
        state.setDirty();

        target.sendSystemMessage(Component.literal("You have been kicked from the party."));
        if (DimensionHelper.isInRpgWorld(target)) {
            DimensionHelper.teleportToOverworld(target);
        }
        syncPartyToAll(leader.level().getServer());
    }

    public static void leaveParty(ServerPlayer player) {
        RpgWorldState state = RpgWorldState.get(player.level().getServer());
        PartyData party = state.getParty();

        if (!party.isMember(player.getUUID())) {
            player.sendSystemMessage(Component.literal("You are not in a party."));
            return;
        }

        party.removeMember(player.getUUID());
        state.setDirty();

        player.sendSystemMessage(Component.literal("You left the party."));
        if (DimensionHelper.isInRpgWorld(player)) {
            DimensionHelper.teleportToOverworld(player);
        }
        syncPartyToAll(player.level().getServer());
    }

    public static void handlePartyAction(ServerPlayer player, PartyActionPayload payload) {
        switch (payload.action()) {
            case "accept_invite" -> acceptInvite(player);
            case "request_join" -> requestJoin(player);
            case "leave" -> leaveParty(player);
        }
    }

    public static void syncPartyToAll(MinecraftServer server) {
        RpgWorldState state = RpgWorldState.get(server);
        PartyData party = state.getParty();

        SyncPartyPayload payload = SyncPartyPayload.fromPartyData(party, server);

        for (UUID memberId : party.getMembers()) {
            ServerPlayer member = server.getPlayerList().getPlayer(memberId);
            if (member != null) {
                ServerPlayNetworking.send(member, payload);
            }
        }
    }
}
