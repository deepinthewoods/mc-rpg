package ninja.trek.rpg.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import ninja.trek.rpg.entity.data.CharacterAppearance;
import ninja.trek.rpg.entity.data.Race;
import ninja.trek.rpg.entity.NpcEntity;
import ninja.trek.rpg.network.payloads.CreateNpcPayload;
import ninja.trek.rpg.registry.ModEntities;

import net.minecraft.client.input.MouseButtonEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Simplified character creation screen with 3 steps:
 * RACE -> APPEARANCE -> NAME
 * Stripped of D&D 5e CLASS, ABILITY_SCORES steps.
 */
public class CharacterCreationScreen extends Screen {
    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;
    private static final int SPACING = 25;
    private static final int PREVIEW_PANEL_WIDTH = 150;
    private static final int PREVIEW_PANEL_HEIGHT = 230;
    private static final int PREVIEW_MARGIN = 20;
    private static final int PREVIEW_BACKGROUND_COLOR = 0xAA111111;
    private static final int PREVIEW_BORDER_COLOR = 0xFFFFFFFF;
    private static final Component PREVIEW_TITLE = Component.literal("Character Preview");

    private enum CreationStep {
        RACE,
        APPEARANCE,
        NAME
    }

    private CreationStep currentStep = CreationStep.RACE;

    // Character data being built
    private Race selectedRace = Race.HUMAN;
    private int bodyIndex = 0;
    private int legsIndex = 0;
    private int armsIndex = 0;
    private int headIndex = 0;
    private String characterName = "";
    private String currentTitle = "";

    // UI widgets
    private EditBox nameField;
    private final List<Button> stepButtons = new ArrayList<>();
    private NpcEntity previewEntity;
    private boolean previewDirty = true;

    // Preview interaction state
    private boolean isDraggingPreview = false;
    private float previewYaw = 0.0f;
    private float previewPitch = 0.0f;
    private float previewZoom = 1.0f;

    public CharacterCreationScreen() {
        super(Component.literal("Character Creation"));
    }

    @Override
    protected void init() {
        super.init();
        stepButtons.clear();

        int centerX = this.width / 2;
        int startY = this.height / 4;

        switch (currentStep) {
            case RACE -> initRaceStep(centerX, startY);
            case APPEARANCE -> initAppearanceStep(centerX, startY);
            case NAME -> initNameStep(centerX, startY);
        }

        addNavigationButtons(centerX, this.height - 40);
    }

    private void initRaceStep(int centerX, int startY) {
        currentTitle = "Choose Race";

        int y = startY;
        for (Race race : Race.values()) {
            Button button = Button.builder(
                Component.literal(capitalize(race.getName())),
                btn -> {
                    selectedRace = race;
                    bodyIndex = race.getDefaultBodyIndex();
                    legsIndex = race.getDefaultLegsIndex();
                    armsIndex = race.getDefaultArmsIndex();
                    headIndex = race.getDefaultHeadIndex();
                    markPreviewDirty();
                    nextStep();
                }
            )
            .bounds(centerX - BUTTON_WIDTH / 2, y, BUTTON_WIDTH, BUTTON_HEIGHT)
            .build();

            stepButtons.add(button);
            addRenderableWidget(button);
            y += SPACING;
        }
    }

    private void initAppearanceStep(int centerX, int startY) {
        currentTitle = "Customize Appearance";

        String[] partNames = {"Body", "Legs", "Arms", "Head"};

        int y = startY;
        for (int i = 0; i < 4; i++) {
            final int partIndex = i;

            Button decreaseBtn = Button.builder(
                Component.literal("<"),
                btn -> decreaseAppearance(partIndex)
            )
            .bounds(centerX - 120, y, 20, BUTTON_HEIGHT)
            .build();

            Button increaseBtn = Button.builder(
                Component.literal(">"),
                btn -> increaseAppearance(partIndex)
            )
            .bounds(centerX + 100, y, 20, BUTTON_HEIGHT)
            .build();

            stepButtons.add(decreaseBtn);
            stepButtons.add(increaseBtn);
            addRenderableWidget(decreaseBtn);
            addRenderableWidget(increaseBtn);

            y += SPACING;
        }
    }

    private void initNameStep(int centerX, int startY) {
        currentTitle = "Name Your Character";

        nameField = new EditBox(
            this.font,
            centerX - BUTTON_WIDTH / 2,
            startY,
            BUTTON_WIDTH,
            BUTTON_HEIGHT,
            Component.literal("Character Name")
        );
        nameField.setMaxLength(32);
        nameField.setValue(characterName);
        nameField.setResponder(text -> characterName = text);

        addRenderableWidget(nameField);
        setInitialFocus(nameField);

        // Create button
        Button createButton = Button.builder(
            Component.literal("Create NPC"),
            btn -> createNpc()
        )
        .bounds(centerX - BUTTON_WIDTH / 2, startY + 40, BUTTON_WIDTH, BUTTON_HEIGHT)
        .build();

        stepButtons.add(createButton);
        addRenderableWidget(createButton);
    }

