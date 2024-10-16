package com.dupernite.bossTimer.client.screens;

import com.dupernite.bossTimer.client.BossTimerClient;
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
import java.util.Properties;

public class BossSelectionScreen extends Screen {

    private final BossNameComponent bossNameComponent;
    private final BossTimerComponent bossTimerComponent;
    private TextFieldWidget spawnTimeField;
    private final Path timestampFilePath;
    private final BossTimerClient bossTimerClient;
    private int currentBossIndex;
    private Text errorMessage;

    public BossSelectionScreen(BossNameComponent bossNameComponent, BossTimerComponent bossTimerComponent, Path timestampFilePath, BossTimerClient bossTimerClient) {
        super(Text.literal("Next Boss Spawn Selection").styled(style -> style.withColor(Formatting.DARK_RED)));
        this.bossNameComponent = bossNameComponent;
        this.bossTimerComponent = bossTimerComponent;
        this.timestampFilePath = timestampFilePath;
        this.bossTimerClient = bossTimerClient;
        this.currentBossIndex = 0;
        this.errorMessage = Text.literal("");
    }

    @Override
    protected void init() {
        int width = this.width / 2 - 100;
        int height = this.height / 2 - 50;

        ButtonWidget bossButton = ButtonWidget.builder(bossNameComponent.getBossNames().get(currentBossIndex), button -> {
            currentBossIndex = (currentBossIndex + 1) % bossNameComponent.getBossNames().size();
            button.setMessage(bossNameComponent.getBossNames().get(currentBossIndex));
        }).dimensions(width, height, 200, 20).build();
        this.addDrawableChild(bossButton);

        spawnTimeField = new TextFieldWidget(this.textRenderer, width, height + 40, 200, 20, Text.literal("Spawn Time (MM:SS)"));
        this.addSelectableChild(spawnTimeField);

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Submit"), button -> {
            String bossName = bossNameComponent.getBossNames().get(currentBossIndex).getString();
            String input = spawnTimeField.getText();
            if (isValidTimeFormat(input)) {
                String[] timeParts = input.split(":");
                long spawnTime = (Integer.parseInt(timeParts[0]) * 60 + Integer.parseInt(timeParts[1])) * 1000L;
                saveTimestamp(bossName, spawnTime);
                bossNameComponent.setCurrentBoss(bossName);
                bossTimerComponent.startTimer(spawnTime);
                this.client.setScreen(null);
            } else {
                errorMessage = Text.literal("Invalid time format! Please use MM:SS. (example: 30:00)").styled(style -> style.withColor(Formatting.RED));
            }
        }).dimensions(width, height + 70, 200, 20).build());
    }

    private boolean isValidTimeFormat(String input) {
        if (input.matches("\\d{1,2}:\\d{2}")) {
            String[] parts = input.split(":");
            int minutes = Integer.parseInt(parts[0]);
            int seconds = Integer.parseInt(parts[1]);
            return seconds < 60;
        }
        return false;
    }

    private void saveTimestamp(String bossName, long spawnTime) {
        long endTime = System.currentTimeMillis() + spawnTime;
        bossTimerComponent.setEndTime(endTime);
        bossTimerClient.saveTimestamp(bossName, endTime);
        bossNameComponent.setStartTime(endTime);
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        this.renderBackground(drawContext);
        super.render(drawContext, mouseX, mouseY, delta);
        drawContext.drawText(this.textRenderer, this.title, this.width / 2 - this.textRenderer.getWidth(this.title) / 2, 20, 0xFFFFFF, false);
        drawContext.drawText(this.textRenderer, Text.literal("Select Boss:"), this.width / 2 - 100, this.height / 2 - 60, 0xFFFFFF, false);
        drawContext.drawText(this.textRenderer, Text.literal("Spawn Time (MM:SS):"), this.width / 2 - 100, this.height / 2 - 20, 0xFFFFFF, false);
        spawnTimeField.render(drawContext, mouseX, mouseY, delta);
        if (!errorMessage.getString().isEmpty()) {
            drawContext.drawText(this.textRenderer, errorMessage, this.width / 2 - this.textRenderer.getWidth(errorMessage) / 2, this.height / 2 + 100, 0xFFFFFF, false);
        }
    }
}