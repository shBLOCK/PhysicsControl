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

    // Called after mergeWith(), to check if this command should be removed from the command history (for example: move command move to the start position)
    public boolean shouldRemove() {
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
    public abstract CompoundNBT serializeNBT();

    @Override
    public abstract void deserializeNBT(CompoundNBT nbt);
}
