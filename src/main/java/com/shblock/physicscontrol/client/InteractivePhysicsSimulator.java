package com.shblock.physicscontrol.client;

import com.jme3.bullet.PhysicsSpace;
import com.shblock.physicscontrol.command.AbstractCommand;
import com.shblock.physicscontrol.command.CommandHistory;

public class InteractivePhysicsSimulator { //TODO: serialize this instead of space
    private static InteractivePhysicsSimulator currentInstance;

    private PhysicsSpace space;
    private boolean simulationRunning;
    private CommandHistory commandHistory;

    public InteractivePhysicsSimulator(PhysicsSpace space) {
        if (currentInstance != null) {
            throw new RuntimeException("You have to close the last Instance to create a new one!");
        }
        assert currentInstance == null;
        currentInstance = this;
        this.space = space;
    }

    public static InteractivePhysicsSimulator getInstance() {
        return currentInstance;
    }

    public void close() {
        currentInstance = null;
    }

    public void singleStep() {
        this.space.update(this.space.getAccuracy(), 0);
    }

    public void step(float time) {
        this.space.update(time);
    }

    public PhysicsSpace getSpace() {
        return this.space;
    }

    public void executeCommand(AbstractCommand command) {
        commandHistory.execute(command);
    }

    public AbstractCommand undo() {
        return commandHistory.undo();
    }

    public AbstractCommand redo() {
        return commandHistory.redo();
    }
}
