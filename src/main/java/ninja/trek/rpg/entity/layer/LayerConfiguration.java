package ninja.trek.rpg.entity.layer;

import java.util.HashMap;
import java.util.Map;

/**
 * Complete layer configuration for a character, defining which mesh parts and equipment
 * should be rendered.
 */
public class LayerConfiguration {

    // Base body part variants (0-based indices)
    private int bodyVariant = 0;
    private int legsVariant = 0;
    private int armsVariant = 0;
    private int headVariant = 0;

    // Equipment slots (null = no equipment in that slot)
    private String helmetModel = null;
    private String chestArmorModel = null;
    private String legArmorModel = null;
    private String bootArmorModel = null;
    private String capeModel = null;
    private String mainHandModel = null;
    private String offHandModel = null;

    // Texture overrides
    private String skinTexture = "default";
    private String clothingTexture = "default";
    private Map<String, String> armorTextures = new HashMap<>();

    // Visibility flags
    private boolean showHair = true;
    private boolean showEars = true;
    private boolean showCape = true;

    public LayerConfiguration() {
    }

    public int getBodyVariant() { return bodyVariant; }
    public void setBodyVariant(int bodyVariant) { this.bodyVariant = bodyVariant; }

    public int getLegsVariant() { return legsVariant; }
    public void setLegsVariant(int legsVariant) { this.legsVariant = legsVariant; }

    public int getArmsVariant() { return armsVariant; }
    public void setArmsVariant(int armsVariant) { this.armsVariant = armsVariant; }

    public int getHeadVariant() { return headVariant; }
    public void setHeadVariant(int headVariant) { this.headVariant = headVariant; }

    public String getHelmetModel() { return helmetModel; }
    public void setHelmetModel(String helmetModel) {
        this.helmetModel = helmetModel;
        if (helmetModel != null) {
            this.showHair = false;
            this.showEars = false;
        }
    }

    public String getChestArmorModel() { return chestArmorModel; }
    public void setChestArmorModel(String chestArmorModel) { this.chestArmorModel = chestArmorModel; }

    public String getLegArmorModel() { return legArmorModel; }
    public void setLegArmorModel(String legArmorModel) { this.legArmorModel = legArmorModel; }

    public String getBootArmorModel() { return bootArmorModel; }
    public void setBootArmorModel(String bootArmorModel) { this.bootArmorModel = bootArmorModel; }

    public String getCapeModel() { return capeModel; }
    public void setCapeModel(String capeModel) { this.capeModel = capeModel; }

    public String getMainHandModel() { return mainHandModel; }
    public void setMainHandModel(String mainHandModel) { this.mainHandModel = mainHandModel; }

    public String getOffHandModel() { return offHandModel; }
    public void setOffHandModel(String offHandModel) { this.offHandModel = offHandModel; }

    public String getSkinTexture() { return skinTexture; }
    public void setSkinTexture(String skinTexture) { this.skinTexture = skinTexture; }

    public String getClothingTexture() { return clothingTexture; }
    public void setClothingTexture(String clothingTexture) { this.clothingTexture = clothingTexture; }

    public Map<String, String> getArmorTextures() { return armorTextures; }
    public void setArmorTexture(String slot, String texture) { this.armorTextures.put(slot, texture); }

    public boolean isShowHair() { return showHair; }
    public void setShowHair(boolean showHair) { this.showHair = showHair; }

    public boolean isShowEars() { return showEars; }
    public void setShowEars(boolean showEars) { this.showEars = showEars; }

    public boolean isShowCape() { return showCape; }
    public void setShowCape(boolean showCape) { this.showCape = showCape; }

    public boolean hasAnyEquipment() {
        return helmetModel != null || chestArmorModel != null || legArmorModel != null
                || bootArmorModel != null || capeModel != null;
    }

    public boolean hasEquipmentInSlot(EquipmentLayerSlot slot) {
        return switch (slot) {
            case HELMET -> helmetModel != null;
            case CHEST -> chestArmorModel != null;
            case LEGS -> legArmorModel != null;
            case BOOTS -> bootArmorModel != null;
            case CAPE -> capeModel != null;
            case MAIN_HAND -> mainHandModel != null;
            case OFF_HAND -> offHandModel != null;
        };
    }

    public String getEquipmentModel(EquipmentLayerSlot slot) {
        return switch (slot) {
            case HELMET -> helmetModel;
            case CHEST -> chestArmorModel;
            case LEGS -> legArmorModel;
            case BOOTS -> bootArmorModel;
            case CAPE -> capeModel;
            case MAIN_HAND -> mainHandModel;
            case OFF_HAND -> offHandModel;
        };
    }
}
