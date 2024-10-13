package com.dupernite.bossTimer.client;

import com.dupernite.bossTimer.client.components.BossNameComponent;
import com.dupernite.bossTimer.client.components.BossTimerComponent;
import com.dupernite.bossTimer.client.components.Corner;
import com.dupernite.bossTimer.client.components.HudComponent;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class BossTimerClient implements ClientModInitializer {

    private BossTimerComponent bossTimerComponent;
    private BossNameComponent bossNameComponent;
    private boolean hudVisible = true;
    private static final String TIMESTAMP_FILE = "boss_timer_timestamp.txt";
    private HudRenderer hudRenderer;

    private static final KeyBinding toggleHudKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
        "key.bossTimer.toggleHud",
        InputUtil.Type.KEYSYM,
        GLFW.GLFW_KEY_H,
        "category.bossTimer"
    ));

    private static final KeyBinding restartTimerKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
        "key.bossTimer.restartTimer",
        InputUtil.Type.KEYSYM,
        GLFW.GLFW_KEY_R,
        "category.bossTimer"
    ));

    private static final KeyBinding changePositionKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
        "key.bossTimer.changePosition",
        InputUtil.Type.KEYSYM,
        GLFW.GLFW_KEY_P,
        "category.bossTimer"
    ));

    @Override
    public void onInitializeClient() {
        bossNameComponent = new BossNameComponent(Corner.TOP_LEFT, 10);
        bossTimerComponent = new BossTimerComponent(Corner.TOP_LEFT, 10, bossNameComponent);
        List<HudComponent> hudComponents = new ArrayList<>();
        hudComponents.add(bossTimerComponent);
        hudComponents.add(bossNameComponent);

        hudRenderer = new HudRenderer(hudComponents, hudVisible);
        HudRenderCallback.EVENT.register(hudRenderer);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world != null && !bossTimerComponent.timerStarted) {
                loadTimestamp();
                bossTimerComponent.startTimer();
            }

            while (toggleHudKey.wasPressed()) {
                hudVisible = !hudVisible;
                hudRenderer.setHudVisible(hudVisible);
            }

            while (restartTimerKey.wasPressed()) {
                saveTimestamp();
                bossNameComponent.reset();
                bossTimerComponent.reset();
            }

            while (changePositionKey.wasPressed()) {
                bossTimerComponent.changePosition();
                bossNameComponent.changePosition();
            }
        });
    }

    private void saveTimestamp() {
        try {
            long timestamp = System.currentTimeMillis();
            Path path = FabricLoader.getInstance().getConfigDir().resolve(TIMESTAMP_FILE);
            Files.write(path, String.valueOf(timestamp).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadTimestamp() {
        try {
            Path path = FabricLoader.getInstance().getConfigDir().resolve(TIMESTAMP_FILE);
            if (Files.exists(path)) {
                long timestamp = Long.parseLong(new String(Files.readAllBytes(path)));
                bossNameComponent.setStartTime(timestamp);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}