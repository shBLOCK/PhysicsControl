package com.shblock.physicscontrol.client;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.shblock.physicscontrol.command.AbstractCommand;
import com.shblock.physicscontrol.command.CommandHistory;
import com.shblock.physicscontrol.command.CommandStartSimulation;
import com.shblock.physicscontrol.command.CommandStopSimulation;

import java.util.HashMap;
import java.util.Map;

public class InteractivePhysicsSimulator2D { //TODO: serialize this instead of space
    private static InteractivePhysicsSimulator2D currentInstance;

    private PhysicsSpace space;
    private final Map<Integer, Integer> zIndexes = new HashMap<>();
    private int currentId = -1;
    private boolean simulationRunning;
    public enum StepModes {
        TICK, //step the simulation every tick (1/20 sec)
        FRAME //step the simulation every frame
    }
    private StepModes stepMode = StepModes.FRAME;
    private float simulationSpeed = 1F;
    private CommandHistory commandHistory;

    public InteractivePhysicsSimulator2D(PhysicsSpace space) {
        if (currentInstance != null) {
            throw new RuntimeException("You have to close the last Instance to create a new one!");
        }
        assert currentInstance == null;
        currentInstance = this;
        this.space = space;
        this.commandHistory = new CommandHistory();
    }

    public static InteractivePhysicsSimulator2D getInstance() {
        return currentInstance;
    }

    public static void setInstance(InteractivePhysicsSimulator2D simulator) {
        currentInstance = simulator;
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

    //Should be called every tick (0.05 sec)
    public void tick() {
        if (isSimulationRunning() && this.stepMode == StepModes.TICK) {
            step(0.05F * this.simulationSpeed);
        }
    }

    //Should be called every frame
    public void frame(float particleTick) {
        if (isSimulationRunning() && this.stepMode == StepModes.FRAME) {
            step(particleTick * 0.05F * this.simulationSpeed);
        }
    }

    public PhysicsSpace getSpace() {
        return this.space;
    }

    public void setSpace(PhysicsSpace new_space) {
        this.space = new_space;
    }

    public void addRigidBody(PhysicsRigidBody body) {

    }

    public void executeCommand(AbstractCommand command) {
        this.commandHistory.execute(command);
    }

    public AbstractCommand undo() {
        return commandHistory.undo();
    }

    public AbstractCommand redo() {
        return commandHistory.redo();
    }

    public StepModes getStepMode() {
        return stepMode;
    }

    public void setStepMode(StepModes stepMode) {
        this.stepMode = stepMode;
    }

    public boolean isSimulationRunning() {
        return simulationRunning;
    }

    public void setSimulationRunning(boolean simulationRunning) {
        this.simulationRunning = simulationRunning;
    }

    public void switchSimulationRunning() {
        if (isSimulationRunning()) {
            executeCommand(new CommandStopSimulation());
        } else {
            executeCommand(new CommandStartSimulation());
        }
    }

    public float getSimulationSpeed() {
        return simulationSpeed;
    }

    public void setSimulationSpeed(float simulationSpeed) {
        this.simulationSpeed = simulationSpeed;
    }

    public int getCurrentId() {
        return currentId;
    }

    public void setCurrentId(int currentId) {
        this.currentId = currentId;
    }

    public CommandHistory getCommandHistory() {
        return commandHistory;
    }

    public void setCommandHistory(CommandHistory commandHistory) {
        this.commandHistory = commandHistory;
    }

    public int nextId() {
        return this.currentId++;
    }

    public int getZIndex(int id) {
        return this.zIndexes.get(id);
    }
}
