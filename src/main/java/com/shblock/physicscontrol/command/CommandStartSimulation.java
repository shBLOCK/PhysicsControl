package com.shblock.physicscontrol.command;

import com.jme3.bullet.PhysicsSpace;
import com.shblock.physicscontrol.client.InteractivePhysicsSimulator2D;

public class CommandStartSimulation extends PhysicsCommandBase {
    public CommandStartSimulation() {}

    public CommandStartSimulation(PhysicsSpace space) {
        super(space);
    }

    @Override
    public void execute() {
        InteractivePhysicsSimulator2D.getInstance().setSimulationRunning(true);
    }

    @Override
    public String getName() {
        return "start_simulation";
    }
}
