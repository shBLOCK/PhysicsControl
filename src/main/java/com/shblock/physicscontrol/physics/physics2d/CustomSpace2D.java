package com.shblock.physicscontrol.physics.physics2d;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.SolverType;
import com.jme3.math.Vector3f;
import com.shblock.physicscontrol.physics.util.MyVector2f;
import com.shblock.physicscontrol.physics.util.Vector2f;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.stream.Collectors;

public class CustomSpace2D extends PhysicsSpace {
    public CustomSpace2D(Vector2f worldMin, Vector2f worldMax, BroadphaseType broadphaseType, SolverType solverType) {
        super(worldMin.toVec3(), worldMax.toVec3(), broadphaseType, solverType);
    }

    public CustomSpace2D(Vector2f worldMin, Vector2f worldMax) {
        this(worldMin, worldMax, BroadphaseType.DBVT, SolverType.SI);//TODO: choose BroadphaseType and SolverType through config file
    }

    public CustomSpace2D() {
        this(new Vector2f(-10000F, -10000F), new Vector2f(10000F, 10000F));
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
            Field field = getClass().getDeclaredField("maxSubSteps");
            field.setAccessible(true);
            int result = field.getInt(this);
            field.setAccessible(false);
            return result;
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public float getMaxTimeStep() {
        try {
            Field field = getClass().getDeclaredField("maxTimeStep");
            field.setAccessible(true);
            float result = field.getFloat(this);
            field.setAccessible(false);
            return result;
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        return Float.NaN;
    }
}
