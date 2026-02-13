package ninja.trek.rpg.quest;

import net.minecraft.server.MinecraftServer;
import ninja.trek.rpg.Mcrpg;
import ninja.trek.rpg.data.CharacterExtraEntry;
import ninja.trek.rpg.data.FactionData;
import ninja.trek.rpg.data.QuestOutcomes;
import ninja.trek.rpg.data.loader.RpgDataRegistry;
import ninja.trek.rpg.state.CharacterExtra;
import ninja.trek.rpg.state.RpgWorldState;

import java.util.Map;

public class OutcomeApplicator {

    public static void apply(MinecraftServer server, QuestOutcomes outcomes, String source) {
        RpgWorldState state = RpgWorldState.get(server);

        // Faction stats
        for (Map.Entry<String, Integer> entry : outcomes.factionStats().entrySet()) {
            String key = entry.getKey(); // "faction.stat"
            String[] parts = key.split("\\.", 2);
            if (parts.length == 2) {
                state.addFactionStat(parts[0], parts[1], entry.getValue());
                Mcrpg.LOGGER.debug("Applied faction stat: {}.{} += {}", parts[0], parts[1], entry.getValue());
            }
        }

        // Global vars
        for (Map.Entry<String, Boolean> entry : outcomes.globalVars().entrySet()) {
            state.setGlobalVar(entry.getKey(), entry.getValue());
            Mcrpg.LOGGER.debug("Set global var: {} = {}", entry.getKey(), entry.getValue());
        }

        // Location states
        for (Map.Entry<String, String> entry : outcomes.locationStates().entrySet()) {
            state.setLocationState(entry.getKey(), entry.getValue());
            LocationManager.onStateChanged(server, entry.getKey(), entry.getValue());
            Mcrpg.LOGGER.debug("Set location state: {} -> {}", entry.getKey(), entry.getValue());
        }

        // Character extras
        for (CharacterExtraEntry extraEntry : outcomes.characterExtras()) {
            state.addCharacterExtra(extraEntry.character(), new CharacterExtra(extraEntry.text(), source));
            Mcrpg.LOGGER.debug("Added character extra to {}: {}", extraEntry.character(), extraEntry.text());
        }

        // Move characters
        for (Map.Entry<String, String> entry : outcomes.moveCharacters().entrySet()) {
            state.setCharacterLocation(entry.getKey(), entry.getValue());
            Mcrpg.LOGGER.debug("Moved character {} to {}", entry.getKey(), entry.getValue());
        }

        // Unlock factions
        for (String factionId : outcomes.unlockFactions()) {
            FactionData faction = RpgDataRegistry.getInstance().getFaction(factionId);
            if (faction != null) {
                Mcrpg.LOGGER.info("Unlocked faction: {}", factionId);
                // Faction unlock is tracked via the quests becoming available
            }
        }
    }
}
