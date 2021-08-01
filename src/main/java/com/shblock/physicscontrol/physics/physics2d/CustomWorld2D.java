package com.shblock.physicscontrol.physics.physics2d;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.bullet.SolverInfo;
import com.jme3.bullet.SolverType;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.joints.Constraint;
import com.jme3.bullet.joints.PhysicsJoint;
import com.jme3.bullet.objects.PhysicsCharacter;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.bullet.objects.PhysicsVehicle;
import com.jme3.math.Vector3f;
import com.shblock.physicscontrol.physics.util.MyVector2f;
import com.shblock.physicscontrol.physics.util.Vector2f;
import jme3utilities.Validate;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class CustomWorld2D extends PhysicsSpace {
    public CustomWorld2D(Vector2f worldMin, Vector2f worldMax, BroadphaseType broadphaseType, SolverType solverType) {
        super(worldMin.toVec3(), worldMax.toVec3(), broadphaseType, solverType);
    }

    public CustomWorld2D(Vector2f worldMin, Vector2f worldMax) {
        super(worldMin.toVec3(), worldMax.toVec3(), BroadphaseType.DBVT, SolverType.SI);//TODO: choose BroadphaseType and SolverType through config file
    }

    // *************************************************************************
    //PhysicsSpace

    public Vector2f getGravity(Vector2f storeResult) {
        return MyVector2f.storeToVec2(getGravity((Vector3f) null), storeResult);
    }

    public Collection<CustomRigidBody2D> get2DRigidBodyList() {
        return super.getRigidBodyList().stream().map(body -> (CustomRigidBody2D) body).collect(Collectors.toList());
    }

    public void setGravity(Vector2f gravity) {
        super.setGravity(gravity.toVec3());
    }

    // *************************************************************************
    // CollisionSpace

    public Vector2f getWorldMax(Vector2f storeResult) {
        return MyVector2f.storeToVec2(super.getWorldMax(null), storeResult);
    }

    public Vector2f getWorldMin(Vector2f storeResult) {
        return MyVector2f.storeToVec2(super.getWorldMin(null), storeResult);
    }

    // *************************************************************************
    // Other

    public int getMaxSubSteps() {
        try {
            return getClass().getDeclaredField("maxSubSteps").getInt(this);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public float getMaxTimeStep() {
        try {
            return getClass().getDeclaredField("maxTimeStep").getFloat(this);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        return Float.NaN;
    }
}
