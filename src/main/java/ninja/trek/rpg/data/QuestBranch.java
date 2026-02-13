package ninja.trek.rpg.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record QuestBranch(
    String id,
    String summary,
    BranchRequirements requirements,
    QuestOutcomes outcomes
) {
    public static final Codec<QuestBranch> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.STRING.fieldOf("id").forGetter(QuestBranch::id),
            Codec.STRING.optionalFieldOf("summary", "").forGetter(QuestBranch::summary),
            BranchRequirements.CODEC.optionalFieldOf("requirements", BranchRequirements.EMPTY).forGetter(QuestBranch::requirements),
            QuestOutcomes.CODEC.fieldOf("outcomes").forGetter(QuestBranch::outcomes)
        ).apply(instance, QuestBranch::new)
    );
}
