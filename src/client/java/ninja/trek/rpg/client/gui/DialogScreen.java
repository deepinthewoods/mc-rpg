package ninja.trek.rpg.client.gui;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import ninja.trek.rpg.network.payloads.DialogResponsePayload;

import java.util.ArrayList;
import java.util.List;

public class DialogScreen extends Screen {

    private String speakerName;
    private String fullText;
    private String displayedText = "";
    private int charIndex = 0;
    private int tickCount = 0;
    private boolean textComplete = false;
    private List<String> responses;
    private final String questFullId;
    private final List<Button> responseButtons = new ArrayList<>();

    public DialogScreen(String speakerName, String text, List<String> responses, String questFullId) {
        super(Component.literal("Dialog"));
        this.speakerName = speakerName;
        this.fullText = text;
        this.responses = responses;
        this.questFullId = questFullId;
    }

    @Override
    protected void init() {
        super.init();
        rebuildResponseButtons();
    }

    private void rebuildResponseButtons() {
        responseButtons.forEach(this::removeWidget);
        responseButtons.clear();

        if (!textComplete) return;

        int buttonWidth = Math.min(300, width - 40);
        int startY = height - 30 - (responses.size() * 25);

        for (int i = 0; i < responses.size(); i++) {
            final int index = i;
            Button btn = Button.builder(
                Component.literal(responses.get(i)),
                b -> onResponseClicked(index)
            ).bounds((width - buttonWidth) / 2, startY + i * 25, buttonWidth, 20).build();
            responseButtons.add(btn);
            addRenderableWidget(btn);
        }
    }

    @Override
    public void tick() {
        super.tick();
        tickCount++;

        if (!textComplete && tickCount % 1 == 0) {
            if (charIndex < fullText.length()) {
                charIndex++;
                displayedText = fullText.substring(0, charIndex);
            } else {
                textComplete = true;
                rebuildResponseButtons();
            }
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean fromKeyboard) {
        if (!textComplete) {
            textComplete = true;
            charIndex = fullText.length();
            displayedText = fullText;
            rebuildResponseButtons();
            return true;
        }
        return super.mouseClicked(event, fromKeyboard);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        // Dark background panel
        int panelX = 20;
        int panelY = 20;
        int panelW = width - 40;
        int panelH = height - 40;
        graphics.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xCC000000);

        // Speaker name
        graphics.drawString(font, Component.literal("[" + speakerName + "]"), panelX + 10, panelY + 10, 0xFFFFAA00);

        // Dialog text with word wrapping
        int textX = panelX + 10;
        int textY = panelY + 25;
        int maxWidth = panelW - 20;

        List<String> lines = wrapText(displayedText, maxWidth);
        for (String line : lines) {
            graphics.drawString(font, Component.literal(line), textX, textY, 0xFFFFFFFF);
            textY += 12;
        }

        // Click to continue hint
        if (!textComplete) {
            graphics.drawString(font, Component.literal("(Click to reveal all)"),
                panelX + panelW - 130, panelY + panelH - 15, 0xFF888888);
        }
    }

    private void onResponseClicked(int index) {
        ClientPlayNetworking.send(new DialogResponsePayload(index));
    }

    public void updateDialog(String speakerName, String text, List<String> responses) {
        this.speakerName = speakerName;
        this.fullText = text;
        this.displayedText = "";
        this.charIndex = 0;
        this.tickCount = 0;
        this.textComplete = false;
        this.responses = responses;
        rebuildResponseButtons();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private List<String> wrapText(String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder current = new StringBuilder();

        for (String word : words) {
            String test = current.isEmpty() ? word : current + " " + word;
            if (font.width(test) > maxWidth) {
                if (!current.isEmpty()) {
                    lines.add(current.toString());
                    current = new StringBuilder(word);
                } else {
                    lines.add(word);
                }
            } else {
                current = new StringBuilder(test);
            }
        }
        if (!current.isEmpty()) {
            lines.add(current.toString());
        }

        return lines;
    }
}
