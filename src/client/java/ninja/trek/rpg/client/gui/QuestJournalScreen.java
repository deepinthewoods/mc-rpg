package ninja.trek.rpg.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import ninja.trek.rpg.client.state.ClientRpgState;
import ninja.trek.rpg.network.payloads.SyncJournalPayload;

import java.util.ArrayList;
import java.util.List;

public class QuestJournalScreen extends Screen {

    private enum Tab { ACTIVE, AVAILABLE, COMPLETED }

    private Tab currentTab = Tab.ACTIVE;
    private int selectedIndex = -1;
    private int scrollOffset = 0;
    private List<SyncJournalPayload.JournalEntry> filteredEntries = new ArrayList<>();

    public QuestJournalScreen() {
        super(Component.literal("Quest Journal"));
    }

    @Override
    protected void init() {
        super.init();

        int tabWidth = 80;
        int tabY = 15;
        int tabStartX = (width - tabWidth * 3) / 2;

        addRenderableWidget(Button.builder(Component.literal("Active"), b -> switchTab(Tab.ACTIVE))
            .bounds(tabStartX, tabY, tabWidth, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Available"), b -> switchTab(Tab.AVAILABLE))
            .bounds(tabStartX + tabWidth, tabY, tabWidth, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Completed"), b -> switchTab(Tab.COMPLETED))
            .bounds(tabStartX + tabWidth * 2, tabY, tabWidth, 20).build());

        filterEntries();
    }

    private void switchTab(Tab tab) {
        currentTab = tab;
        selectedIndex = -1;
        scrollOffset = 0;
        filterEntries();
    }

    private void filterEntries() {
        List<SyncJournalPayload.JournalEntry> all = ClientRpgState.getInstance().getJournalEntries();
        filteredEntries = new ArrayList<>();

        for (SyncJournalPayload.JournalEntry entry : all) {
            switch (currentTab) {
                case ACTIVE -> {
                    if ("ACTIVE".equals(entry.state())) filteredEntries.add(entry);
                }
                case AVAILABLE -> {
                    if ("AVAILABLE".equals(entry.state())) filteredEntries.add(entry);
                }
                case COMPLETED -> {
                    if ("COMPLETED".equals(entry.state()) || "FAILED".equals(entry.state())) filteredEntries.add(entry);
                }
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        int panelX = 20;
        int panelY = 40;
        int panelW = width - 40;
        int panelH = height - 60;

        // Background
        graphics.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xCC000000);

        // Divider for left/right panels
        int dividerX = panelX + panelW / 3;
        graphics.fill(dividerX, panelY, dividerX + 1, panelY + panelH, 0xFF444444);

        // Left panel - quest list
        int listY = panelY + 5;
        int listMaxH = panelH - 10;
        int entryHeight = 30;

        for (int i = scrollOffset; i < filteredEntries.size(); i++) {
            int y = listY + (i - scrollOffset) * entryHeight;
            if (y + entryHeight > panelY + listMaxH) break;

            SyncJournalPayload.JournalEntry entry = filteredEntries.get(i);

            // Selection highlight
            if (i == selectedIndex) {
                graphics.fill(panelX + 2, y, dividerX - 2, y + entryHeight - 2, 0x44FFFFFF);
            }

            // Quest summary (truncated)
            String displayText = "[L" + entry.level() + "] " + entry.summary();
            if (font.width(displayText) > dividerX - panelX - 20) {
                while (font.width(displayText + "...") > dividerX - panelX - 20 && displayText.length() > 5) {
                    displayText = displayText.substring(0, displayText.length() - 1);
                }
                displayText += "...";
            }
            graphics.drawString(font, Component.literal(displayText), panelX + 5, y + 3, 0xFFCCCCCC);

            // Faction
            graphics.drawString(font, Component.literal(entry.factionId()), panelX + 5, y + 15, 0xFF888888);
        }

        // Right panel - quest details
        if (selectedIndex >= 0 && selectedIndex < filteredEntries.size()) {
            SyncJournalPayload.JournalEntry entry = filteredEntries.get(selectedIndex);
            int detailX = dividerX + 10;
            int detailY = panelY + 10;
            int detailW = panelX + panelW - dividerX - 20;

            graphics.drawString(font, Component.literal(entry.questFullId()), detailX, detailY, 0xFFFFAA00);
            detailY += 15;

            graphics.drawString(font, Component.literal("Level: " + entry.level() + " | Faction: " + entry.factionId()),
                detailX, detailY, 0xFF888888);
            detailY += 15;

            graphics.drawString(font, Component.literal("Location: " + entry.location()), detailX, detailY, 0xFF888888);
            detailY += 15;

            graphics.drawString(font, Component.literal("Status: " + entry.state()), detailX, detailY,
                "ACTIVE".equals(entry.state()) ? 0xFF55FF55 :
                "AVAILABLE".equals(entry.state()) ? 0xFFFFFF55 :
                "COMPLETED".equals(entry.state()) ? 0xFF5555FF :
                0xFFFF5555);
            detailY += 20;

            // Summary with word wrap
            List<String> lines = wrapText(entry.summary(), detailW);
            for (String line : lines) {
                graphics.drawString(font, Component.literal(line), detailX, detailY, 0xFFFFFFFF);
                detailY += 12;
            }

            // Timer info for active quests
            if ("ACTIVE".equals(entry.state()) && entry.timer() > 0) {
                detailY += 5;
                graphics.drawString(font, Component.literal("Auto-resolve timer active"), detailX, detailY, 0xFFFF8800);
            }
        }

        // Tab indicator
        String tabName = currentTab.name();
        graphics.drawString(font, Component.literal("=== " + tabName + " ==="),
            panelX + 5, panelY - 10, 0xFFAAAAAA);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean fromKeyboard) {
        // Check if clicking in the left panel
        double mouseX = event.x();
        double mouseY = event.y();
        int panelX = 20;
        int panelY = 40;
        int panelW = width - 40;
        int dividerX = panelX + panelW / 3;

        if (mouseX >= panelX && mouseX < dividerX && mouseY >= panelY + 5) {
            int relativeY = (int)(mouseY - panelY - 5);
            int index = scrollOffset + relativeY / 30;
            if (index >= 0 && index < filteredEntries.size()) {
                selectedIndex = index;
                return true;
            }
        }

        return super.mouseClicked(event, fromKeyboard);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        scrollOffset = Math.max(0, scrollOffset - (int)scrollY);
        return true;
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
