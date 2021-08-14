package com.shblock.physicscontrol.command;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.ArrayList;
import java.util.List;

// https://www.youtube.com/watch?v=vqRHjhaECv4
public class CommandHistory implements INBTSerializable<CompoundNBT> {
    private int MAX_HISTORY = 64;
    private List<AbstractCommand> history = new ArrayList<>();
    private int pointer = -1; //index of current command in the history list

    public void execute(AbstractCommand command) {
        command.execute();
        if (!command.shouldSave()) {
            return;
        }

        while (history.size() > pointer + 1) {
            history.remove(pointer + 1);
        }

        boolean didMerge = false;
        if (pointer > -1) {
            didMerge = history.get(pointer).mergeWith(command);
        }
        if (!didMerge) {
            // if there's more command behind the current pointer, they will be removed by the while loop on top, so we don't have to insert the new command to the pointer location
            history.add(command);
            pointer++;
            while (history.size() > MAX_HISTORY && pointer > 3) { //always keep a few history (pointer > 3)
                history.remove(0);
                pointer--;
            }
        } else if(history.get(pointer).shouldRemove()) {
            history.remove(pointer);
            this.pointer--;
        }
    }

    public AbstractCommand undo() {
        if (pointer == -1) {
            return null;
        }
        AbstractCommand command = history.get(pointer);
        if (command.undo()) {
            return null;
        }
        pointer--;
        return command;
    }

    public AbstractCommand redo() {
        if (pointer < history.size() - 1) {
            AbstractCommand command = history.get(pointer + 1);
            command.redo(); //By default just calls command.execute()
            pointer++;
            return command;
        }
        return null;
    }

    public int getMaxHistory() {
        return MAX_HISTORY;
    }

    public void setMaxHistory(int MAX_HISTORY) {
        this.MAX_HISTORY = MAX_HISTORY;
    }

    public List<AbstractCommand> getList() {
        return history;
    }

    public void setList(List<AbstractCommand> history) {
        this.history = history;
    }

    public int getPointer() {
        return pointer;
    }

    public void setPointer(int pointer) {
        this.pointer = pointer;
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putInt("max_history", this.MAX_HISTORY);
        nbt.putInt("pointer", this.pointer);
        ListNBT commands = new ListNBT();
        for (AbstractCommand cmd : this.history) {
            commands.add(CommandSerializer.toNBT(cmd));
        }
        nbt.put("history", commands);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        this.MAX_HISTORY = nbt.getInt("max_history");
        this.pointer = nbt.getInt("pointer");
        this.history.clear();
        ListNBT commands = nbt.getList("history", Constants.NBT.TAG_COMPOUND);
        for (int i=0; i<commands.size(); i++) {
            this.history.add(CommandSerializer.fromNBT(commands.getCompound(i)));
        }
    }
}
