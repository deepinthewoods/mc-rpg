package ninja.trek.rpg.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Optional;

public record QuestData(
    String id,
    String factionId,
    int level,
    boolean consequential,
    String summary,
    String dialogId,
    String location,
    List<String> characters,
    QuestRequirements requirements,
    List<QuestBranch> branches,
    Optional<AutoResolveConfig> autoResolve
) {
    public static final Codec<QuestData> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.STRING.fieldOf("id").forGetter(QuestData::id),
            Codec.STRING.fieldOf("faction_id").forGetter(QuestData::factionId),
            Codec.INT.optionalFieldOf("level", 1).forGetter(QuestData::level),
            Codec.BOOL.optionalFieldOf("consequential", false).forGetter(QuestData::consequential),
            Codec.STRING.optionalFieldOf("summary", "").forGetter(QuestData::summary),
            Codec.STRING.optionalFieldOf("dialog_id", "").forGetter(QuestData::dialogId),
            Codec.STRING.fieldOf("location").forGetter(QuestData::location),
            Codec.STRING.listOf().optionalFieldOf("characters", List.of()).forGetter(QuestData::characters),
            QuestRequirements.CODEC.optionalFieldOf("requirements", QuestRequirements.EMPTY).forGetter(QuestData::requirements),
            QuestBranch.CODEC.listOf().optionalFieldOf("branches", List.of()).forGetter(QuestData::branches),
            AutoResolveConfig.CODEC.optionalFieldOf("auto_resolve").forGetter(QuestData::autoResolve)
        ).apply(instance, QuestData::new)
    );

    public String fullId() {
        return factionId + "." + id;
    }
}
