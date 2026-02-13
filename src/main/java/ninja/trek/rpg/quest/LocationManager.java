package ninja.trek.rpg.quest;

import net.minecraft.server.MinecraftServer;
import ninja.trek.rpg.Mcrpg;
import ninja.trek.rpg.data.LocationData;
import ninja.trek.rpg.data.LocationStateData;
import ninja.trek.rpg.data.loader.RpgDataRegistry;
import ninja.trek.rpg.state.RpgWorldState;
import ninja.trek.rpg.world.StructureReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocationManager {

    private static final Map<String, StructureReference> structureReferences = new HashMap<>();

    public static void registerStructure(String locationId, StructureReference reference) {
        structureReferences.put(locationId, reference);
        Mcrpg.LOGGER.info("Registered structure for location {}: {}", locationId, reference.id());
    }

    public static StructureReference getStructureReference(String locationId) {
        return structureReferences.get(locationId);
    }

    public static void onStateChanged(MinecraftServer server, String locationId, String newState) {
        Mcrpg.LOGGER.info("Location {} state changed to: {}", locationId, newState);
    }

    public static List<String> getCurrentConnections(MinecraftServer server, String locationId) {
        RpgDataRegistry registry = RpgDataRegistry.getInstance();
        RpgWorldState state = RpgWorldState.get(server);

        LocationData location = registry.getLocation(locationId);
        if (location == null) return List.of();

        String currentStateName = state.getLocationState(locationId);
        if (currentStateName == null) {
            currentStateName = location.defaultState();
        }

        LocationStateData stateData = location.states().get(currentStateName);
        if (stateData != null && stateData.connections().isPresent()) {
            return stateData.connections().get();
        }

        return location.defaultConnections();
    }
}
