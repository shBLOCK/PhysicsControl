package com.shblock.physicscontrol.command;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;

public abstract class AbstractCommand implements INBTSerializable<CompoundNBT> {
//    protected final List<AbstractCommand> childCommands = new ArrayList<>();

    //Must have a empty constructor to make a dummy object for deserializeNBT
    public AbstractCommand() {}

    public abstract void execute();

    public abstract void undo();

    public void redo() {
        execute();
    }

    // Will always call this function in the command in front to merge with the new command
    public boolean mergeWith(AbstractCommand command) {
        return false;
    }

    public abstract String getName();

    /**
     * If this command should be saved in the command history list.
     */
    public boolean shouldSave() {
        return true;
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putString("type", getName());
//        ListNBT childes = new ListNBT();
//        for (AbstractCommand child : childCommands) {
//            childes.add(CommandSerializer.toNBT(child));
//        }
//        nbt.put("childes", childes);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
//        ListNBT childes = nbt.getList("childes", Constants.NBT.TAG_COMPOUND);
//        for (int i=0; i<childes.size(); i++) {
//            childCommands.add(CommandSerializer.fromNBT(childes.getCompound(i)));
//        }
    }
}
