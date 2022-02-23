package com.shblock.physicscontrol.client;

import com.google.common.collect.Lists;
import com.shblock.physicscontrol.PhysicsControl;
import com.shblock.physicscontrol.client.gui.PhysicsSimulator.GuiSimulatorContactListener;
import com.shblock.physicscontrol.command.*;
import com.shblock.physicscontrol.motionsensor.MotionSensorInstance;
import com.shblock.physicscontrol.physics.user_obj.BodyUserObj;
import com.shblock.physicscontrol.physics.user_obj.ElasticGroupUserObj;
import com.shblock.physicscontrol.physics.user_obj.UserObjBase;
import com.shblock.physicscontrol.physics.util.BodyHelper;
import com.shblock.physicscontrol.physics.util.NBTSerializer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import org.apache.logging.log4j.Level;
import org.jbox2d.collision.Collision;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;
import org.jbox2d.dynamics.joints.Joint;
import org.jbox2d.dynamics.joints.JointDef;
import org.jbox2d.dynamics.joints.JointType;
import org.jbox2d.particle.ParticleDef;
import org.jbox2d.particle.ParticleGroup;
import org.jbox2d.particle.ParticleGroupDef;

import java.util.*;
import java.util.function.Consumer;

// Don't call xxxLocal functions from the Gui code!
public class InteractivePhysicsSimulator2D implements INBTSerializable<CompoundNBT> { //TODO: serialize this instead of space
    private static InteractivePhysicsSimulator2D currentInstance;

    private World space;
    private int currentBodyId = -1;
    private int currentGroupId = -1;
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
    private final Map<Integer, ParticleGroup> idGroupMap = new HashMap<>();
    private final List<Body> selectedBodies = new ArrayList<>(); //TODO: store only id when serializing
//    private final Set<Integer> selectedParticles = new HashSet<>();

    public InteractivePhysicsSimulator2D(World space) {
        if (currentInstance != null) {
            throw new RuntimeException("You have to close the last Instance to create a new one!");
        }
        assert currentInstance == null;
        currentInstance = this;
        this.space = space;
        initSpace();
        this.commandHistory = new CommandHistory();
    }

    public static InteractivePhysicsSimulator2D getInstance() {
        return currentInstance;
    }

    public void initSpace() {
        this.space.setContactListener(GuiSimulatorContactListener.getInstance());
        this.space.setSleepingAllowed(false);
        this.space.setAllowSleep(false);
        this.space.setParticleRadius(0.125F);
    }

    public void close() {
        currentInstance = null;
    }

    public void singleStep(int steps) {
        for (int i=0; i<steps; i++) {
            this.space.step(this.singleStepLength, this.velocityIterations, this.positionIterations);
            update();
        }
    }

    private void syncMotionSensorData() {
        forEachBody(body -> {
            BodyUserObj obj = (BodyUserObj) body.getUserData();
            MotionSensorInstance sensor = obj.getMotionSensor();
            if (sensor != null) {
                body.setAwake(true);
                body.setActive(true);

                Vec2 spdVec = new Vec2(sensor.spdX, sensor.spdZ);
                if (body.getType() == BodyType.DYNAMIC) {
                    body.setLinearVelocity(spdVec);
                    body.setAngularVelocity((float) Math.toRadians(sensor.angSpdX));
                } else {
                    setBodyPosLocal(body, body.getPosition().add(spdVec));
                    body.setTransform(body.getPosition(), (float) Math.toRadians(sensor.angleX));
                }
            }
        });
    }

    public void step(float time) {
        syncMotionSensorData();
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
        for (int i=0; i<=this.currentBodyId; i++) { // Because we might delete bodies in the idBodyMap while iterating
            Body body = getBodyFromId(i);
            if (body != null) {
                consumer.accept(body);
            }
        }
    }

    public List<Body> getSelectedBodies() {
        return this.selectedBodies;
    }

    public Body getBodyFromId(int id) {
        return this.idBodyMap.getOrDefault(id, null);
    }

    public Body addBodyLocal(BodyDef bodyDef, FixtureDef fixture) {
        return this.addBodyLocal(bodyDef, new FixtureDef[]{fixture});
    }

