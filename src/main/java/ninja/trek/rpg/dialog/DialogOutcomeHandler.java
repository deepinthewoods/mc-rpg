package ninja.trek.rpg.dialog;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import ninja.trek.rpg.data.DialogOutcome;
import ninja.trek.rpg.state.RpgWorldState;

import java.util.Map;

public class DialogOutcomeHandler {

    public static void apply(DialogOutcome outcome, ServerPlayer player, DialogSession session) {
        RpgWorldState state = RpgWorldState.get(player.level().getServer());

        // Select branch
        outcome.selectBranch().ifPresent(session::setSelectedBranch);

        // Faction stats
        for (Map.Entry<String, Integer> entry : outcome.factionStats().entrySet()) {
            String key = entry.getKey();
            String[] parts = key.split("\\.", 2);
            if (parts.length == 2) {
                state.addFactionStat(parts[0], parts[1], entry.getValue());
            }
        }

        // Global vars
        for (Map.Entry<String, Boolean> entry : outcome.globalVars().entrySet()) {
            state.setGlobalVar(entry.getKey(), entry.getValue());
        }

        // Give items
        for (Map.Entry<String, Integer> entry : outcome.giveItems().entrySet()) {
            Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(entry.getKey()));
            if (item != null) {
                ItemStack stack = new ItemStack(item, entry.getValue());
                player.getInventory().add(stack);
            }
        }

        // Take items
        for (Map.Entry<String, Integer> entry : outcome.takeItems().entrySet()) {
            Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(entry.getKey()));
            if (item != null) {
                int remaining = entry.getValue();
                int inventorySize = player.getInventory().getContainerSize();
                for (int i = 0; i < inventorySize && remaining > 0; i++) {
                    ItemStack stack = player.getInventory().getItem(i);
                    if (stack.is(item)) {
                        int toRemove = Math.min(remaining, stack.getCount());
                        stack.shrink(toRemove);
                        remaining -= toRemove;
                    }
                }
            }
        }
    }
}
