package com.shblock.physicscontrol.physics.util;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.SolverInfo;
import com.jme3.bullet.SolverType;
import com.jme3.bullet.collision.Activation;
import com.jme3.bullet.collision.AfMode;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.math.Matrix3f;
import com.jme3.math.Vector3f;
import com.shblock.physicscontrol.physics.physics2d.CustomRigidBody2D;
import com.shblock.physicscontrol.physics.physics2d.CustomWorld2D;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.FloatNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class NBTHelper {
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
            nbt.add(FloatNBT.valueOf(matrix.get(i, i / 3)));
        }
        return nbt;
    }

    public static Matrix3f matrix3FromNBT(ListNBT nbt) {
        Matrix3f matrix = new Matrix3f();
        for (int i=0;i<9;i++) {
            matrix.set(i, i / 3, nbt.getFloat(i));
        }
        return matrix;
    }

    public static CompoundNBT toNBT(CollisionShape shape) {

    }

    public static CollisionShape shapeFromNBT(CompoundNBT nbt) {

    }

    public static CompoundNBT toNBT(CustomRigidBody2D body) {
        CompoundNBT nbt = new CompoundNBT();
        nbt.put("shape", toNBT(body.getCollisionShape()));
        nbt.putFloat("mass", body.getMass());
        //don't have to store scale because it's in the shape
        nbt.putFloat("angular_factor", body.getAngularFactor());
        nbt.put("linear_factor", toNBT(body.getLinearFactor((Vector2f) null)));
        nbt.put("gravity", toNBT(body.getGravity((Vector2f) null)));
        nbt.put("location", toNBT(body.getPhysicsLocation((Vector2f) null)));
        nbt.put("rotation", toNBT(body.getPhysicsRotationMatrix(null)));
        nbt.put("linear_velocity", toNBT(body.getLinearVelocity((Vector2f) null)));
        nbt.putFloat("angular_velocity", body.getAngularVelocity());
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
        return nbt;
    }

    public static CustomRigidBody2D body2dFromNBT(CompoundNBT nbt) {
        CustomRigidBody2D body = new CustomRigidBody2D(shapeFromNBT(nbt.getCompound("shape")), nbt.getFloat("mass"));
        //don't have to store scale because it's in the shape
        body.setAngularFactor(nbt.getFloat("angular_factor"));
        body.setLinearFactor(vec2FromNBT(nbt.getList("linear_factor", Constants.NBT.TAG_FLOAT)));
        body.setGravity(vec2FromNBT(nbt.getList("gravity", Constants.NBT.TAG_FLOAT)));
        body.setPhysicsLocation(vec2FromNBT(nbt.getList("location", Constants.NBT.TAG_FLOAT)));
        body.setPhysicsRotation(matrix3FromNBT(nbt.getList("rotation", Constants.NBT.TAG_FLOAT)));
        body.setLinearVelocity(vec2FromNBT(nbt.getList("linear_velocity", Constants.NBT.TAG_FLOAT)));
        body.setAngularVelocity(nbt.getFloat("angular_velocity"));
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
        return body;
    }

    private static ListNBT writeAllBody2D(Collection<CustomRigidBody2D> collection) {
        ListNBT nbt = new ListNBT();
        for (CustomRigidBody2D body : collection) {
            nbt.add(toNBT(body));
        }
        return nbt;
    }

    //this function also read the ignore list
    private static CustomRigidBody2D[] readAllBody2D(ListNBT list_nbt) {
        int size = list_nbt.size();
        CustomRigidBody2D[] bodies = new CustomRigidBody2D[size];
        long[] ids = new long[size];
        long[][] ignores = new long[size][];
        for (int i=0;i<size;i++) {
            CompoundNBT nbt = list_nbt.getCompound(i);
            bodies[i] = body2dFromNBT(nbt);
            ids[i] = nbt.getLong("id");
            ignores[i] = nbt.getLongArray("ignored_ids");
        }
        for (int i=0;i<size;i++) {
            for (long ignored_id : ignores[i]) {
                bodies[i].addToIgnoreList(bodies[Arrays.binarySearch(ids, ignored_id)]);
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

    public static CompoundNBT toNBT(CustomWorld2D world) {
        CompoundNBT nbt = new CompoundNBT();
        nbt.put("world_min", toNBT(world.getWorldMin((Vector2f) null)));
        nbt.put("world_max", toNBT(world.getWorldMax((Vector2f) null)));
        nbt.putInt("broad_phase_type", world.getBroadphaseType().ordinal());
        nbt.putInt("solver_type", world.getSolverType().ordinal());
        nbt.put("solver_info", toNBT(world.getSolverInfo()));
        nbt.put("gravity", toNBT(world.getGravity((Vector2f) null)));
        nbt.putFloat("accuracy", world.getAccuracy());
        nbt.putInt("max_sub_steps", world.getMaxSubSteps());
        nbt.putFloat("max_time_step", world.getMaxTimeStep());
        nbt.putInt("ray_test_flags", world.getRayTestFlags());
        nbt.put("2d_rigid_body_list", writeAllBody2D(world.get2DRigidBodyList()));
        return nbt;
    }

    public static CustomWorld2D world2FromNBT(CompoundNBT nbt) {
        CustomWorld2D world = new CustomWorld2D(
                vec2FromNBT(nbt.getList("world_min", Constants.NBT.TAG_FLOAT)),
                vec2FromNBT(nbt.getList("world_max", Constants.NBT.TAG_FLOAT)),
                PhysicsSpace.BroadphaseType.values()[nbt.getInt("broad_phase_type")],
                SolverType.values()[nbt.getInt("solver_type")]
        );
        solverInfoFromNBT(nbt.getCompound("solver_info"), world.getSolverInfo());
        world.setGravity(vec2FromNBT(nbt.getList("gravity", Constants.NBT.TAG_FLOAT)));
        world.setAccuracy(nbt.getFloat("accuracy"));
        world.setMaxSubSteps(nbt.getInt("max_sub_steps"));
        world.setMaxTimeStep(nbt.getFloat("max_time_step"));
        world.setRayTestFlags(nbt.getInt("ray_test_flags"));
        CustomRigidBody2D[] bodies = readAllBody2D(nbt.getList("2d_rigid_body_list", Constants.NBT.TAG_COMPOUND));
        Arrays.stream(bodies).forEach(world::addCollisionObject); //add all rigid bodies
        worl
        return world;
    }
}
