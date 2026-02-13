package ninja.trek.rpg.dialog;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import ninja.trek.rpg.data.DialogCondition;
import ninja.trek.rpg.quest.RequirementChecker;
import ninja.trek.rpg.state.QuestState;
import ninja.trek.rpg.state.RpgWorldState;

public class DialogConditionEvaluator {

    public static boolean evaluate(DialogCondition condition, ServerPlayer player) {
        RpgWorldState state = RpgWorldState.get(player.level().getServer());

        // Quest completed check
        if (condition.questCompleted().isPresent()) {
            String questId = condition.questCompleted().get();
            if (state.getQuestState(questId) != QuestState.COMPLETED) {
                return false;
            }
        }

        // Faction stat check (format: "faction.stat >= value")
        if (condition.factionStat().isPresent()) {
            String expr = condition.factionStat().get();
            // Parse "faction.stat >= value"
            String[] parts = expr.split("\\s+", 2);
            if (parts.length == 2) {
                String[] keyParts = parts[0].split("\\.", 2);
                if (keyParts.length == 2) {
                    int currentVal = state.getFactionStat(keyParts[0], keyParts[1]);
                    if (!RequirementChecker.evaluateComparison(parts[1], currentVal)) {
                        return false;
                    }
                }
            }
        }

        // Has item check
        if (condition.hasItem().isPresent()) {
            String itemId = condition.hasItem().get();
            int requiredCount = condition.hasItemCount().orElse(1);

            Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(itemId));
            if (item == null) return false;

            int count = 0;
            int inventorySize = player.getInventory().getContainerSize();
            for (int i = 0; i < inventorySize; i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (stack.is(item)) {
                    count += stack.getCount();
                }
            }
            if (count < requiredCount) return false;
        }

        // Global var check
        if (condition.globalVar().isPresent()) {
            String varName = condition.globalVar().get();
            if (!state.getGlobalVar(varName)) {
                return false;
            }
        }

        return true;
    }
}
