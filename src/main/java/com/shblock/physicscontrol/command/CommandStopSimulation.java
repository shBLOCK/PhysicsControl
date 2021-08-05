package com.shblock.physicscontrol.command;

import com.shblock.physicscontrol.client.InteractivePhysicsSimulator2D;

public class CommandStopSimulation extends AbstractCommand {
    @Override
    public void execute() {
        InteractivePhysicsSimulator2D.getInstance().setSimulationRunning(false);
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
