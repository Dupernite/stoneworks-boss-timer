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
    private static final String TIMESTAMP_FILE = "boss_timer_timestamp.txt";
    private HudRenderer hudRenderer120;

    private static final KeyBinding toggleHudKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
        "key.bossTimer.toggleHud",
        InputUtil.Type.KEYSYM,
        GLFW.GLFW_KEY_Z,
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
        GLFW.GLFW_KEY_X,
        "category.bossTimer"
    ));

    private static final KeyBinding changePositionKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
        "key.bossTimer.changePosition",
        InputUtil.Type.KEYSYM,
        GLFW.GLFW_KEY_C,
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

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world != null && !bossTimerComponent.timerStarted) {
                loadTimestamp();
            }

            while (toggleHudKey.wasPressed()) {
                hudVisible = !hudVisible;
                hudRenderer120.setHudVisible(hudVisible);
            }

            while (setupTimerKey.wasPressed()) {
                client.setScreen(new BossSelectionScreen(bossNameComponent, bossTimerComponent, FabricLoader.getInstance().getConfigDir().resolve(TIMESTAMP_FILE), this));
            }

            while (restartTimerKey.wasPressed()) {
                if (bossTimerComponent.timerStarted) {
                    bossTimerComponent.restartTimer();
                    saveTimestamp(bossNameComponent.getBossNames().get(bossNameComponent.currentBossIndex).getString(), bossTimerComponent.endTime);
                }
            }

            while (changePositionKey.wasPressed()) {
                bossTimerComponent.changePosition();
                bossNameComponent.changePosition();
                saveConfig();
            }
        });

        ClientTickEvents.START_WORLD_TICK.register(world -> checkAndCreateConfig());

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> saveTimestamp(bossNameComponent.getBossNames().get(bossNameComponent.currentBossIndex).getString(), bossTimerComponent.endTime));
    }

    private void checkAndCreateConfig() {
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE);
        if (!Files.exists(configPath)) {
            try {
                Files.createDirectories(configPath.getParent());
                Files.createFile(configPath);
                Properties properties = new Properties();
                properties.setProperty("timerPosition", Corner.TOP_LEFT.name());
                properties.setProperty("namePosition", Corner.TOP_LEFT.name());
                properties.setProperty("hudVisible", Boolean.toString(true));
                properties.setProperty("timerSetup", Boolean.toString(false));
                properties.store(Files.newOutputStream(configPath), null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void loadTimestamp() {
        try {
            Path timestampPath = FabricLoader.getInstance().getConfigDir().resolve(TIMESTAMP_FILE);
            if (Files.exists(timestampPath)) {
                Properties properties = new Properties();
                properties.load(Files.newInputStream(timestampPath));
                String bossName = properties.getProperty("bossName");
                String endTimeStr = properties.getProperty("endTime");

                if (bossName != null && endTimeStr != null) {
                    long endTime = Long.parseLong(endTimeStr);
                    long currentTime = System.currentTimeMillis();
                    long elapsedTime = currentTime - endTime;

                    if (elapsedTime < 0) {
                        // Timer is still running
                        bossNameComponent.setCurrentBoss(bossName);
                        bossTimerComponent.setEndTime(endTime);
                        bossTimerComponent.calculateRemainingTime();
                    } else {
                        // Timer has expired, calculate elapsed cycles
                        int elapsedCycles = (int) (elapsedTime / (40 * 60 * 1000));
                        bossNameComponent.updateCurrentBoss(elapsedCycles + 1);
                        long newRemainingTime = 40 * 60 * 1000 - (elapsedTime % (40 * 60 * 1000));
                        bossTimerComponent.startTimer(newRemainingTime);
                    }
                } else {
                    System.err.println("bossName or endTime is null in the timestamp file.");
                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }

    public void saveConfig() {
        try {
            Path configPath = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE);
            if (!Files.exists(configPath)) {
                Files.createDirectories(configPath.getParent());
                Files.createFile(configPath);
            }
            Properties properties = new Properties();
            properties.setProperty("timerPosition", bossTimerComponent.corner.name());
            properties.setProperty("namePosition", bossNameComponent.corner.name());
            properties.setProperty("hudVisible", Boolean.toString(hudVisible));
            properties.setProperty("timerSetup", Boolean.toString(bossTimerComponent.isTimerSetup()));
            properties.store(Files.newOutputStream(configPath), null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveTimestamp(String bossName, long endTime) {
        try {
            Path timestampPath = FabricLoader.getInstance().getConfigDir().resolve(TIMESTAMP_FILE);
            if (!Files.exists(timestampPath)) {
                Files.createDirectories(timestampPath.getParent());
                Files.createFile(timestampPath);
            }
            Properties properties = new Properties();
            properties.setProperty("bossName", bossName);
            properties.setProperty("endTime", Long.toString(endTime));
            properties.store(Files.newOutputStream(timestampPath), null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadConfig() {
        try {
            Path configPath = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE);
            if (Files.exists(configPath)) {
                Properties properties = new Properties();
                properties.load(Files.newInputStream(configPath));
                bossTimerComponent.corner = Corner.valueOf(properties.getProperty("timerPosition", "TOP_LEFT"));
                bossNameComponent.corner = Corner.valueOf(properties.getProperty("namePosition", "TOP_LEFT"));
                hudVisible = Boolean.parseBoolean(properties.getProperty("hudVisible", "true"));
                bossTimerComponent.setTimerSetup(Boolean.parseBoolean(properties.getProperty("timerSetup", "false")));
                hudRenderer120.setHudVisible(hudVisible);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}