    public Body addBodyLocal(BodyDef bodyDef, FixtureDef[] fixtures) {
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

    public Body addBodyLocal(BodyDef bodyDef, CompoundNBT nbt) {
        Body body = getSpace().createBody(bodyDef);
        assert body.getUserData() instanceof BodyUserObj;
        this.idBodyMap.put(((BodyUserObj) body.getUserData()).getId(), body);
        try {
            NBTSerializer.applyFixture(body, nbt);
        } catch (AssertionError e) {
            PhysicsControl.log(Level.WARN, "Failed to create fixture, did you make your polygon too small?");
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
        executeCommand(new CommandSetBodyPos(this.selectedBodies, offset, isFirst));
    }

    public void deleteSelected() {
        executeCommand(new CommandDeleteBodies(this.selectedBodies));
    }

    public CompoundNBT copyBodies(List<Body> bodies, Vec2 mousePos) {
        CompoundNBT nbt = new CompoundNBT();

        ListNBT list = new ListNBT();
        for (Body body : bodies) {
            list.add(NBTSerializer.toNBT(body));
        }
        nbt.put("list", list);

        nbt.put("mouse_pos", NBTSerializer.toNBT(mousePos));
        return nbt;
    }

    public void pasteBodies(CompoundNBT nbt, Vec2 mousePos) {
        executeCommand(new CommandPasteBodies(nbt, mousePos));
    }

    public void pasteBodiesLocal(CompoundNBT nbt, Vec2 mousePos) {
        Vec2 posDelta = mousePos.sub(NBTSerializer.vec2FromNBT(nbt.getCompound("mouse_pos")));

        ListNBT list = nbt.getList("list", Constants.NBT.TAG_COMPOUND);
        List<BodyDef> bodies = new ArrayList<>();
        for (int i=0; i<list.size(); i++) {
            bodies.add(NBTSerializer.bodyFromNBT(list.getCompound(i)));
        }
        unselectAll();
        for (int i=0; i<bodies.size(); i++) {
            BodyDef body = bodies.get(i);
            if (body.userData instanceof BodyUserObj) {
                ((BodyUserObj) body.userData).setId(nextBodyId());
                body.position.addLocal(posDelta);
                Body result = addBodyLocal(body, list.getCompound(i));
                select(result);
            }
        }
    }

    public void changeZLevel(Body pco, int change) {
        executeCommand(new CommandChangeZLevel(change, Lists.newArrayList(pco)));
    }

    public void changeSelectedZLevel(int change) {
        executeCommand(new CommandChangeZLevel(change, this.selectedBodies));
    }

    public void freezeBody(Body body) {
        if (body.getUserData() instanceof BodyUserObj) {
            body.setGravityScale(0F);
            body.setLinearVelocity(new Vec2(0F, 0F));
            body.setAngularVelocity(0F);
            body.setFixedRotation(true);
        }
    }

    public void startMove() {
        for (Body body : this.selectedBodies) {
            freezeBody(body);
        }
    }

    public void unfreezeBody(Body body) {
        if (body.getUserData() instanceof BodyUserObj) {
            body.setGravityScale(1F);
            body.setAwake(true);
            body.setActive(true);
            body.setFixedRotation(false);
        }
    }

    public void stopMove() {
        for (Body body : this.selectedBodies) {
            unfreezeBody(body);
        }
    }

    public Joint addJoint(JointDef def) {
        return getSpace().createJoint(def);
    }

    public void deleteJoint(Joint joint) {
        getSpace().destroyJoint(joint);
    }

    public Joint createBearingAt(Vec2 worldPos, float size) {
        List<Body> bodies = pointTestSorted(worldPos);
        if (bodies.isEmpty())
            return null;
        JointDef def = new JointDef(JointType.REVOLUTE);
        def.bodyA = bodies.get(0);
        def.bodyB = bodies.size() == 1 ? null : bodies.get(1);
        def.userData = size;
        return addJoint(def);
    }

    public ParticleGroup getGroupFromId(int id) {
        return this.idGroupMap.get(id);
    }

    public void addParticleLocal(ParticleDef def) {
        getSpace().createParticle(def);
    }

    public void deleteParticlesLocal(int[] indexes) {
        Set<ParticleGroup> elasticGroupsToUpdate = null;

        for (int index : indexes) {
            getSpace().destroyParticle(index);

            ParticleGroup group = getSpace().getParticleGroupBuffer()[index];
            if (group != null) {
                if (group.getUserData() instanceof ElasticGroupUserObj) {
                    if (elasticGroupsToUpdate == null) {
                        elasticGroupsToUpdate = new HashSet<>();
                    }
                    elasticGroupsToUpdate.add(group);
                }
            }
        }

        if (elasticGroupsToUpdate != null) {
            for (ParticleGroup group : elasticGroupsToUpdate) {
                ((ElasticGroupUserObj) group.getUserData()).buildMesh(getSpace(), group);
            }
            for (ParticleGroup group : elasticGroupsToUpdate) {
                if (group.getParticleCount() < 3) {
                    getSpace().destroyParticlesInGroup(group);
                }
            }
        }
    }

    public void deleteParticleLocal(int index) {
        this.deleteParticlesLocal(new int[]{index});
    }

    public ParticleGroup addParticleGroupLocal(ParticleGroupDef def) {
        def.destroyAutomatically = true; // Other wise we can't delete a particle group.
        ParticleGroup group = getSpace().createParticleGroup(def);
        assert !(group.getUserData() instanceof UserObjBase);
        this.idGroupMap.put(((UserObjBase) group.getUserData()).getId(), group);
        return group;
    }

    public void joinParticleGroupLocal(ParticleGroup a, ParticleGroup b) {
        getSpace().joinParticleGroups(a, b);
        this.idGroupMap.remove(((UserObjBase) b.getUserData()).getId());
    }

    public void deleteParticleGroupLocal(ParticleGroup group) {
        getSpace().destroyParticlesInGroup(group);
        this.idGroupMap.remove(((UserObjBase) group.getUserData()).getId());
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

    public Collection<Body> contactTest(Body pco) {
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
        return this.selectedBodies.contains(pco);
    }

    public boolean isAnySelected() {
        return !this.selectedBodies.isEmpty();
    }

    public boolean select(Body pco) {
        if (!isSelected(pco)) {
            this.selectedBodies.add(pco);
            return true;
        }
        return false;
    }

    public void selectAll() {
        unselectAll();
        forEachBody(body -> select(body));
    }

    public boolean unselect(Body obj) {
        return this.selectedBodies.remove(obj);
    }

    public void unselectAll() {
        this.selectedBodies.clear();
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

    public int getCurrentBodyId() {
        return currentBodyId;
    }

    public void setCurrentBodyId(int currentId) {
        this.currentBodyId = currentId;
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

    public int nextBodyId() {
        this.currentBodyId++;
        return this.currentBodyId;
    }

    public int nextGroupId() {
        this.currentGroupId++;
        return this.currentGroupId;
    }

    public BodyUserObj getNextUserObj(String name) {
        return new BodyUserObj(InteractivePhysicsSimulator2D.getInstance().nextBodyId(), name);
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.put("space", NBTSerializer.toNBT(this.space));
        nbt.putInt("current_body_id", this.currentBodyId);
        nbt.putInt("current_group_id", this.currentGroupId);
        nbt.putInt("step_mode", this.stepMode.ordinal());
        nbt.putInt("velocity_iterations", this.velocityIterations);
        nbt.putInt("position_iterations", this.positionIterations);
        nbt.putFloat("simulation_speed", this.simulationSpeed);
        nbt.putFloat("single_step_length", this.singleStepLength);
        nbt.put("command_history", this.commandHistory.serializeNBT());
        List<Integer> selectedBs = new ArrayList<>();
        for (int i = 0; i<this.selectedBodies.size(); i++) {
            Body body = this.selectedBodies.get(i);
            if (body != null) {
                BodyUserObj obj = (BodyUserObj) body.getUserData();
                if (obj != null) {
                    selectedBs.add(obj.getId());
                }
            }
        }
        nbt.putIntArray("selected_bodies", selectedBs);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        this.space = NBTSerializer.spaceFromNBT(nbt.getCompound("space"));
        initSpace();
        this.currentBodyId = nbt.getInt("current_body_id");
        this.currentGroupId = nbt.getInt("current_group_id");
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

        this.selectedBodies.clear();
        for (int id : nbt.getIntArray("selected_bodies")) {
            this.selectedBodies.add(getBodyFromId(id));
        }
    }
}
