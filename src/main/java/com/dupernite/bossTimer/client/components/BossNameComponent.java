package com.dupernite.bossTimer.client.components;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

import java.util.Arrays;
import java.util.List;

public class BossNameComponent extends HudComponent {

    private final List<Text> bossNames;
    private int currentBossIndex;
    private int width;
    private long startTime;

    public BossNameComponent(Corner corner, int padding) {
        super(corner, padding);
        this.bossNames = Arrays.asList(
            Text.literal("Kazk").styled(style -> style.withColor(TextColor.fromFormatting(Formatting.DARK_RED))),
            Text.literal("Vespera").styled(style -> style.withColor(TextColor.fromFormatting(Formatting.DARK_RED))),
            Text.literal("Fallen Duo").styled(style -> style.withColor(TextColor.fromFormatting(Formatting.DARK_RED))),
            Text.literal("Kobold Assassin").styled(style -> style.withColor(TextColor.fromFormatting(Formatting.DARK_RED))),
            Text.literal("Dragon Sentinel").styled(style -> style.withColor(TextColor.fromFormatting(Formatting.DARK_RED))),
            Text.literal("Wither/Blaze King").styled(style -> style.withColor(TextColor.fromFormatting(Formatting.DARK_RED)))
        );
        this.currentBossIndex = 0;
    }

    public void nextBoss() {
        currentBossIndex = (currentBossIndex + 1) % bossNames.size();
    }

    public void reset() {
        currentBossIndex = 0;
    }

    public void changePosition() {
        this.corner = Corner.values()[(this.corner.ordinal() + 1) % Corner.values().length];
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
        updateCurrentBoss();
    }

    private void updateCurrentBoss() {
        long elapsedTime = System.currentTimeMillis() - startTime;
        int elapsedCycles = (int) (elapsedTime / (40 * 60 * 1000));
        currentBossIndex = elapsedCycles % bossNames.size();
    }

    @Override
    public void render(DrawContext drawContext, RenderTickCounter renderTickCounter, int x, int y) {
        TextRenderer textRenderer = client.textRenderer;
        Text bossName = bossNames.get(currentBossIndex);
        Text text = Text.literal("Name: ").append(bossName);

        int textWidth = textRenderer.getWidth(text);
        int textHeight = textRenderer.fontHeight;

        int padding = 4;
        int backgroundColor = 0x80000000;
        drawContext.fill(x - padding, y - padding - 1, x + textWidth + padding - 1, y + textHeight + padding - 1, backgroundColor);

        // Draw the text
        drawContext.drawText(textRenderer, text, x, y, 0xFFFFFF, false);
        width = textWidth + 1;
    }

    @Override
    public int getWidth() {
        return width;
    }
}