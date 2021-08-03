package com.shblock.physicscontrol.command;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractCommand {
    protected final List<AbstractCommand> childCommands = new ArrayList<>();

    //Must have a empty constructor to make a dummy object for fromNBT
    public AbstractCommand() {}

    public abstract void execute();

    public abstract void undo();

    // Will always call this function in the command in front to merge with the new command
    public boolean mergeWith(AbstractCommand command) {
        return false;
    }

    public abstract String getName();

    // Don't call this, call CommandSerializer.toNBT()
    protected abstract CompoundNBT toNBT();

    // Don't call this, call CommandSerializer.fromNBT()
    protected abstract AbstractCommand fromNBT(CompoundNBT nbt, @Nullable AbstractCommand command);

    protected static ListNBT serializerAllChildes(List<AbstractCommand> childes) {
        ListNBT childes_nbt = new ListNBT();
        for (AbstractCommand command : childes) {
            childes_nbt.add(command.toNBT());
        }
        return childes_nbt;
    }

    protected static List<AbstractCommand> deserializerAllChildes(ListNBT nbt) {
        List<AbstractCommand> childes = new ArrayList<>();
        for (int i=0; i<nbt.size(); i++) {
            childes.add();
        }
    }
}
