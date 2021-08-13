package com.shblock.physicscontrol.client.gui.PhysicsSimulator;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;

public class SaveHelper {
    public static final File PATH = new File(Minecraft.getInstance().gameDirectory, "PhysicsWorlds");
    public static final String EXTENSION = "nbt";
    public static final FilenameFilter FILTER = (dir, name) -> name.endsWith("." + EXTENSION);
    public static final String AUTOSAVE_FILENAME = "_AUTOSAVE";
    public static final String BACKUP_FILENAME = "_BACKUP_%s";

    /**
     * Ensure the target folder exist, if not, create a new one.
     * @return true if the folder exist or is created successfully, false if it doesn't exist and failed to create a new one.
     */
    public static boolean ensureFolderExist() {
        if (!PATH.exists()) {
            try {
                return PATH.mkdir();
            } catch (SecurityException ignored) {
                return false;
            }
        }
        return true;
    }

    public static String[] getNBTFileList() {
        if (!ensureFolderExist()) {
            return new String[0];
        }
        return Arrays.stream(PATH.list(FILTER)).map(s -> s.substring(0, s.length() - 4)).toArray(String[]::new);
    }

    public static boolean contains(String name) {
        if (!ensureFolderExist()) {
            return false;
        }
        for (String f : getNBTFileList()) {
            if (name.equals(f)) {
                return true;
            }
        }
        return false;
    }

    public static File saveNBTFile(CompoundNBT nbt, String name) throws IOException {
        if (!ensureFolderExist()) {
            throw new IOException("Can't create the dest folder!");
        }
        File file = new File(PATH, name + "." + EXTENSION);
        CompressedStreamTools.writeCompressed(nbt, file);
        return file;
    }

    public static CompoundNBT readNBTFile(String name) throws IOException {
        if (!ensureFolderExist()) {
            throw new IOException("Can't create the dest folder!");
        }
        File file = new File(PATH, name + "." + EXTENSION);
        return CompressedStreamTools.readCompressed(file);
    }
}
