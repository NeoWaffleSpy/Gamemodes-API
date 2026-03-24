# <font color="cyan">Gamemode API</font>

This is a basic API to create new Cameras with custom mouse controle and interactions.
This is still a work in progress.

## <font color="cyan">Set up a custom Camera</font>
Simply go in the AssetEditor and create a new CustomCameraSettings (see official [doc](https://hytalemodding.dev/en/docs/guides/plugin/customizing-camera-controls))

The Camera will be directly available in game using /camera set <CameraName>

Built-in camera modes:
- TopDown
- SideView
- Isometric
- ShoulderView

## <font color="orange">[WIP]</font> <font color="cyan">Custom Mouse Control</font>
To create a custom mouse control, create a new java class that extend `AbstractMouseControl` and override
the mouse button abstract methods, the default mouse controle is adapted to top-down view
but not very adapted to the others.

the abstract class handle the mouse event dispatch and some data parsing, but you can override it
if you need more control.

## <font color="cyan">Create the camera instance</font>
Now in the `setup()` of your plugin, create the MouseControle class and after that,
create the camera instance with the code
```Java
new CameraInitializer("CameraInstanceName", new myCustomMouseControle(), "MyCustomCameraSettings");
```

Example with the available default options:
```Java
new CameraInitializer("IsometricCamera2", new DefaultMouseControl(), "isometric");
```

### Apply Camera to a player
To apply or remove the custom gamemode, simply add the `PlayerPOVComponent` to the player.
Do not forget to remove it if it was already present as it won't register properly otherwise.
```Java
CameraInitializer.setPlayerPov("CameraInstanceName", playerRef);
```

## <font color="cyan">Custom commands</font>

- `/camera` *Command group for individual camera management*
- - `/camera get` *Get the name of the current camera instance*
- - `/camera list` *Get the name of all available cameras*
- - `/camera reset` *Reset all camera, controls and setting to default*
- - `/camera set <CameraInstanceName>` *Apply the given camera configuration*
- `/cameraGroup` *Command group for global camera management*
- - `/cameraGroup activate <CameraInstanceName>`
- - `/cameraGroup deactivate <CameraInstanceName>`

**A disabled camera group reset all players using it to default and player cannot
use the configuration until reenabled**

****

## <font color="cyan">Technical issues and WIP stuff</font>
I am working on a way to automatize the process with some event such as joining a specific instance
or if you use a specific item, but I need to have a working logic and way to make
it modular for easy configuration.

There is an issue with interaction and block placement that calculate as if you never moved from the point where
you changed your camera, but combat/hitboxes works fine and in multiplayer your model move correctly, still looking into it.

In very rare cases, the camera in third person get stuck looking at the sky, just reset the POV
and it start working again, i failed to find the reason that said.

When using a custom camera, when you Alt-Tab, the camera start to jitter, especially with the `Look Multiplier`
value increased, it goes back to normal when focusing on the game again, it seems like a client-side issue.

I welcome any feedback and advices to improve the idea.