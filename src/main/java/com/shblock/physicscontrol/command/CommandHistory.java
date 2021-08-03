package com.shblock.physicscontrol.command;

import java.util.ArrayList;
import java.util.List;

// https://www.youtube.com/watch?v=vqRHjhaECv4
public class CommandHistory {
    private final List<AbstractCommand> history = new ArrayList<>();
    private int pointer = -1; //index of current command in the history list

    public void execute(AbstractCommand command) {
        while (history.size() >= pointer) {
            history.remove(pointer + 1);
        }
        command.execute();
        boolean didMerge = false;
        if (pointer > -1) {
            didMerge = history.get(pointer).mergeWith(command);
        }
        if (!didMerge) {
            // if there's more command behind the current pointer, they will be removed by the while loop on top, so we don't have to insert the new command to the pointer location
            history.add(command);
            pointer++;
        }
    }

    public AbstractCommand undo() {
        if (pointer == -1) {
            return null;
        }
        AbstractCommand command = history.get(pointer);
        command.undo();
        pointer--;
        return command;
    }

    public AbstractCommand redo() {
        if (pointer < history.size() - 1) {
            AbstractCommand command = history.get(pointer);
            command.redo(); //By default just calls command.execute()
            pointer++;
            return command;
        }
        return null;
    }
}
