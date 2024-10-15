package com.dupernite.bossTimer.client;

import com.dupernite.bossTimer.client.components.BossNameComponent;
import com.dupernite.bossTimer.client.components.BossTimerComponent;
import com.dupernite.bossTimer.client.components.Corner;
import com.dupernite.bossTimer.client.components.HudComponent;
import com.dupernite.bossTimer.client.screens.BossSelectionScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
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
    private HudRenderer hudRenderer120;

    private static final KeyBinding toggleHudKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
        "key.bossTimer.toggleHud",
        InputUtil.Type.KEYSYM,
        GLFW.GLFW_KEY_H,
        "category.bossTimer"
    ));

    private static final KeyBinding setupTimerKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
        "key.bossTimer.setupTimer",
        InputUtil.Type.KEYSYM,
        GLFW.GLFW_KEY_R,
        "category.bossTimer"
    ));

    private static final KeyBinding restartTimerKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
        "key.bossTimer.restartTimer",
        InputUtil.Type.KEYSYM,
        GLFW.GLFW_KEY_T,
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

        hudRenderer120 = new HudRenderer(hudComponents, hudVisible);
        HudRenderCallback.EVENT.register(hudRenderer120);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world != null && !bossTimerComponent.timerStarted) {
                loadTimestamp();
            }

            while (toggleHudKey.wasPressed()) {
                hudVisible = !hudVisible;
                hudRenderer120.setHudVisible(hudVisible);
            }

            while (setupTimerKey.wasPressed()) {
                client.setScreen(new BossSelectionScreen(bossNameComponent, bossTimerComponent, FabricLoader.getInstance().getConfigDir().resolve(TIMESTAMP_FILE)));
            }

            while (restartTimerKey.wasPressed()) {
                bossTimerComponent.restartTimer();
                saveTimestamp();
            }

            while (changePositionKey.wasPressed()) {
                bossTimerComponent.changePosition();
                bossNameComponent.changePosition();
            }
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> saveTimestamp());
    }

    private void loadTimestamp() {
        try {
            Path path = FabricLoader.getInstance().getConfigDir().resolve(TIMESTAMP_FILE);
            if (Files.exists(path)) {
                String content = new String(Files.readAllBytes(path));
                String[] parts = content.split(":");
                if (parts.length == 2) {
                    String bossName = parts[0];
                    long timestamp = Long.parseLong(parts[1]);
                    bossNameComponent.setCurrentBoss(bossName);
                    bossNameComponent.setStartTime(timestamp);
                    bossTimerComponent.startTimer(timestamp - System.currentTimeMillis());
                } else {
                    System.err.println("Invalid timestamp file format");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveTimestamp() {
        try {
            Path path = FabricLoader.getInstance().getConfigDir().resolve(TIMESTAMP_FILE);
            String bossName = bossNameComponent.getBossNames().get(bossNameComponent.currentBossIndex).getString();
            long timestamp = bossTimerComponent.endTime;
            String content = bossName + ":" + timestamp;
            Files.write(path, content.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}