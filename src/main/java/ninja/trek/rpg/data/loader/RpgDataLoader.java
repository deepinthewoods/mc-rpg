package ninja.trek.rpg.data.loader;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import ninja.trek.rpg.Mcrpg;
import ninja.trek.rpg.data.*;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class RpgDataLoader implements SimpleSynchronousResourceReloadListener {

    private static final String DATA_PREFIX = "rpg/";

    @Override
    public Identifier getFabricId() {
        return Identifier.fromNamespaceAndPath(Mcrpg.MOD_ID, "rpg_data_loader");
    }

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        RpgDataRegistry registry = RpgDataRegistry.getInstance();
        registry.clear();

        Mcrpg.LOGGER.info("Loading RPG data...");

        int characters = loadAll(manager, DATA_PREFIX + "characters", CharacterData.CODEC, (id, data) -> registry.registerCharacter(data));
        int locations = loadAll(manager, DATA_PREFIX + "locations", LocationData.CODEC, (id, data) -> registry.registerLocation(data));
        int factions = loadAll(manager, DATA_PREFIX + "factions", FactionData.CODEC, (id, data) -> registry.registerFaction(data));
        int quests = loadAll(manager, DATA_PREFIX + "quests", QuestData.CODEC, (id, data) -> registry.registerQuest(data));
        int dialogs = loadAll(manager, DATA_PREFIX + "dialogs", DialogTreeData.CODEC, (id, data) -> registry.registerDialog(data));

        Mcrpg.LOGGER.info("Loaded RPG data: {} characters, {} locations, {} factions, {} quests, {} dialogs",
            characters, locations, factions, quests, dialogs);

        registry.validate();
    }

    private <T> int loadAll(ResourceManager manager, String directory, Codec<T> codec, DataConsumer<T> consumer) {
        int count = 0;
        Map<Identifier, Resource> resources = manager.listResources(directory, id -> id.getPath().endsWith(".json"));

        for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
            Identifier resourceId = entry.getKey();
            // Only load from our namespace
            if (!resourceId.getNamespace().equals(Mcrpg.MOD_ID)) continue;

            try (InputStreamReader reader = new InputStreamReader(entry.getValue().open(), StandardCharsets.UTF_8)) {
                JsonElement json = JsonParser.parseReader(reader);
                var result = codec.parse(JsonOps.INSTANCE, json);

                if (result.isSuccess()) {
                    consumer.accept(resourceId.toString(), result.getOrThrow());
                    count++;
                } else {
                    Mcrpg.LOGGER.error("Failed to parse {}: {}", resourceId, result.error().orElse(null));
                }
            } catch (Exception e) {
                Mcrpg.LOGGER.error("Failed to load {}: {}", resourceId, e.getMessage());
            }
        }

        return count;
    }

    @FunctionalInterface
    private interface DataConsumer<T> {
        void accept(String id, T data);
    }
}
