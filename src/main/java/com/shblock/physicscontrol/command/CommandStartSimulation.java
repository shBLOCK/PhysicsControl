package com.shblock.physicscontrol.command;

import com.shblock.physicscontrol.client.InteractivePhysicsSimulator;

public class CommandStartSimulation extends PhysicsCommandBase {
    @Override
    public void execute() {
        InteractivePhysicsSimulator.getInstance().setSimulationRunning(true);
    }

    @Override
    public String getName() {
        return "start_simulation";
    }
}
