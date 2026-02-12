package ninja.trek.rpg.client.render;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.item.ItemStack;
import ninja.trek.rpg.entity.data.CharacterAppearance;
import ninja.trek.rpg.entity.data.Race;
import ninja.trek.rpg.entity.layer.LayerConfiguration;
import software.bernie.geckolib.constant.dataticket.DataTicket;
import software.bernie.geckolib.renderer.base.GeoRenderState;

import java.util.Map;

/**
 * GeckoLib render state for NPC entities.
 * Contains all data needed for rendering including race, appearance, and layer configuration.
 */
public class NpcGeoRenderState extends LivingEntityRenderState implements GeoRenderState {
    private final Map<DataTicket<?>, Object> geckolibDataMap = new Object2ObjectOpenHashMap<>();

    public Race race = Race.HUMAN;
    public CharacterAppearance appearance;
    public LayerConfiguration layerConfiguration;
    public float sizeScale = 1.0f;

    // Custom model/texture overrides
    public String customModelPath = "";
    public String customTexturePath = "";

    // Animation data
    public boolean isMoving;
    public boolean isSprinting;
    public boolean isAttacking;

    // Held items
    public ItemStack mainHandStack = ItemStack.EMPTY;
    public ItemStack offHandStack = ItemStack.EMPTY;

    @Override
    public Map<DataTicket<?>, Object> getDataMap() {
        return geckolibDataMap;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <D> D getGeckolibData(DataTicket<D> dataTicket) {
        return (D) geckolibDataMap.get(dataTicket);
    }

    @Override
    public boolean hasGeckolibData(DataTicket<?> dataTicket) {
        return geckolibDataMap.containsKey(dataTicket);
    }

    @Override
    public <D> void addGeckolibData(DataTicket<D> dataTicket, D data) {
        geckolibDataMap.put(dataTicket, data);
    }
}
