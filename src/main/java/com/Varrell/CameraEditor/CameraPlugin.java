package com.Varrell.CameraEditor;

import com.Varrell.CameraEditor.Camera.CameraInitializer;
import com.Varrell.CameraEditor.Camera.CustomCameraSettings;
import com.Varrell.CameraEditor.Commands.Camera.CameraCommand;
import com.Varrell.CameraEditor.Commands.CameraGroup.CameraGroupCommand;
import com.Varrell.CameraEditor.Component.Data.PlayerPOVComponent;
import com.Varrell.CameraEditor.Component.System.PlayerPOVSystem;
import com.hypixel.hytale.assetstore.event.LoadedAssetsEvent;
import com.hypixel.hytale.assetstore.event.RemovedAssetsEvent;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.HytaleAssetStore;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class CameraPlugin extends JavaPlugin {
    private static CameraPlugin instance;
    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private ComponentType<EntityStore, PlayerPOVComponent> playerPOVComponentType;


    public static CameraPlugin get() {
        return instance;
    }

    public ComponentType<EntityStore, PlayerPOVComponent> getPlayerPOVComponentType() {
        return this.playerPOVComponentType;
    }

    public CameraPlugin(JavaPluginInit init) {
        super(init);
        instance = this;
    }

    @Override
    protected void setup() {
        this.playerPOVComponentType = this.getEntityStoreRegistry().registerComponent(PlayerPOVComponent.class, () -> {
            throw new UnsupportedOperationException("Not implemented!");
        });
        getAssetRegistry().register(HytaleAssetStore.builder(CustomCameraSettings.class, new DefaultAssetMap<>())
                .setPath("CameraSettings")
                .setCodec(CustomCameraSettings.CODEC)
                .setKeyFunction(CustomCameraSettings::getId)
                .setReplaceOnRemove(CustomCameraSettings::new)
                .build());
        getEventRegistry().register(LoadedAssetsEvent.class, CustomCameraSettings.class, this::onAssetsLoaded);
        getEventRegistry().register(RemovedAssetsEvent.class, CustomCameraSettings.class, this::onAssetsRemoved);
        this.getEntityStoreRegistry().registerSystem(new PlayerPOVSystem());
        this.getCommandRegistry().registerCommand(new CameraCommand());
        this.getCommandRegistry().registerCommand(new CameraGroupCommand());
    }

    private void onAssetsLoaded(LoadedAssetsEvent<String, CustomCameraSettings, DefaultAssetMap<String, CustomCameraSettings>> event) {
        event.getLoadedAssets().forEach((name, cam) -> CameraInitializer.updateCodecSetting(name));
    }

    private void onAssetsRemoved(RemovedAssetsEvent<String, CustomCameraSettings, DefaultAssetMap<String, CustomCameraSettings>> event) {
        event.getRemovedAssets().forEach(CameraInitializer::remove);
    }

    @Override
    protected void start() {
        CameraInitializer.init();
    }
}
