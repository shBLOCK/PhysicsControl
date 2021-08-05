package com.shblock.physicscontrol.command;

import com.shblock.physicscontrol.client.InteractivePhysicsSimulator2D;

public class CommandStartSimulation extends PhysicsCommandBase {
    @Override
    public void execute() {
        InteractivePhysicsSimulator2D.getInstance().setSimulationRunning(true);
    }

    @Override
    public String getName() {
        return "start_simulation";
    }
}
