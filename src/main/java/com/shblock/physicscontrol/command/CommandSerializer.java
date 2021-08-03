package com.shblock.physicscontrol.command;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants;

import java.util.HashMap;
import java.util.Map;

public class CommandSerializer {
    private static final Map<String, AbstractCommand> handlers = new HashMap<>();

    public static void register(String name, AbstractCommand obj) {
        handlers.put(name, obj);
    }

    public static CompoundNBT toNBT(AbstractCommand command) {
        CompoundNBT nbt = command.toNBT();
        nbt.putString("type", command.getName());
        ListNBT childes = new ListNBT();
        for (Object child : command.childCommands) {
            childes.add(toNBT((AbstractCommand) child));
        }
        nbt.put("childes", childes);
        return nbt;
    }

    public static AbstractCommand fromNBT(CompoundNBT nbt) {
        AbstractCommand command = handlers.get(nbt.getString("type")).fromNBT(nbt, null);
        ListNBT childes = nbt.getList("childes", Constants.NBT.TAG_COMPOUND);
        for (int i=0; i<childes.size(); i++) {
            command.childCommands.add(fromNBT(childes.getCompound(i)));
        }
        return command;
    }
}
