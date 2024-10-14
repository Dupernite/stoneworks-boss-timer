package com.dupernite.bossTimer.client.components;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public abstract class HudComponent {

    public final MinecraftClient client;
    public Corner corner;
    public final int padding;

    public HudComponent(Corner corner, int padding) {
        this.client = MinecraftClient.getInstance();
        this.corner = corner;
        this.padding = padding;
    }

    public abstract void render(DrawContext drawContext, int x, int y);

    public abstract int getWidth();
}