    private void addNavigationButtons(int centerX, int y) {
        if (currentStep != CreationStep.RACE) {
            Button backButton = Button.builder(
                Component.literal("Back"),
                btn -> previousStep()
            )
            .bounds(centerX - BUTTON_WIDTH - 10, y, BUTTON_WIDTH / 2, BUTTON_HEIGHT)
            .build();

            addRenderableWidget(backButton);
        }

        if (currentStep == CreationStep.APPEARANCE) {
            Button nextButton = Button.builder(
                Component.literal("Next"),
                btn -> nextStep()
            )
            .bounds(centerX + 10, y, BUTTON_WIDTH / 2, BUTTON_HEIGHT)
            .build();

            addRenderableWidget(nextButton);
        }

        Button cancelButton = Button.builder(
            Component.literal("Cancel"),
            btn -> onClose()
        )
        .bounds(10, this.height - 30, 80, BUTTON_HEIGHT)
        .build();

        addRenderableWidget(cancelButton);
    }

    private void decreaseAppearance(int partIndex) {
        switch (partIndex) {
            case 0 -> bodyIndex = Math.max(0, bodyIndex - 1);
            case 1 -> legsIndex = Math.max(0, legsIndex - 1);
            case 2 -> armsIndex = Math.max(0, armsIndex - 1);
            case 3 -> headIndex = Math.max(0, headIndex - 1);
        }
        rebuildWidgets();
        markPreviewDirty();
    }

    private void increaseAppearance(int partIndex) {
        switch (partIndex) {
            case 0 -> bodyIndex++;
            case 1 -> legsIndex++;
            case 2 -> armsIndex++;
            case 3 -> headIndex++;
        }
        rebuildWidgets();
        markPreviewDirty();
    }

    private void nextStep() {
        currentStep = CreationStep.values()[Math.min(currentStep.ordinal() + 1, CreationStep.values().length - 1)];
        rebuildWidgets();
    }

    private void previousStep() {
        currentStep = CreationStep.values()[Math.max(currentStep.ordinal() - 1, 0)];
        rebuildWidgets();
    }

