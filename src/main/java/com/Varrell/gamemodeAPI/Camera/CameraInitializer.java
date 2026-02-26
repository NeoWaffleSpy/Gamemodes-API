package com.Varrell.gamemodeAPI.Camera;

import com.Varrell.gamemodeAPI.Camera.MouseControl.DefaultMouseControl;
import com.Varrell.gamemodeAPI.Camera.MouseControl.AbstractMouseControl;
import com.Varrell.gamemodeAPI.Component.Data.PlayerPOVComponent;
import com.Varrell.gamemodeAPI.GamemodeAPI;
import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.protocol.ClientCameraView;
import com.hypixel.hytale.protocol.ServerCameraSettings;
import com.hypixel.hytale.protocol.packets.camera.SetServerCamera;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.event.events.player.PlayerInteractEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerMouseButtonEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public class CameraInitializer {
    private static Dictionary<String, CameraInitializer> camDict = new Hashtable<>();
    private final EventRegistry eventRegistry = new EventRegistry(new CopyOnWriteArrayList<>(), () -> this.isActive, "CameraDemo is not active!", HytaleServer.get().getEventBus());
    private final ServerCameraSettings cameraSettings;
    private final AbstractMouseControl mouseControl;
    public String cameraName;
    public boolean isActive;
    public boolean isPlayerHidden;

    public CameraInitializer(String cameraName) {
        this.cameraSettings = CameraTemplates.get(cameraName);
        this.mouseControl = new DefaultMouseControl();
        this.isPlayerHidden = false;
        camDict.put(cameraName, this);
        activate();
    }

    public CameraInitializer(String cameraName, AbstractMouseControl mouseControl, boolean isPlayerHidden, String templateName) {
        this.cameraSettings = CameraTemplates.get(templateName);
        this.mouseControl = mouseControl;
        this.isPlayerHidden = isPlayerHidden;
        this.cameraName = cameraName;
        camDict.put(cameraName, this);
        activate();
    }

    public static void init() {
        new CameraInitializer("topDown");
        new CameraInitializer("sideView");
        new CameraInitializer("isometric");
        new CameraInitializer("isometric2", new DefaultMouseControl(), false, "isometric");
    }

    public static void editCameraSettings(PlayerRef playerRef, ServerCameraSettings newSettings) {
        PlayerPOVComponent pPOV = getPOV(playerRef);
        if (pPOV != null) {
            pPOV.setCamSettings(newSettings);
            playerRef.getPacketHandler().writeNoCache(new SetServerCamera(ClientCameraView.Custom, true, newSettings));
        }
    }

    public static void editMouseControl(PlayerRef playerRef, AbstractMouseControl newControl) {
        PlayerPOVComponent pPOV = getPOV(playerRef);
        if (pPOV != null)
            pPOV.setMouseControl(newControl);
    }

    public static CameraInitializer get(String key) {
        return camDict.get(key);
    }

    public void activate() {
        if (!this.isActive) {
            this.eventRegistry.enable();
            this.isActive = true;
            //this.eventRegistry.register(PlayerConnectEvent.class, (event) -> this.setPOV(event.getPlayerRef()));
            this.eventRegistry.register(PlayerMouseButtonEvent.class, this::dispatchControl);
            this.eventRegistry.registerGlobal(PlayerInteractEvent.class, (event) -> event.setCancelled(true));
            Universe.get().getPlayers().forEach((pRef) -> {
                PlayerPOVComponent pPOV = getPOV(pRef);
                if (pPOV != null) {
                    String componentName = getPOV(pRef).getPOVName();
                    if (componentName.equals(cameraName))
                        this.setPOV(pRef);
                }
            });
        }
    }

    public void setPOV(@Nonnull PlayerRef playerRef) {
        if (!isActive)
            return;
        if (isPlayerHidden)
            playerRef.getHiddenPlayersManager().hidePlayer(playerRef.getUuid());
        else
            playerRef.getHiddenPlayersManager().showPlayer(playerRef.getUuid());
        PlayerPOVComponent pPOV = getPOV(playerRef);
        if (pPOV == null)
            return;
        pPOV.setMouseControl(mouseControl);
        pPOV.setCamSettings(cameraSettings);
        playerRef.getPacketHandler().writeNoCache(new SetServerCamera(ClientCameraView.Custom, true, this.cameraSettings));
    }

    public void deactivate() {
        if (this.isActive) {
            this.eventRegistry.shutdown();
            Universe.get().getPlayers().forEach((PlayerRef playerRef) -> {
                PlayerPOVComponent pPOV = getPOV(playerRef);
                if (pPOV != null && Objects.equals(pPOV.getPOVName(), cameraName)) {
                    CameraInitializer.resetCamera(playerRef);
                }
            });
            this.isActive = false;
        }
    }

    public static void resetCamera(PlayerRef playerRef) {
        playerRef.getHiddenPlayersManager().showPlayer(playerRef.getUuid());
        playerRef.getPacketHandler().writeNoCache(new SetServerCamera(ClientCameraView.Custom, false, null));
    }

    public void dispatchControl(PlayerMouseButtonEvent event) {
        event.getPlayerRef().getStore().getComponent(event.getPlayerRef(), PlayerPOVComponent.getComponentType()).getMouseControl().onPlayerMouseButton(event);
    }

    private static PlayerPOVComponent getPOV(PlayerRef playerRef) {
        PlayerPOVComponent pPOV = null;
        try {
            pPOV = playerRef.getReference().getStore().getComponent(playerRef.getReference(), PlayerPOVComponent.getComponentType());
        } catch (NullPointerException npe) {
            try {
                pPOV = playerRef.getHolder().getComponent(PlayerPOVComponent.getComponentType());
            } catch (NullPointerException npe2) {
                GamemodeAPI.LOGGER.atSevere().log("PlayerPOVComponent.getComponentType() is null for " + playerRef.getUsername());
                return null;
            }
        }
        return pPOV;
    }

    public static String getCameraList() {
        if (camDict.isEmpty())
            return null;
        return String.join(", ", Collections.list(camDict.keys()));
    }
}
