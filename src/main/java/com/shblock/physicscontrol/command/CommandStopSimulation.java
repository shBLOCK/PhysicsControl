package com.shblock.physicscontrol.command;

import com.shblock.physicscontrol.client.InteractivePhysicsSimulator2D;
import net.minecraft.nbt.CompoundNBT;

public class CommandStopSimulation extends AbstractCommand {
    public CommandStopSimulation() {}

    @Override
    public void execute() {
        InteractivePhysicsSimulator2D.getInstance().setSimulationRunning(false);
    }

    @Override
    public boolean undo() {
        return true;
    }

    @Override
    public String getName() {
        return "stop_simulation";
    }

    @Override
    public boolean shouldSave() {
        return false;
    }

    @Override
    public CompoundNBT serializeNBT() {
        return new CompoundNBT();
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) { }
}
