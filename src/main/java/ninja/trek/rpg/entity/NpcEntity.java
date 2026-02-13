package ninja.trek.rpg.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import ninja.trek.rpg.dialog.DialogManager;
import ninja.trek.rpg.entity.data.CharacterAppearance;
import ninja.trek.rpg.entity.data.Race;
import ninja.trek.rpg.entity.layer.LayerConfiguration;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.object.PlayState;
import software.bernie.geckolib.animation.state.AnimationTest;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * Generic NPC entity with race-specific GeckoLib models.
 * Stripped of all D&D 5e combat/stats systems.
 * Supports custom model/texture path overrides for fully custom NPCs.
 */
public class NpcEntity extends PathfinderMob implements GeoEntity {

    // Synced entity data
    private static final EntityDataAccessor<Integer> DATA_BODY_INDEX =
        SynchedEntityData.defineId(NpcEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_LEGS_INDEX =
        SynchedEntityData.defineId(NpcEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ARMS_INDEX =
        SynchedEntityData.defineId(NpcEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_HEAD_INDEX =
        SynchedEntityData.defineId(NpcEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> DATA_RACE =
        SynchedEntityData.defineId(NpcEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> DATA_CUSTOM_MODEL =
        SynchedEntityData.defineId(NpcEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> DATA_CUSTOM_TEXTURE =
        SynchedEntityData.defineId(NpcEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> DATA_CHARACTER_ID =
        SynchedEntityData.defineId(NpcEntity.class, EntityDataSerializers.STRING);

    // Character data
    private Race race = Race.HUMAN;
    private float sizeScale = 1.0f;
    private LayerConfiguration layerConfiguration = new LayerConfiguration();

    // GeckoLib animation cache
    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);

    // Animation state tracking
    private boolean isAttacking = false;
    private long attackStartTime = 0;

    // Animation definitions
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.character.idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.character.walk");
    private static final RawAnimation RUN = RawAnimation.begin().thenLoop("animation.character.run");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("animation.character.attack");

    public NpcEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 20.0)
            .add(Attributes.MOVEMENT_SPEED, 0.25)
            .add(Attributes.ATTACK_DAMAGE, 2.0)
            .add(Attributes.ARMOR, 0.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_BODY_INDEX, 0);
        builder.define(DATA_LEGS_INDEX, 0);
        builder.define(DATA_ARMS_INDEX, 0);
        builder.define(DATA_HEAD_INDEX, 0);
        builder.define(DATA_RACE, "human");
        builder.define(DATA_CUSTOM_MODEL, "");
        builder.define(DATA_CUSTOM_TEXTURE, "");
        builder.define(DATA_CHARACTER_ID, "");
    }

    /**
     * Initialize NPC from creation data.
     */
    public void initializeNpc(Race race, CharacterAppearance appearance) {
        this.race = race;
        this.sizeScale = race.getDefaultSizeScale();
        this.entityData.set(DATA_RACE, race.getName());
        setAppearance(appearance);

        // Update layer configuration from appearance
        layerConfiguration.setBodyVariant(appearance.bodyIndex());
        layerConfiguration.setLegsVariant(appearance.legsIndex());
        layerConfiguration.setArmsVariant(appearance.armsIndex());
        layerConfiguration.setHeadVariant(appearance.headIndex());
    }

    public void setAppearance(CharacterAppearance appearance) {
        this.entityData.set(DATA_BODY_INDEX, appearance.bodyIndex());
        this.entityData.set(DATA_LEGS_INDEX, appearance.legsIndex());
        this.entityData.set(DATA_ARMS_INDEX, appearance.armsIndex());
        this.entityData.set(DATA_HEAD_INDEX, appearance.headIndex());
    }

    public CharacterAppearance getAppearance() {
        return new CharacterAppearance(
            this.entityData.get(DATA_BODY_INDEX),
            this.entityData.get(DATA_LEGS_INDEX),
            this.entityData.get(DATA_ARMS_INDEX),
            this.entityData.get(DATA_HEAD_INDEX)
        );
    }

    public Race getRace() {
        return race;
    }

    public void setRace(Race race) {
        this.race = race;
        this.entityData.set(DATA_RACE, race.getName());
    }

    public String getRaceName() {
        return this.entityData.get(DATA_RACE);
    }

    public float getSizeScale() {
        return sizeScale;
    }

    public void setSizeScale(float sizeScale) {
        this.sizeScale = Math.max(0.5f, Math.min(3.0f, sizeScale));
    }

    public LayerConfiguration getLayerConfiguration() {
        return layerConfiguration;
    }

    public void setLayerConfiguration(LayerConfiguration layerConfiguration) {
        this.layerConfiguration = layerConfiguration;
    }

    public String getCustomModelPath() {
        return this.entityData.get(DATA_CUSTOM_MODEL);
    }

    public void setCustomModelPath(String path) {
        this.entityData.set(DATA_CUSTOM_MODEL, path != null ? path : "");
    }

    public String getCustomTexturePath() {
        return this.entityData.get(DATA_CUSTOM_TEXTURE);
    }

    public void setCustomTexturePath(String path) {
        this.entityData.set(DATA_CUSTOM_TEXTURE, path != null ? path : "");
    }

    public String getCharacterId() {
        return this.entityData.get(DATA_CHARACTER_ID);
    }

    public void setCharacterId(String characterId) {
        this.entityData.set(DATA_CHARACTER_ID, characterId != null ? characterId : "");
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
            String characterId = getCharacterId();
            if (!characterId.isEmpty()) {
                DialogManager.handleNpcInteraction(serverPlayer, characterId, this);
                return InteractionResult.SUCCESS;
            }
        }
        return super.mobInteract(player, hand);
    }

    public boolean isAttacking() {
        return isAttacking;
    }

    public boolean startAttack() {
        if (isAttacking) return false;
        isAttacking = true;
        attackStartTime = System.currentTimeMillis();
        return true;
    }

    public void completeAttack() {
        isAttacking = false;
        attackStartTime = 0;
    }

    public long getAttackElapsedTime() {
        if (!isAttacking) return 0;
        return System.currentTimeMillis() - attackStartTime;
    }

    // GeckoLib implementation

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>("movement", 0, this::animationController));
    }

    private PlayState animationController(AnimationTest<?> animTest) {
        // Check for attacking
        if (isAttacking || this.swinging) {
            if (getAttackElapsedTime() >= 600) {
                completeAttack();
            } else {
                return animTest.setAndContinue(ATTACK);
            }
        }

        // Locomotion
        if (animTest.isMoving()) {
            if (this.isSprinting()) {
                return animTest.setAndContinue(RUN);
            } else {
                return animTest.setAndContinue(WALK);
            }
        }

        // Idle
        return animTest.setAndContinue(IDLE);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.animationCache;
    }

    // NBT Persistence

    @Override
    protected void addAdditionalSaveData(ValueOutput tag) {
        super.addAdditionalSaveData(tag);

        tag.putString("Race", race.name());

        CharacterAppearance appearance = getAppearance();
        tag.putInt("BodyIndex", appearance.bodyIndex());
        tag.putInt("LegsIndex", appearance.legsIndex());
        tag.putInt("ArmsIndex", appearance.armsIndex());
        tag.putInt("HeadIndex", appearance.headIndex());
        tag.putFloat("SizeScale", sizeScale);

        String customModel = getCustomModelPath();
        if (customModel != null && !customModel.isEmpty()) {
            tag.putString("CustomModel", customModel);
        }
        String customTexture = getCustomTexturePath();
        if (customTexture != null && !customTexture.isEmpty()) {
            tag.putString("CustomTexture", customTexture);
        }

        String characterId = getCharacterId();
        if (characterId != null && !characterId.isEmpty()) {
            tag.putString("CharacterId", characterId);
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput tag) {
        super.readAdditionalSaveData(tag);

        String raceName = tag.getStringOr("Race", "HUMAN");
        try {
            this.race = Race.valueOf(raceName);
            this.entityData.set(DATA_RACE, race.getName());
        } catch (IllegalArgumentException e) {
            this.race = Race.HUMAN;
        }

        CharacterAppearance appearance = new CharacterAppearance(
            tag.getIntOr("BodyIndex", 0),
            tag.getIntOr("LegsIndex", 0),
            tag.getIntOr("ArmsIndex", 0),
            tag.getIntOr("HeadIndex", 0)
        );
        setAppearance(appearance);

        sizeScale = tag.getFloatOr("SizeScale", 1.0f);

        tag.getString("CustomModel").ifPresent(this::setCustomModelPath);
        tag.getString("CustomTexture").ifPresent(this::setCustomTexturePath);
        tag.getString("CharacterId").ifPresent(this::setCharacterId);
    }
}
