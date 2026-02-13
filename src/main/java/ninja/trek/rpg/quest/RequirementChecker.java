package ninja.trek.rpg.quest;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import ninja.trek.rpg.data.BranchRequirements;
import ninja.trek.rpg.data.QuestRequirements;
import ninja.trek.rpg.state.QuestState;
import ninja.trek.rpg.state.RpgWorldState;

import java.util.Map;

public class RequirementChecker {

    public static boolean checkRequirements(QuestRequirements requirements, RpgWorldState state) {
        // Check completed quests
        for (String questId : requirements.questsCompleted()) {
            if (state.getQuestState(questId) != QuestState.COMPLETED) {
                return false;
            }
        }

        // Check faction stats
        for (Map.Entry<String, String> entry : requirements.factionStats().entrySet()) {
            String key = entry.getKey(); // format: "faction.stat"
            String comparison = entry.getValue(); // format: ">= 5"

            String[] parts = key.split("\\.", 2);
            if (parts.length != 2) continue;

            int currentValue = state.getFactionStat(parts[0], parts[1]);
            if (!evaluateComparison(comparison, currentValue)) {
                return false;
            }
        }

        // Check global vars
        for (Map.Entry<String, Boolean> entry : requirements.globalVars().entrySet()) {
            if (state.getGlobalVar(entry.getKey()) != entry.getValue()) {
                return false;
            }
        }

        return true;
    }

    public static boolean checkBranchRequirements(BranchRequirements requirements, ServerPlayer player) {
        for (Map.Entry<String, Integer> entry : requirements.itemRequirements().entrySet()) {
            String itemId = entry.getKey();
            int requiredCount = entry.getValue();

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
        return true;
    }

    public static boolean evaluateComparison(String comparison, int value) {
        comparison = comparison.trim();
        if (comparison.startsWith(">=")) {
            int target = Integer.parseInt(comparison.substring(2).trim());
            return value >= target;
        } else if (comparison.startsWith("<=")) {
            int target = Integer.parseInt(comparison.substring(2).trim());
            return value <= target;
        } else if (comparison.startsWith(">")) {
            int target = Integer.parseInt(comparison.substring(1).trim());
            return value > target;
        } else if (comparison.startsWith("<")) {
            int target = Integer.parseInt(comparison.substring(1).trim());
            return value < target;
        } else if (comparison.startsWith("==")) {
            int target = Integer.parseInt(comparison.substring(2).trim());
            return value == target;
        } else {
            // Try as plain number (equals)
            try {
                int target = Integer.parseInt(comparison);
                return value >= target;
            } catch (NumberFormatException e) {
                return false;
            }
        }
    }
}
