package com.shblock.physicscontrol.physics.util;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.SolverInfo;
import com.jme3.bullet.SolverType;
import com.jme3.bullet.collision.Activation;
import com.jme3.bullet.collision.AfMode;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.infos.CompoundMesh;
import com.jme3.bullet.collision.shapes.infos.IndexedMesh;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Matrix3f;
import com.jme3.math.Plane;
import com.jme3.math.Vector3f;
import com.shblock.physicscontrol.client.InteractivePhysicsSimulator2D;
import com.shblock.physicscontrol.command.AbstractCommand;
import com.shblock.physicscontrol.command.CommandHistory;
import com.shblock.physicscontrol.command.CommandSerializer;
import com.shblock.physicscontrol.physics.UserObjBase;
import com.shblock.physicscontrol.physics.physics2d.CollisionObjectUserObj2D;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.FloatNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

public class NBTSerializer {
    public static ListNBT toNBT(Vector3f vec) {
        ListNBT nbt = new ListNBT();
        nbt.add(FloatNBT.valueOf(vec.x));
        nbt.add(FloatNBT.valueOf(vec.y));
        nbt.add(FloatNBT.valueOf(vec.z));
        return nbt;
    }

    public static Vector3f vec3FromNBT(ListNBT nbt) {
        return new Vector3f(
                nbt.getFloat(0),
                nbt.getFloat(1),
                nbt.getFloat(2)
        );
    }

    public static ListNBT toNBT(Vector2f vec) {
        ListNBT nbt = new ListNBT();
        nbt.add(FloatNBT.valueOf(vec.x));
        nbt.add(FloatNBT.valueOf(vec.y));
        return nbt;
    }

    public static Vector2f vec2FromNBT(ListNBT nbt) {
        return new Vector2f(
                nbt.getFloat(0),
                nbt.getFloat(1)
        );
    }

    public static ListNBT toNBT(Matrix3f matrix) {
        ListNBT nbt = new ListNBT();
        for (int i=0;i<9;i++) {
            nbt.add(FloatNBT.valueOf(matrix. get(i % 3,  i / 3)));
        }
        return nbt;
    }

    public static Matrix3f matrix3FromNBT(ListNBT nbt) {
        Matrix3f matrix = new Matrix3f();
        for (int i=0;i<9;i++) {
            matrix.set(i % 3, i / 3, nbt.getFloat(i));
        }
        return matrix;
    }

    public static CompoundNBT toNBT(IndexedMesh mesh) {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putIntArray("indices", mesh.copyIndices().array());
        ListNBT vertexes = new ListNBT();
        for (float v : mesh.copyVertexPositions().array()) {
            vertexes.add(FloatNBT.valueOf(v));
        }
        nbt.put("vertexes", vertexes);
        return nbt;
    }

    public static IndexedMesh iMeshFromNBT(CompoundNBT nbt) {
        ListNBT vertexes_nbt = nbt.getList("vertexes", Constants.NBT.TAG_FLOAT);
        Vector3f[] vertexes = new Vector3f[vertexes_nbt.size() / 3];
        for (int i=0; i<vertexes_nbt.size()/3; i++) {
            vertexes[i] = new Vector3f(
                    vertexes_nbt.getFloat(i * 3),
                    vertexes_nbt.getFloat(i * 3 + 1),
                    vertexes_nbt.getFloat(i * 3 + 2)
            );
        }
        return new IndexedMesh(vertexes, nbt.getIntArray("indices"));
    }

