package com.shblock.physicscontrol.client;

import com.google.common.collect.Lists;
import com.shblock.physicscontrol.PhysicsControl;
import com.shblock.physicscontrol.command.*;
import com.shblock.physicscontrol.physics.physics.BodyUserObj;
import com.shblock.physicscontrol.physics.util.BodyHelper;
import com.shblock.physicscontrol.physics.util.NBTSerializer;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;
import org.apache.logging.log4j.Level;
import org.jbox2d.collision.Collision;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;

import java.util.*;
import java.util.function.Consumer;

public class InteractivePhysicsSimulator2D implements INBTSerializable<CompoundNBT> { //TODO: serialize this instead of space
    private static InteractivePhysicsSimulator2D currentInstance;

    private World space;
    private int currentId = -1;
    private boolean simulationRunning;

    public enum StepModes {
        TICK, //step the simulation every tick (1/20 sec)
        FRAME //step the simulation every frame
    }
    private StepModes stepMode = StepModes.FRAME;
    private int velocityIterations = 10;
    private int positionIterations = 8;
    private float simulationSpeed = 1F;
    private float singleStepLength = 1F / 60F;
    private CommandHistory commandHistory;
    private final Map<Integer, Body> idBodyMap = new HashMap<>();
    private final List<Body> selectedObjects = new ArrayList<>(); //TODO: store only id when serializing

