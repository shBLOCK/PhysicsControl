package com.shblock.physicscontrol.client;

import com.google.common.collect.Lists;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.PhysicsRayTestResult;
import com.jme3.bullet.objects.PhysicsGhostObject;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Vector3f;
import com.shblock.physicscontrol.command.*;
import com.shblock.physicscontrol.physics.physics2d.CollisionObjectUserObj2D;
import com.shblock.physicscontrol.physics.util.Vector2f;

import java.util.*;
import java.util.stream.Collectors;

public class InteractivePhysicsSimulator2D { //TODO: serialize this instead of space
    private static InteractivePhysicsSimulator2D currentInstance;

    private PhysicsSpace space;
    private int currentId = -1;
    private boolean simulationRunning;
    public enum StepModes {
        TICK, //step the simulation every tick (1/20 sec)
        FRAME //step the simulation every frame
    }
    private StepModes stepMode = StepModes.FRAME;
    private float simulationSpeed = 1F;
    private CommandHistory commandHistory;
    private final List<PhysicsCollisionObject> selectedObjects = new ArrayList<>(); //TODO: store only id when serializing

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

//    public static void setInstance(InteractivePhysicsSimulator2D simulator) {
//        currentInstance = simulator;
//    }

    public void close() {
        currentInstance = null;
    }

    public void singleStep(int steps) {
        for (int i=0; i<steps; i++) {
            this.space.update(this.space.getAccuracy(), 0);
            update();
        }
    }

    public void step(float time) {
        this.space.update(time);
        update();
    }

    private void update() {
        for (int i=0; i<this.selectedObjects.size(); i++) {
            if (!this.selectedObjects.get(i).isInWorld()) {
                this.selectedObjects.remove(i);
                i--;
            }
        }
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

    public PhysicsCollisionObject getPcoFromId(int id) {
        for (PhysicsCollisionObject pco : getSpace().getPcoList()) {
            if (((CollisionObjectUserObj2D) pco.getUserObject()).getId() == id) {
                return pco;
            }
        }
        return null;
    }

    public void addPco(PhysicsCollisionObject pco) {
        getSpace().addCollisionObject(pco);
    }

    public void movePco(PhysicsCollisionObject pco, Vector2f offset) {
        Vector3f pos = pco.getPhysicsLocation(null).add(offset.toVec3());
        if (pco instanceof PhysicsRigidBody) {
            ((PhysicsRigidBody) pco).setPhysicsLocation(pos);
        } else if (pco instanceof PhysicsGhostObject) {
            ((PhysicsGhostObject) pco).setPhysicsLocation(pos);
        }
        InteractivePhysicsSimulator2D.getInstance().reAddToUpdate(pco);
    }

    public void deletePco(PhysicsCollisionObject pco) {
        getSpace().removeCollisionObject(pco);
    }

    public void moveSelected(Vector2f offset, boolean isFirst) {
        executeCommand(new CommandMoveCollisionObjects(this.selectedObjects, offset.toVec3(), isFirst));
    }

    public void deleteSelected() {
        executeCommand(new CommandDeleteCollisionObjects(this.selectedObjects));
    }

    /**
     * Change the Z-Level of currently selected objects.
     * @param change increase the level by 1 when this is 1, decrease the level by 1 when this is -1, assert otherwise.
     */
    public void changeSelectedZLevel(int change) {
        assert change == 1 || change == -1 : change;
        List<CollisionObjectUserObj2D> obj_list = this.selectedObjects.stream().map(pco -> (CollisionObjectUserObj2D) pco.getUserObject()).collect(Collectors.toList());
        obj_list.sort((a, b) -> {
            return Integer.compare(a.getZLevel(), b.getZLevel()) * (-change); // if change=-1, sort the z-level in inverted order
        });
        for (CollisionObjectUserObj2D obj : obj_list) {
            if (change == 1 && obj.getZLevel() >= space.countCollisionObjects() - 1) {
                return;
            } else if (change == -1 && obj.getZLevel() <= 0) {
                return;
            }
        }
        for (CollisionObjectUserObj2D obj : obj_list) {
            if (change == 1) {
                obj.moveZLevelUp(getSpace());
            } else {
                obj.moveZLevelDown(getSpace());
            }
        }
    }

    public void reAddToUpdate(PhysicsCollisionObject pco) { // we can update the Collision Object's position by remove and re adding it to the space
        getSpace().removeCollisionObject(pco);
        getSpace().addCollisionObject(pco);
    }

    public List<PhysicsCollisionObject> pointTest(Vector2f point) {
        return this.pointRayTest(point);
    }

//    public List<PhysicsCollisionObject> pointContactTest(Vector2f point) {
//        PhysicsGhostObject pco = new PhysicsGhostObject(new SphereCollisionShape(0.001F));
//        pco.setPhysicsLocation(point.toVec3());
//        return new ArrayList<>(contactTest(pco));
//    }

    private List<PhysicsCollisionObject> pointRayTest(Vector2f point) {
        Vector3f vec = point.toVec3();
        Vector3f offset = new Vector3f(0F, 0F, 10000F);
        return getSpace().rayTestRaw(
                vec.add(offset),
                vec.subtract(offset)
        ).stream().map(PhysicsRayTestResult::getCollisionObject).collect(Collectors.toList());
    }

    public List<PhysicsCollisionObject> pointTestSorted(Vector2f point) {
        List<PhysicsCollisionObject> result = pointTest(point);
        result.sort(Comparator.comparingInt(a -> ((CollisionObjectUserObj2D) a.getUserObject()).getZLevel()));
        result = Lists.reverse(result);
        return result;
    }

    public boolean isPointOnAnySelected(Vector2f point) {
        if (!isAnySelected()) {
            return false;
        }
        List<PhysicsCollisionObject> results = pointTestSorted(point);
        if (results.isEmpty()) {
            return false;
        }
        return isSelected(results.get(0));
    }

    public Collection<PhysicsCollisionObject> contactTest(PhysicsCollisionObject pco) {
        Collection<PhysicsCollisionObject> results = new HashSet<>();
        getSpace().contactTest(pco, event -> {
            if (event.getObjectA() == pco) {
                results.add(event.getObjectB());
            } else {
                results.add(event.getObjectA());
            }
        });
        return results;
    }

    public boolean isSelected(PhysicsCollisionObject pco) {
        return this.selectedObjects.contains(pco);
    }

    public boolean isAnySelected() {
        return !this.selectedObjects.isEmpty();
    }

    public boolean select(PhysicsCollisionObject pco) {
        if (!isSelected(pco)) {
            this.selectedObjects.add(pco);
            return true;
        }
        return false;
    }

    public void selectAll() {
        unselectAll();
        for (PhysicsCollisionObject pco : getSpace().getPcoList()) {
            select(pco);
        }
    }

    public boolean unselect(PhysicsCollisionObject obj) {
        return this.selectedObjects.remove(obj);
    }

    public void unselectAll() {
        this.selectedObjects.clear();
    }

    public PhysicsSpace getSpace() {
        return this.space;
    }

    public void setSpace(PhysicsSpace new_space) {
        this.space = new_space;
    }

    public void executeCommand(AbstractCommand command) {
        this.commandHistory.execute(command);
    }

    public AbstractCommand undo() {
        unselectAll();
        return commandHistory.undo();
    }

    public AbstractCommand redo() {
        unselectAll();
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
            executeCommand(new CommandStartSimulation(false));
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
        this.currentId++;
        return this.currentId;
    }
}
