package ninja.trek.rpg.party;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import ninja.trek.rpg.state.PartyData;
import ninja.trek.rpg.state.RpgWorldState;

import java.util.UUID;

public class PartyCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("party")
            .then(Commands.literal("info")
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    RpgWorldState state = RpgWorldState.get(player.level().getServer());
                    PartyData party = state.getParty();

                    if (party.getLeader() == null) {
                        ctx.getSource().sendSuccess(() -> Component.literal("No party exists yet. Enter the RPG world to create one."), false);
                        return 1;
                    }

                    StringBuilder sb = new StringBuilder("=== Party Info ===\n");
                    ServerPlayer leader = player.level().getServer().getPlayerList().getPlayer(party.getLeader());
                    sb.append("Leader: ").append(leader != null ? leader.getName().getString() : party.getLeader().toString()).append("\n");
                    sb.append("Members: ");
                    for (UUID memberId : party.getMembers()) {
                        ServerPlayer member = player.level().getServer().getPlayerList().getPlayer(memberId);
                        sb.append(member != null ? member.getName().getString() : memberId.toString()).append(", ");
                    }
                    ctx.getSource().sendSuccess(() -> Component.literal(sb.toString()), false);
                    return 1;
                })
            )
            .then(Commands.literal("invite")
                .then(Commands.argument("player", EntityArgument.player())
                    .executes(ctx -> {
                        ServerPlayer player = ctx.getSource().getPlayerOrException();
                        ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                        PartyManager.invitePlayer(player, target);
                        return 1;
                    })
                )
            )
            .then(Commands.literal("join")
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    PartyManager.requestJoin(player);
                    return 1;
                })
            )
            .then(Commands.literal("leave")
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    PartyManager.leaveParty(player);
                    return 1;
                })
            )
            .then(Commands.literal("kick")
                .then(Commands.argument("player", EntityArgument.player())
                    .executes(ctx -> {
                        ServerPlayer player = ctx.getSource().getPlayerOrException();
                        ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                        PartyManager.kickPlayer(player, target);
                        return 1;
                    })
                )
            )
            .then(Commands.literal("accept")
                .then(Commands.argument("player", EntityArgument.player())
                    .executes(ctx -> {
                        ServerPlayer leader = ctx.getSource().getPlayerOrException();
                        ServerPlayer requester = EntityArgument.getPlayer(ctx, "player");
                        PartyManager.acceptRequest(leader, requester);
                        return 1;
                    })
                )
            )
        );
    }
}