    public InteractivePhysicsSimulator2D(World space) {
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
            this.space.step(this.singleStepLength, this.velocityIterations, this.positionIterations);
            update();
        }
    }

    public void step(float time) {
        this.space.step(time, this.velocityIterations, this.positionIterations);
        update();
    }

    private void update() {
//        for (int i=0; i<this.selectedObjects.size(); i++) {
//            if (this.selectedObjects.get(i).getWorld() == null) {
//                this.selectedObjects.remove(i);
//                i--;
//            }
//        }
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

    public void forEachBody(Consumer<Body> consumer) {
        for (Body body : this.idBodyMap.values()) {
            consumer.accept(body);
        }
    }

    public Body getBodyFromId(int id) {
        return this.idBodyMap.getOrDefault(id, null);
    }

    public Body addBodyLocal(BodyDef bodyDef, FixtureDef fixture) {
        return this.addBodyLocal(bodyDef, new FixtureDef[]{fixture});
    }

    public Body addBodyLocal(BodyDef bodyDef, FixtureDef[] fixtures) {
//        body.setSleepingAllowed(false);
        Body body = getSpace().createBody(bodyDef);
        assert body.getUserData() instanceof BodyUserObj;
        this.idBodyMap.put(((BodyUserObj) body.getUserData()).getId(), body);
        for (FixtureDef fixture : fixtures) {
            try {
                body.createFixture(fixture);
            } catch (AssertionError error) {
                PhysicsControl.log(Level.WARN, "Failed to create fixture, did you make your polygon too small?");
            }
        }
        return body;
    }

    public void setBodyPosLocal(Body body, Vec2 pos) {
        body.setTransform(pos, body.getAngle());
    }

    public void moveBodyLocal(Body body, Vec2 offset) {
        setBodyPosLocal(body, body.getPosition().add(offset));
    }

    public void deleteBodyLocal(Body body) {
        getSpace().destroyBody(body);
        assert body.getUserData() instanceof BodyUserObj;
        this.idBodyMap.remove(((BodyUserObj) body.getUserData()).getId());
    }

    public void moveSelected(Vec2 offset, boolean isFirst) {
        executeCommand(new CommandSetBodyPos(this.selectedObjects, offset, isFirst));
    }

    public void deleteSelected() {
        executeCommand(new CommandDeleteBodies(this.selectedObjects));
    }

    public void changeZLevel(Body pco, int change) {
        executeCommand(new CommandChangeZLevel(change, Lists.newArrayList(pco)));
    }

    public void changeSelectedZLevel(int change) {
        executeCommand(new CommandChangeZLevel(change, this.selectedObjects));
    }

    public void startMove() {
        for (Body body : this.selectedObjects) {
            body.setGravityScale(0F);
            body.setLinearVelocity(new Vec2(0F, 0F));
            body.setAngularVelocity(0F);
        }
    }

    public void stopMove() {
        for (Body body : this.selectedObjects) {
            body.setGravityScale(1F);
            body.setAwake(true);
            body.setActive(true);
        }
    }

    public List<Body> pointTest(Vec2 point) {
        List<Body> results = new ArrayList<>();
        forEachBody(body -> {
            BodyHelper.forEachFixture(
                    body,
                    fixture -> {
                        if (fixture.testPoint(point)) {
                            results.add(body);
                        }
                    }
            );
        });
        return results;
    }

    public List<Body> pointTestSorted(Vec2 point) {
        List<Body> result = pointTest(point);
        result.sort(Comparator.comparingInt(a -> ((BodyUserObj) a.getUserData()).getZLevel()));
        result = Lists.reverse(result);
        return result;
    }

    public boolean isPointOnAnySelected(Vec2 point) {
        if (!isAnySelected()) {
            return false;
        }
        List<Body> results = pointTestSorted(point);
        if (results.isEmpty()) {
            return false;
        }
        return isSelected(results.get(0));
    }

    public Collection<Body> contactTest(Body pco) { //TODO
        Collection<Body> results = new HashSet<>();
        Collision collision = getSpace().getPool().getCollision();

        Body body = getSpace().getBodyList();
        while (body != null) {
            if (!(body.getUserData() instanceof BodyUserObj)) { // to avoid temporary bodies
                body = body.m_next;
                continue;
            }
            Fixture fa = body.getFixtureList();
            while (fa != null) {
                Shape sa = fa.getShape();

                Fixture fb = pco.getFixtureList();
                boolean didCollide = false;
                while (fb != null) {
                    Shape sb = fb.getShape();
                    if (collision.testOverlap(sa, 0, sb, 0, body.getTransform(), pco.getTransform())) {
                        didCollide = true;
                        results.add(body);
                        break;
                    }
                    fb = fb.m_next;
                }
                if (didCollide) {
                    break;
                }

                fa = fa.m_next;
            }
            body = body.m_next;
        }

        return results;
    }

    public boolean isSelected(Body pco) {
        return this.selectedObjects.contains(pco);
    }

    public boolean isAnySelected() {
        return !this.selectedObjects.isEmpty();
    }

    public boolean select(Body pco) {
        if (!isSelected(pco)) {
            this.selectedObjects.add(pco);
            return true;
        }
        return false;
    }

    public void selectAll() {
        unselectAll();
        forEachBody(body -> select(body));
    }

    public boolean unselect(Body obj) {
        return this.selectedObjects.remove(obj);
    }

    public void unselectAll() {
        this.selectedObjects.clear();
    }

    public World getSpace() {
        return this.space;
    }

    public void setSpace(World new_space) {
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

    public float getSingleStepLength() {
        return singleStepLength;
    }

    public int nextId() {
        this.currentId++;
        return this.currentId;
    }

    public BodyUserObj getNextUserObj(String name) {
        return new BodyUserObj(InteractivePhysicsSimulator2D.getInstance().nextId(), name);
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.put("space", NBTSerializer.toNBT(this.space));
        nbt.putInt("current_id", this.currentId);
        nbt.putInt("step_mode", this.stepMode.ordinal());
        nbt.putInt("velocity_iterations", this.velocityIterations);
        nbt.putInt("position_iterations", this.positionIterations);
        nbt.putFloat("simulation_speed", this.simulationSpeed);
        nbt.putFloat("single_step_length", this.singleStepLength);
        nbt.put("command_history", this.commandHistory.serializeNBT());
        int[] selected = new int[this.selectedObjects.size()];
        for (int i=0; i<this.selectedObjects.size(); i++) {
            selected[i] = ((BodyUserObj) this.selectedObjects.get(i).getUserData()).getId();
        }
        nbt.putIntArray("selected_objects", selected);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        this.space = NBTSerializer.spaceFromNBT(nbt.getCompound("space"));
        this.currentId = nbt.getInt("current_id");
        this.simulationRunning = false;
        this.stepMode = StepModes.values()[nbt.getInt("step_mode")];
        this.velocityIterations = nbt.getInt("velocity_iterations");
        this.positionIterations = nbt.getInt("position_iterations");
        this.simulationSpeed = nbt.getFloat("simulation_speed");
        this.singleStepLength = nbt.getFloat("single_step_length");
        if (nbt.contains("command_history")) {
            this.commandHistory.deserializeNBT(nbt.getCompound("command_history"));
        }

        this.idBodyMap.clear();
        Body body = this.space.getBodyList();
        while (body != null) {
            if (body.getUserData() instanceof BodyUserObj) {
                this.idBodyMap.put(((BodyUserObj) body.getUserData()).getId(), body);
            }
            body = body.getNext();
        }

        this.selectedObjects.clear();
        for (int id : nbt.getIntArray("selected_objects")) {
            this.selectedObjects.add(getBodyFromId(id));
        }
    }
}
