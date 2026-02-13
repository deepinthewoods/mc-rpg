package ninja.trek.rpg.quest;

import net.minecraft.server.MinecraftServer;
import ninja.trek.rpg.Mcrpg;
import ninja.trek.rpg.data.CharacterData;
import ninja.trek.rpg.data.QuestData;
import ninja.trek.rpg.data.loader.RpgDataRegistry;
import ninja.trek.rpg.state.RpgWorldState;

import java.util.ArrayList;
import java.util.List;

public class CharacterManager {

    public static void initializeCharacterLocations(MinecraftServer server) {
        RpgWorldState state = RpgWorldState.get(server);
        RpgDataRegistry registry = RpgDataRegistry.getInstance();

        for (CharacterData character : registry.getAllCharacters()) {
            if (state.getCharacterLocation(character.id()) == null) {
                state.setCharacterLocation(character.id(), character.homeLocation());
            }
        }

        Mcrpg.LOGGER.info("Initialized character locations for {} characters", registry.getAllCharacters().size());
    }

    public static void moveCharactersForQuest(MinecraftServer server, QuestData quest) {
        RpgWorldState state = RpgWorldState.get(server);
        for (String charId : quest.characters()) {
            state.setCharacterLocation(charId, quest.location());
            Mcrpg.LOGGER.debug("Moved character {} to {} for quest {}", charId, quest.location(), quest.fullId());
        }
    }

    public static void returnCharactersHome(MinecraftServer server, QuestData quest) {
        RpgWorldState state = RpgWorldState.get(server);
        RpgDataRegistry registry = RpgDataRegistry.getInstance();

        for (String charId : quest.characters()) {
            CharacterData character = registry.getCharacter(charId);
            if (character != null) {
                state.setCharacterLocation(charId, character.homeLocation());
                Mcrpg.LOGGER.debug("Returned character {} home to {}", charId, character.homeLocation());
            }
        }
    }

    public static List<String> getCharactersAtLocation(MinecraftServer server, String locationId) {
        RpgWorldState state = RpgWorldState.get(server);
        List<String> result = new ArrayList<>();
        state.getAllCharacterLocations().forEach((charId, locId) -> {
            if (locId.equals(locationId)) {
                result.add(charId);
            }
        });
        return result;
    }
}
