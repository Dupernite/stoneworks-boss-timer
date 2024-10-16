package com.dupernite.bossTimer.client;

import com.dupernite.bossTimer.client.components.HudComponent;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

import java.util.List;

public class HudRenderer implements HudRenderCallback {

    private final List<HudComponent> hudComponents;
    private boolean hudVisible;

    public HudRenderer(List<HudComponent> hudComponents, boolean hudVisible) {
        this.hudComponents = hudComponents;
        this.hudVisible = hudVisible;
    }

    public void setHudVisible(boolean hudVisible) {
        this.hudVisible = hudVisible;
    }

    @Override
    public void onHudRender(DrawContext drawContext, float v) {
        if (!hudVisible) {
            return;
        }

        int screenWidth = drawContext.getScaledWindowWidth();

        int topLeftY = 0;
        int topRightY = 0;

        for (HudComponent component : hudComponents) {
            int x = component.padding;
            int y = component.padding;

            switch (component.corner) {
                case TOP_LEFT:
                    y = topLeftY + component.padding;
                    topLeftY = y + component.padding;
                    break;
                case TOP_RIGHT:
                    x = screenWidth - component.padding - component.getWidth();
                    y = topRightY + component.padding;
                    topRightY = y + component.padding;
                    break;
                default:
                    break;
            }

            component.render(drawContext, x, y);
        }
    }
}