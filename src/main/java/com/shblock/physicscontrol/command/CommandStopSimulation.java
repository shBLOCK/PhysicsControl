package com.shblock.physicscontrol.command;

import com.shblock.physicscontrol.client.InteractivePhysicsSimulator;

public class CommandStopSimulation extends AbstractCommand {
    @Override
    public void execute() {
        InteractivePhysicsSimulator.getInstance().setSimulationRunning(false);
    }

    @Override
    public void undo() {

    }

    @Override
    public String getName() {
        return "stop_simulation";
    }

    @Override
    public boolean shouldSave() {
        return false;
    }
}