    public static CompoundNBT toNBT(CompoundMesh mesh) {
        CompoundNBT nbt = new CompoundNBT();
        nbt.put("scale", toNBT(mesh.getScale(null)));
        ListNBT sub_meshes = new ListNBT();
        try {
            Field field = mesh.getClass().getDeclaredField("submeshes");
            field.setAccessible(true);
            for (IndexedMesh sub_mesh : (ArrayList<IndexedMesh>) field.get(mesh)) {
                sub_meshes.add(toNBT(sub_mesh));
            }
            field.setAccessible(false);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        nbt.put("sub_meshes", sub_meshes);
        return nbt;
    }

    public static CompoundMesh cMeshFromNBT(CompoundNBT nbt) {
        CompoundMesh mesh = new CompoundMesh();
        mesh.setScale(vec3FromNBT(nbt.getList("scale", Constants.NBT.TAG_FLOAT)));
        nbt.getList("sub_meshes", Constants.NBT.TAG_COMPOUND).forEach(
                mesh_nbt -> mesh.add(iMeshFromNBT((CompoundNBT) mesh_nbt))
        );
        return mesh;
    }

    public static CompoundNBT toNBT(Plane plane) {
        CompoundNBT nbt = new CompoundNBT();
        nbt.put("normal", toNBT(plane.getNormal()));
        nbt.putFloat("constant", plane.getConstant());
        return nbt;
    }

    public static Plane planeFromNBT(CompoundNBT nbt) {
        return new Plane(
                vec3FromNBT(nbt.getList("normal", Constants.NBT.TAG_FLOAT)),
                nbt.getFloat("constant")
        );
    }

    public static CompoundNBT toNBT(CollisionShape shape) {
        return CollisionShapeSerializer.toNBT(shape);
    }

    public static CollisionShape shapeFromNBT(CompoundNBT nbt) {
        return CollisionShapeSerializer.fromNBT(nbt);
    }

    public static CompoundNBT toNBT(PhysicsRigidBody body) {
        CompoundNBT nbt = new CompoundNBT();
        nbt.put("shape", toNBT(body.getCollisionShape()));
        nbt.putFloat("mass", body.getMass());
        //don't have to store scale because it's in the shape
        nbt.put("angular_factor", toNBT(body.getAngularFactor(null)));
        nbt.put("linear_factor", toNBT(body.getLinearFactor(null)));
        nbt.put("gravity", toNBT(body.getGravity(null)));
        nbt.put("location", toNBT(body.getPhysicsLocation(null)));
        nbt.put("rotation", toNBT(body.getPhysicsRotationMatrix(null)));
        nbt.put("linear_velocity", toNBT(body.getLinearVelocity(null)));
        nbt.put("angular_velocity", toNBT(body.getAngularVelocity(null)));
        nbt.putFloat("friction", body.getFriction());
        nbt.putFloat("rolling_friction", body.getRollingFriction());
        nbt.putFloat("spinning_friction", body.getSpinningFriction());
        nbt.putFloat("linear_damping", body.getLinearDamping());
        nbt.putFloat("angular_damping", body.getAngularDamping());
        //TODO: InverseInertia?
        nbt.putBoolean("kinematic", body.isKinematic());
        nbt.putBoolean("enable_sleep", body.getActivationState() == Activation.active);
        nbt.putFloat("linear_sleeping_threshold", body.getLinearSleepingThreshold());
        nbt.putFloat("angular_sleeping_threshold", body.getAngularSleepingThreshold());
        nbt.putFloat("ccd_motion_threshold", body.getCcdMotionThreshold());
        nbt.putFloat("ccd_swept_sphere_radius", body.getCcdSweptSphereRadius());
        nbt.putFloat("deactivation_time", body.getDeactivationTime());
        nbt.putFloat("contact_damping", body.getContactDamping());
        nbt.putFloat("contact_processing_threshold", body.getContactProcessingThreshold());
        nbt.putFloat("contact_stiffness", body.getContactStiffness());
        nbt.putFloat("restitution", body.getRestitution());
        nbt.putBoolean("protect_gravity", body.isGravityProtected());
        nbt.putInt("collision_group", body.getCollisionGroup());
        nbt.putInt("collide_with_groups", body.getCollideWithGroups());
        nbt.putBoolean("active", body.isActive());
        if (body.hasAnisotropicFriction(AfMode.basic)) {
            nbt.put("af_basic", toNBT(body.getAnisotropicFriction(null)));
        } else if (body.hasAnisotropicFriction(AfMode.rolling)) {
            nbt.put("af_rolling", toNBT(body.getAnisotropicFriction(null)));
        }
        nbt.putLongArray("ignored_ids", body.listIgnoredIds());
        nbt.putLong("id", body.nativeId()); //just for ignore list
        if (body.getUserObject() != null) {
            nbt.put("user_obj", ((UserObjBase) body.getUserObject()).serializeNBT());
        }
        return nbt;
    }

    public static PhysicsRigidBody bodyFromNBT(CompoundNBT nbt) {
        PhysicsRigidBody body = new PhysicsRigidBody(shapeFromNBT(nbt.getCompound("shape")), nbt.getFloat("mass"));
        //don't have to store scale because it's in the shape
        body.setAngularFactor(vec3FromNBT(nbt.getList("angular_factor", Constants.NBT.TAG_FLOAT)));
        body.setLinearFactor(vec3FromNBT(nbt.getList("linear_factor", Constants.NBT.TAG_FLOAT)));
        PhysicsRigidBody.logger2.setLevel(Level.OFF);
        body.setGravity(vec3FromNBT(nbt.getList("gravity", Constants.NBT.TAG_FLOAT)));
        PhysicsRigidBody.logger2.setLevel(Level.ALL);
        body.setPhysicsLocation(vec3FromNBT(nbt.getList("location", Constants.NBT.TAG_FLOAT)));
        body.setPhysicsRotation(matrix3FromNBT(nbt.getList("rotation", Constants.NBT.TAG_FLOAT)));
        body.setLinearVelocity(vec3FromNBT(nbt.getList("linear_velocity", Constants.NBT.TAG_FLOAT)));
        body.setAngularVelocity(vec3FromNBT(nbt.getList("angular_velocity", Constants.NBT.TAG_FLOAT)));
        body.setFriction(nbt.getFloat("friction"));
        body.setRollingFriction(nbt.getFloat("rolling_friction"));
        body.setSpinningFriction(nbt.getFloat("spinning_friction"));
        body.setLinearDamping(nbt.getFloat("linear_damping"));
        body.setAngularDamping(nbt.getFloat("angular_damping"));
        //TODO: InverseInertia?
        body.setKinematic(nbt.getBoolean("kinematic"));
        body.setEnableSleep(nbt.getBoolean("enable_sleep"));
        body.setLinearSleepingThreshold(nbt.getFloat("linear_sleeping_threshold"));
        body.setAngularSleepingThreshold(nbt.getFloat("angular_sleeping_threshold"));
        body.setCcdMotionThreshold(nbt.getFloat("ccd_motion_threshold"));
        body.setCcdSweptSphereRadius(nbt.getFloat("ccd_swept_sphere_radius"));
        body.setDeactivationTime(nbt.getFloat("deactivation_time"));
        body.setContactDamping(nbt.getFloat("contact_damping"));
        body.setContactProcessingThreshold(nbt.getFloat("contact_processing_threshold"));
        body.setContactStiffness(nbt.getFloat("contact_stiffness"));
//        body.setContactResponse(nbt.getBoolean("contact_response"));
        body.setRestitution(nbt.getFloat("restitution"));
        body.setProtectGravity(nbt.getBoolean("protect_gravity"));
        body.setCollisionGroup(nbt.getInt("collision_group"));
        body.setCollideWithGroups(nbt.getInt("collide_with_groups"));
        if (nbt.getBoolean("active")) {
            body.activate(true);
        }
        if (nbt.contains("af_basic")) {
            body.setAnisotropicFriction(vec3FromNBT(nbt.getList("af_basic", Constants.NBT.TAG_FLOAT)), AfMode.basic);
        } else if (nbt.contains("af_rolling")) {
            body.setAnisotropicFriction(vec3FromNBT(nbt.getList("af_rolling", Constants.NBT.TAG_FLOAT)), AfMode.rolling);
        }
        if (nbt.contains("user_obj")) {
            CollisionObjectUserObj2D obj = new CollisionObjectUserObj2D(0); //TODO: make it not only deserialize to CollisionObjectUserObj2D
            obj.deserializeNBT(nbt.getCompound("user_obj"));
            body.setUserObject(obj);
        }
        return body;
    }

    private static ListNBT writeAllBody(Collection<PhysicsRigidBody> collection) {
        ListNBT nbt = new ListNBT();
        for (PhysicsRigidBody body : collection) {
            nbt.add(toNBT(body));
        }
        return nbt;
    }

    //this function also read the ignore list
    private static PhysicsRigidBody[] readAllBody(ListNBT list_nbt) {
        int size = list_nbt.size();
        PhysicsRigidBody[] bodies = new PhysicsRigidBody[size];
        long[] ids = new long[size];
        long[][] ignores = new long[size][];
        for (int i=0; i < size; i++) {
            CompoundNBT nbt = list_nbt.getCompound(i);
            bodies[i] = bodyFromNBT(nbt);
            ids[i] = nbt.getLong("id");
            ignores[i] = nbt.getLongArray("ignored_ids");
        }
        for (int i=0; i<size; i++) {
            for (long ignored_id : ignores[i]) {
                for (int index=0; index < ids.length; index++) {
                    if (ids[index] == ignored_id) {
                        bodies[i].addToIgnoreList(bodies[index]);
                        break;
                    }
                }
            }
        }
        return bodies;
    }

    public static CompoundNBT toNBT(SolverInfo info) {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putFloat("global_cfm", info.globalCfm());
        nbt.putInt("min_batch", info.minBatch());
        nbt.putInt("mode", info.mode());
        nbt.putInt("num_iterations", info.numIterations());
        nbt.putBoolean("split_impulse_enabled", info.isSplitImpulseEnabled());
        nbt.putFloat("split_impulse_erp", info.splitImpulseErp());
        nbt.putFloat("split_impulse_threshold", info.splitImpulseThreshold());
        return nbt;
    }

    public static SolverInfo solverInfoFromNBT(CompoundNBT nbt, SolverInfo storeResult) {
        storeResult.setGlobalCfm(nbt.getFloat("global_cfm"));
        storeResult.setMinBatch(nbt.getInt("min_batch"));
        storeResult.setMode(nbt.getInt("mode"));
        storeResult.setNumIterations(nbt.getInt("num_iterations"));
        storeResult.setSplitImpulseEnabled(nbt.getBoolean("split_impulse_enabled"));
        storeResult.setSplitImpulseErp(nbt.getFloat("split_impulse_erp"));
        storeResult.setSplitImpulseThreshold(nbt.getFloat("split_impulse_threshold"));
        return storeResult;
    }

    public static CompoundNBT toNBT(PhysicsSpace world) {
        CompoundNBT nbt = new CompoundNBT();
        nbt.put("world_min", toNBT(world.getWorldMin(null)));
        nbt.put("world_max", toNBT(world.getWorldMax(null)));
        nbt.putInt("broad_phase_type", world.getBroadphaseType().ordinal());
        nbt.putInt("solver_type", world.getSolverType().ordinal());
        nbt.put("solver_info", toNBT(world.getSolverInfo()));
        nbt.put("gravity", toNBT(world.getGravity(null)));
        nbt.putFloat("accuracy", world.getAccuracy());
        nbt.putInt("max_sub_steps", world.maxSubSteps());
        nbt.putFloat("max_time_step", world.maxTimeStep());
        nbt.putInt("ray_test_flags", world.getRayTestFlags());
        nbt.put("rigid_body_list", writeAllBody(world.getRigidBodyList()));
        return nbt;
    }

    public static PhysicsSpace physicsSpaceFromNBT(CompoundNBT nbt) {
        PhysicsSpace world = new PhysicsSpace(
                vec3FromNBT(nbt.getList("world_min", Constants.NBT.TAG_FLOAT)),
                vec3FromNBT(nbt.getList("world_max", Constants.NBT.TAG_FLOAT)),
                PhysicsSpace.BroadphaseType.values()[nbt.getInt("broad_phase_type")],
                SolverType.values()[nbt.getInt("solver_type")]
        );
        solverInfoFromNBT(nbt.getCompound("solver_info"), world.getSolverInfo());
        world.setGravity(vec3FromNBT(nbt.getList("gravity", Constants.NBT.TAG_FLOAT)));
        world.setAccuracy(nbt.getFloat("accuracy"));
        world.setMaxSubSteps(nbt.getInt("max_sub_steps"));
        world.setMaxTimeStep(nbt.getFloat("max_time_step"));
        world.setRayTestFlags(nbt.getInt("ray_test_flags"));
        PhysicsRigidBody[] bodies = readAllBody(nbt.getList("rigid_body_list", Constants.NBT.TAG_COMPOUND));
        Arrays.stream(bodies).forEach(world::addCollisionObject); //add all rigid bodies
        return world;
    }

//    public static CompoundNBT toNBT(CommandHistory history) {
//        CompoundNBT nbt = new CompoundNBT();
//        ListNBT list = new ListNBT();
//        for (AbstractCommand command : history.getList()) {
//            list.add(CommandSerializer.toNBT(command));
//        }
//        nbt.put("list", list);
//        nbt.putInt("max", history.getMaxHistory());
//        nbt.putInt("pointer", history.getPointer());
//        return nbt;
//    }
//
//    public static CommandHistory historyFromNBT(CompoundNBT nbt) {
//        CommandHistory history = new CommandHistory();
//        List<AbstractCommand> list = new ArrayList<>();
//        ListNBT list_nbt = nbt.getList("list", Constants.NBT.TAG_COMPOUND);
//        for (int i=0; i<list_nbt.size(); i++) {
//            list.add(CommandSerializer.fromNBT(list_nbt.getCompound(i)));
//        }
//        history.setList(list);
//        history.setMaxHistory(nbt.getInt("max"));
//        history.setPointer(nbt.getInt("pointer"));
//        return history;
//    }
//
//    public static CompoundNBT toNBT(InteractivePhysicsSimulator2D simulator) {
//        CompoundNBT nbt = new CompoundNBT();
//        nbt.put("space", toNBT(simulator.getSpace()));
//        nbt.putInt("current_id", simulator.getCurrentId());
//        nbt.putInt("step_mode", simulator.getStepMode().ordinal());
//        nbt.putFloat("simulation_speed", simulator.getSimulationSpeed());
////        nbt.put("command_history", toNBT(simulator.getCommandHistory()));
//        return nbt;
//    }
//
//    public static InteractivePhysicsSimulator2D simulator2DFromNBT(CompoundNBT nbt) {
//        CommandHistory history = InteractivePhysicsSimulator2D.getInstance().getCommandHistory();
//        InteractivePhysicsSimulator2D simulator = new InteractivePhysicsSimulator2D(physicsSpaceFromNBT(nbt.getCompound("space")));
//        simulator.setCommandHistory(history);
//        simulator.setCurrentId(nbt.getInt("current_id"));
//        simulator.setStepMode(InteractivePhysicsSimulator2D.StepModes.values()[nbt.getInt("step_mode")]);
//        simulator.setSimulationSpeed(nbt.getFloat("simulation_speed"));
////        simulator.setCommandHistory(historyFromNBT(nbt.getCompound("command_history")));
//        return simulator;
//    }
}
