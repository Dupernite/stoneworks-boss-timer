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
import java.util.Properties;

public class BossTimerClient implements ClientModInitializer {

    private BossTimerComponent bossTimerComponent;
    private BossNameComponent bossNameComponent;
    private boolean hudVisible = true;
    private static final String CONFIG_FILE = "boss_timer_config.txt";
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
        bossTimerComponent = new BossTimerComponent(Corner.TOP_LEFT, 10, bossNameComponent, this);
        List<HudComponent> hudComponents = new ArrayList<>();
        hudComponents.add(bossTimerComponent);
        hudComponents.add(bossNameComponent);

        hudRenderer120 = new HudRenderer(hudComponents, hudVisible);
        HudRenderCallback.EVENT.register(hudRenderer120);

        loadConfig();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world != null && !bossTimerComponent.timerStarted) {
                loadTimestamp();
            }

            while (toggleHudKey.wasPressed()) {
                hudVisible = !hudVisible;
                hudRenderer120.setHudVisible(hudVisible);
            }

            while (setupTimerKey.wasPressed()) {
                client.setScreen(new BossSelectionScreen(bossNameComponent, bossTimerComponent, FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE)));
            }

            while (restartTimerKey.wasPressed()) {
                if (bossTimerComponent.timerStarted) {
                    bossTimerComponent.restartTimer();
                    saveTimestamp();
                }
            }

            while (changePositionKey.wasPressed()) {
                bossTimerComponent.changePosition();
                bossNameComponent.changePosition();
                saveConfig();
            }
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> saveTimestamp());
    }

    public void loadTimestamp() {
        try {
            Path path = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE);
            if (Files.exists(path)) {
                Properties properties = new Properties();
                properties.load(Files.newInputStream(path));
                String bossName = properties.getProperty("bossName");
                long endTime = Long.parseLong(properties.getProperty("endTime"));
                bossNameComponent.setCurrentBoss(bossName);
                bossTimerComponent.setEndTime(endTime);
                bossTimerComponent.calculateRemainingTime();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveTimestamp() {
        try {
            Path path = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE);
            Properties properties = new Properties();
            properties.setProperty("bossName", bossNameComponent.getBossNames().get(bossNameComponent.currentBossIndex).getString());
            properties.setProperty("endTime", Long.toString(bossTimerComponent.endTime));
            properties.store(Files.newOutputStream(path), null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadConfig() {
        try {
            Path path = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE);
            if (Files.exists(path)) {
                Properties properties = new Properties();
                properties.load(Files.newInputStream(path));
                bossTimerComponent.corner = Corner.valueOf(properties.getProperty("timerPosition", "TOP_LEFT"));
                bossNameComponent.corner = Corner.valueOf(properties.getProperty("namePosition", "TOP_LEFT"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveConfig() {
        try {
            Path path = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE);
            Properties properties = new Properties();
            properties.setProperty("timerPosition", bossTimerComponent.corner.name());
            properties.setProperty("namePosition", bossNameComponent.corner.name());
            properties.store(Files.newOutputStream(path), null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}