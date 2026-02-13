package ninja.trek.rpg.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Map;

public record LocationData(
    String id,
    String name,
    List<String> tags,
    List<String> defaultConnections,
    Map<String, LocationStateData> states,
    String defaultState
) {
    public static final Codec<LocationData> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.STRING.fieldOf("id").forGetter(LocationData::id),
            Codec.STRING.fieldOf("name").forGetter(LocationData::name),
            Codec.STRING.listOf().optionalFieldOf("tags", List.of()).forGetter(LocationData::tags),
            Codec.STRING.listOf().optionalFieldOf("default_connections", List.of()).forGetter(LocationData::defaultConnections),
            Codec.unboundedMap(Codec.STRING, LocationStateData.CODEC).fieldOf("states").forGetter(LocationData::states),
            Codec.STRING.fieldOf("default_state").forGetter(LocationData::defaultState)
        ).apply(instance, LocationData::new)
    );
}