    private void createNpc() {
        if (characterName.isEmpty()) {
            return;
        }

        CharacterAppearance appearance = new CharacterAppearance(
            bodyIndex, legsIndex, armsIndex, headIndex
        );

        CreateNpcPayload payload = new CreateNpcPayload(
            characterName,
            selectedRace,
            appearance
        );

        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(payload);
        onClose();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        int centerX = this.width / 2;
        int startY = this.height / 4;

        // Render title
        if (!currentTitle.isEmpty()) {
            guiGraphics.drawCenteredString(this.font, currentTitle, centerX, 40, 0xFFFFFFFF);
        }

        // Render step-specific content
        switch (currentStep) {
            case RACE -> renderRaceInfo(guiGraphics, centerX, startY);
            case APPEARANCE -> renderAppearanceInfo(guiGraphics, centerX, startY);
            case NAME -> renderNameInfo(guiGraphics, centerX, startY);
        }

        renderCharacterPreview(guiGraphics, mouseX, mouseY);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void renderCharacterPreview(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (this.minecraft == null) return;

        ensurePreviewEntity();
        if (previewEntity == null) return;

        if (previewDirty) {
            updatePreviewEntityState();
        }

        previewEntity.tick();
        previewEntity.tickCount++;

        int panelWidth = PREVIEW_PANEL_WIDTH;
        int availableHeight = Math.max(140, this.height - PREVIEW_MARGIN * 2);
        int panelHeight = Math.min(PREVIEW_PANEL_HEIGHT, availableHeight);
        int left = Math.max(PREVIEW_MARGIN, this.width - panelWidth - PREVIEW_MARGIN);
        int top = (this.height - panelHeight) / 2;

        guiGraphics.fill(left, top, left + panelWidth, top + panelHeight, PREVIEW_BACKGROUND_COLOR);
        drawPanelBorder(guiGraphics, left, top, panelWidth, panelHeight, PREVIEW_BORDER_COLOR);
        guiGraphics.drawString(this.font, PREVIEW_TITLE, left + 8, top + 8, 0xFFFFFFFF);

        int titleHeight = 20;
        int baseRenderSize = (int) (panelHeight * 0.32f);
        int renderSize = (int) (baseRenderSize * previewZoom);

        drawPreviewEntity(guiGraphics, left, top + titleHeight, panelWidth, panelHeight - titleHeight,
                         renderSize, previewYaw, previewPitch);
    }

    private void drawPreviewEntity(GuiGraphics guiGraphics, int panelLeft, int panelTop, int panelWidth, int panelHeight,
                                   int size, float yaw, float pitch) {
        if (previewEntity == null) return;

        int x1 = panelLeft;
        int y1 = panelTop;
        int x2 = panelLeft + panelWidth;
        int y2 = panelTop + panelHeight;

        guiGraphics.enableScissor(x1, y1, x2, y2);

        float centerX = (x1 + x2) / 2.0f;
        float centerY = (y1 + y2) / 2.0f;

        // Use yaw/pitch as simulated mouse offset from center
        float mouseX = centerX + yaw;
        float mouseY = centerY + pitch;

        InventoryScreen.renderEntityInInventoryFollowsMouse(
            guiGraphics,
            x1, y1, x2, y2,
            size,
            0.0625f,
            mouseX,
            mouseY,
            previewEntity
        );

        guiGraphics.disableScissor();
    }

    private void drawPanelBorder(GuiGraphics guiGraphics, int left, int top, int width, int height, int color) {
        guiGraphics.fill(left, top, left + width, top + 1, color);
        guiGraphics.fill(left, top + height - 1, left + width, top + height, color);
        guiGraphics.fill(left, top, left + 1, top + height, color);
        guiGraphics.fill(left + width - 1, top, left + width, top + height, color);
    }

    private void ensurePreviewEntity() {
        if (this.minecraft == null) return;

        ClientLevel world = this.minecraft.level;
        if (world == null) {
            previewEntity = null;
            return;
        }

        if (previewEntity != null && previewEntity.level() != world) {
            previewEntity = null;
            previewDirty = true;
        }

        if (previewEntity == null) {
            previewEntity = new NpcEntity(ModEntities.NPC, world);
            previewEntity.setNoAi(true);
            previewEntity.setSilent(true);
            previewEntity.setNoGravity(true);
            previewEntity.snapTo(0.0, world.getMinY(), 0.0, 180.0f, 0.0f);
            previewEntity.yBodyRot = 180.0f;
            previewEntity.yHeadRot = 180.0f;
            previewEntity.setYRot(180.0f);
            previewEntity.setXRot(0.0f);
            previewEntity.tickCount = 1;
            previewDirty = true;
        }
    }

    private void updatePreviewEntityState() {
        if (previewEntity == null) return;

        CharacterAppearance appearance = new CharacterAppearance(bodyIndex, legsIndex, armsIndex, headIndex);
        previewEntity.initializeNpc(selectedRace, appearance);
        previewEntity.yBodyRot = 180.0f;
        previewEntity.setYRot(180.0f);
        previewEntity.yHeadRot = 180.0f;
        previewEntity.setXRot(0.0f);
        previewDirty = false;
    }

    private void markPreviewDirty() {
        previewDirty = true;
    }

    private void renderRaceInfo(GuiGraphics guiGraphics, int centerX, int startY) {
        int y = startY + SPACING * Race.values().length + 10;
        guiGraphics.drawCenteredString(this.font, "Select a race for your NPC", centerX, y, 0xFFFFFFFF);
    }

    private void renderAppearanceInfo(GuiGraphics guiGraphics, int centerX, int startY) {
        String[] partNames = {"Body", "Legs", "Arms", "Head"};
        int[] indices = {bodyIndex, legsIndex, armsIndex, headIndex};

        int y = startY;
        for (int i = 0; i < 4; i++) {
            String text = partNames[i] + ": " + indices[i];
            guiGraphics.drawCenteredString(this.font, text, centerX, y + 5, 0xFFFFFFFF);
            y += SPACING;
        }
    }

    private void renderNameInfo(GuiGraphics guiGraphics, int centerX, int startY) {
        guiGraphics.drawCenteredString(this.font, "Enter a name for your NPC", centerX, startY - 30, 0xFFFFFFFF);
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean forwarded) {
        if (event.button() == 0 && isMouseOverPreview(event.x(), event.y())) {
            isDraggingPreview = true;
            return true;
        }
        return super.mouseClicked(event, forwarded);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == 0 && isDraggingPreview) {
            isDraggingPreview = false;
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        if (isDraggingPreview && event.button() == 0) {
            previewYaw += (float) deltaX * 0.5f;
            previewPitch += (float) deltaY * 0.5f;
            previewPitch = Math.max(-45.0f, Math.min(45.0f, previewPitch));
            return true;
        }
        return super.mouseDragged(event, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (isMouseOverPreview(mouseX, mouseY)) {
            previewZoom += (float) verticalAmount * 0.1f;
            previewZoom = Math.max(0.5f, Math.min(2.0f, previewZoom));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    private boolean isMouseOverPreview(double mouseX, double mouseY) {
        int panelWidth = PREVIEW_PANEL_WIDTH;
        int availableHeight = Math.max(140, this.height - PREVIEW_MARGIN * 2);
        int panelHeight = Math.min(PREVIEW_PANEL_HEIGHT, availableHeight);
        int left = Math.max(PREVIEW_MARGIN, this.width - panelWidth - PREVIEW_MARGIN);
        int top = (this.height - panelHeight) / 2;

        return mouseX >= left && mouseX <= left + panelWidth &&
               mouseY >= top && mouseY <= top + panelHeight;
    }

    @Override
    public void removed() {
        super.removed();
        previewEntity = null;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
