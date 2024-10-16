package com.dupernite.bossTimer.client.components;

import com.dupernite.bossTimer.client.BossTimerClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class BossTimerComponent extends HudComponent {

    private static final int TIMER_DURATION = 40 * 60 * 1000;
    public long endTime;
    private int width;
    public boolean timerStarted;
    private final BossNameComponent bossNameComponent;
    private final BossTimerClient bossTimerClient;

    public BossTimerComponent(Corner corner, int padding, BossNameComponent bossNameComponent, BossTimerClient bossTimerClient) {
        super(corner, padding);
        this.timerStarted = false;
        this.bossNameComponent = bossNameComponent;
        this.bossTimerClient = bossTimerClient;
    }

    public void startTimer(long spawnTime) {
        this.endTime = System.currentTimeMillis() + spawnTime;
        this.timerStarted = true;
    }

    public void reset() {
        this.endTime = System.currentTimeMillis() + TIMER_DURATION;
        this.timerStarted = true;
        bossNameComponent.reset();
    }

    public void restartTimer() {
        this.endTime = System.currentTimeMillis() + TIMER_DURATION;
        this.timerStarted = true;
    }

    public void changePosition() {
        this.corner = Corner.values()[(this.corner.ordinal() + 1) % Corner.values().length];
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
        this.timerStarted = true;
    }

    public void calculateRemainingTime() {
        long currentTime = System.currentTimeMillis();
        long remainingTime = endTime - currentTime;

        if (remainingTime <= 0) {
            bossNameComponent.nextBoss();
            startTimer(TIMER_DURATION);
            bossTimerClient.saveTimestamp();
        } else {
            startTimer(remainingTime);
        }
    }

    @Override
    public void render(DrawContext drawContext, int x, int y) {
        TextRenderer textRenderer = client.textRenderer;
        Text text;

        if (!timerStarted) {
            text = Text.literal("Press the setup key to setup the timer").styled(style -> style.withColor(Formatting.RED));
        } else {
            long currentTime = System.currentTimeMillis();
            long remainingTime = endTime - currentTime;

            if (remainingTime < 0) {
                remainingTime = 0;
                bossNameComponent.nextBoss();
                startTimer(TIMER_DURATION);
                bossTimerClient.saveTimestamp();
            }

            int minutes = (int) (remainingTime / 60000);
            int seconds = (int) ((remainingTime / 1000) % 60);
            String timeString = String.format("Boss Timer: %02d:%02d", minutes, seconds);
            text = Text.of(timeString);
        }

        int textWidth = textRenderer.getWidth(text);
        int textHeight = textRenderer.fontHeight;

        int padding = 4;
        int backgroundColor = 0x80000000;
        drawContext.fill(x - padding, y - padding - 1, x + textWidth + padding - 1, y + textHeight + padding - 1, backgroundColor);

        drawContext.drawText(textRenderer, text, x, y, 0xFFFFFF, false);
        width = textWidth + 1;
    }

    @Override
    public int getWidth() {
        return width;
    }
}