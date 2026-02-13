package ninja.trek.rpg.world;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class DimensionCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("rpg")
            .then(Commands.literal("enter")
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    if (DimensionHelper.isInRpgWorld(player)) {
                        ctx.getSource().sendFailure(Component.literal("You are already in the RPG world!"));
                        return 0;
                    }
                    DimensionHelper.teleportToRpgWorld(player);
                    ctx.getSource().sendSuccess(() -> Component.literal("Teleported to the RPG world."), true);
                    return 1;
                })
            )
            .then(Commands.literal("leave")
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    if (!DimensionHelper.isInRpgWorld(player)) {
                        ctx.getSource().sendFailure(Component.literal("You are not in the RPG world!"));
                        return 0;
                    }
                    DimensionHelper.teleportToOverworld(player);
                    ctx.getSource().sendSuccess(() -> Component.literal("Returned to the Overworld."), true);
                    return 1;
                })
            )
        );
    }
}
