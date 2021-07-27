package com.shblock.physicscontrol.physics;

import com.jme3.system.NativeLibraryLoader;
import com.shblock.physicscontrol.PhysicsControl;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.ModList;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

//inspired by Rayon's native loader:
//https://github.com/LazuriteMC/Rayon/blob/main/rayon-core/src/main/java/dev/lazurite/rayon/core/impl/physics/util/NativeLoader.java
public class BulletNativeHandler {
    public static final String BULLET_VERSION = "10.5.0";

    public static void load() {
        Path dest = Minecraft.getInstance().gameDirectory
                .toPath()
                .resolve("natives")
                .resolve(BULLET_VERSION);
        Path source = ModList.get()
                .getModFileById(PhysicsControl.MODID)
                .getFile()
                .getFilePath()
                .resolve("assets")
                .resolve(PhysicsControl.MODID)
                .resolve("natives");
        try {
            for (Path path : Files.walk(source).collect(Collectors.toList())) {
                Path copy_dist = dest.resolve(source.relativize(path).getFileName().toString());
                if (!Files.exists(copy_dist)) {
                    if (!Files.exists(copy_dist.getParent())) {
                        Files.createDirectory(copy_dist.getParent());
                    }
                    PhysicsControl.log("Copying bullet natives from:" + path + " to:" + copy_dist);
                    Files.copy(path, copy_dist);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to extract native libraries for bullet physics engine, please report this to PhysicsControl's github!");
        }

        if (!NativeLibraryLoader.loadLibbulletjme(true, dest.toFile(), "Release", "Dp")) { //TODO: make Dp or Sp a config option in config file
            throw new RuntimeException("Failed to load native libraries for bullet physics engine, please report this to PhysicsControl's github!");
        } else {
            PhysicsControl.log("Bullet native libraries loaded");
        }
    }
}
