package com.dupernite.bossTimer.client.screens;

import com.dupernite.bossTimer.client.components.BossNameComponent;
import com.dupernite.bossTimer.client.components.BossTimerComponent;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class BossSelectionScreen extends Screen {

    private final BossNameComponent bossNameComponent;
    private final BossTimerComponent bossTimerComponent;
    private TextFieldWidget spawnTimeField;
    private final Path timestampFilePath;
    private int currentBossIndex;

    public BossSelectionScreen(BossNameComponent bossNameComponent, BossTimerComponent bossTimerComponent, Path timestampFilePath) {
        super(Text.literal("Next Boss Spawn Selection").styled(style -> style.withColor(Formatting.DARK_RED)));
        this.bossNameComponent = bossNameComponent;
        this.bossTimerComponent = bossTimerComponent;
        this.timestampFilePath = timestampFilePath;
        this.currentBossIndex = 0;
    }

    @Override
    protected void init() {
        int width = this.width / 2 - 100;
        int height = this.height / 2 - 50;

        // Boss selection button
        ButtonWidget bossButton = ButtonWidget.builder(bossNameComponent.getBossNames().get(currentBossIndex), button -> {
            currentBossIndex = (currentBossIndex + 1) % bossNameComponent.getBossNames().size();
            button.setMessage(bossNameComponent.getBossNames().get(currentBossIndex));
        }).dimensions(width, height, 200, 20).build();
        this.addDrawableChild(bossButton);

        // Time input field
        spawnTimeField = new TextFieldWidget(this.textRenderer, width, height + 40, 200, 20, Text.literal("Spawn Time (MM:SS)"));
        this.addSelectableChild(spawnTimeField);

        // Submit button
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Submit"), button -> {
            String bossName = bossNameComponent.getBossNames().get(currentBossIndex).getString();
            String[] timeParts = spawnTimeField.getText().split(":");
            long spawnTime = (Integer.parseInt(timeParts[0]) * 60 + Integer.parseInt(timeParts[1])) * 1000L;
            saveTimestamp(bossName, spawnTime);
            bossNameComponent.setCurrentBoss(bossName);
            bossTimerComponent.startTimer(spawnTime);
            this.client.setScreen(null);
        }).dimensions(width, height + 70, 200, 20).build());
    }

    private void saveTimestamp(String bossName, long spawnTime) {
        try {
            String content = bossName + ":" + spawnTime;
            Files.write(timestampFilePath, content.getBytes());
            bossNameComponent.setStartTime(spawnTime);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        this.renderBackground(drawContext, mouseX, mouseY, delta);
        super.render(drawContext, mouseX, mouseY, delta);
        drawContext.drawText(this.textRenderer, this.title, this.width / 2 - this.textRenderer.getWidth(this.title) / 2, 20, 0xFFFFFF, false);
        drawContext.drawText(this.textRenderer, Text.literal("Select Boss:"), this.width / 2 - 100, this.height / 2 - 60, 0xFFFFFF, false);
        drawContext.drawText(this.textRenderer, Text.literal("Spawn Time (MM:SS):"), this.width / 2 - 100, this.height / 2 - 20, 0xFFFFFF, false);
        spawnTimeField.render(drawContext, mouseX, mouseY, delta);
    }
}