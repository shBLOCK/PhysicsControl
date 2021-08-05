package com.shblock.physicscontrol.command;

import net.minecraft.nbt.CompoundNBT;

import java.util.HashMap;
import java.util.Map;

public class CommandSerializer {
    private static final Map<String, Class<? extends AbstractCommand>> registry_map = new HashMap<>();

    public static void register(String name, Class<? extends AbstractCommand> clz) {
        registry_map.put(name, clz);
    }

    public static void register(Class<? extends AbstractCommand> clz) {
        try {
            register(clz.newInstance().getName(), clz);
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            assert false;
        }
    }

    private static AbstractCommand fromName(String name) {
        try {
            return registry_map.get(name).newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static CompoundNBT toNBT(AbstractCommand command) {
        return command.serializeNBT();
    }

    public static AbstractCommand fromNBT(CompoundNBT nbt) {
        AbstractCommand command = fromName(nbt.getString("type"));
        command.deserializeNBT(nbt);
        return command;
    }

    public static void init() {
        register(CommandStartSimulation.class);
        register(CommandStopSimulation.class);
        register(CommandAddRigidBody.class);
    }
}
