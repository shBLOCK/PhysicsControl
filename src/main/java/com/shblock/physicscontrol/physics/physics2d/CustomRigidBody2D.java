package com.shblock.physicscontrol.physics.physics2d;

import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Vector3f;
import com.shblock.physicscontrol.physics.math.BoundingBox2D;
import com.shblock.physicscontrol.physics.math.MyVector2f;
import com.shblock.physicscontrol.physics.math.QuaternionUtil;
import com.shblock.physicscontrol.physics.math.Vector2f;

import javax.annotation.Nullable;

public class CustomRigidBody2D extends PhysicsRigidBody {
    public CustomRigidBody2D(CollisionShape shape) {
        super(shape);
        init2D();
    }

    public CustomRigidBody2D(CollisionShape shape, float mass) {
        super(shape, mass);
        init2D();
    }

    /**
     * setup the object for 2D physics
     */
    public void init2D() {
        setLinearFactor(new Vector3f(1f, 1f, 0f));
        setAngularFactor(new Vector3f(1f, 0f, 0f));
    }

    // *************************************************************************
    // Util methods

    /**
     * Store <code>Vector3f</code>'s x and y value to a <code>Vector2f</code> object and return it.
     * @param result <code>Vector3f</code> object to store from.
     * @param storeResult <code>Vector2f</code> object to store to (can be null).
     * @return the stored <code>Vector2f</code> object, or a new object if storeResult is null
     */
    private Vector2f storeToVec2(Vector3f result, @Nullable Vector2f storeResult) {
        if (storeResult == null) {
            storeResult = new Vector2f();
        }
        storeResult.x = result.x;
        storeResult.y = result.y;
        return storeResult;
    }

    // *************************************************************************
    // PhysicsCollisionObject

    public BoundingBox2D boundingBox(BoundingBox2D storeResult) {
        if (storeResult == null) {
            storeResult = new BoundingBox2D();
        }
        BoundingBox result = super.boundingBox(null);
        storeResult.setMinMax(new Vector2f(result.getMin(null)), new Vector2f(result.getMax(null)));
        return storeResult;
    }

    public Vector2f getAnisotropicFriction(Vector2f storeResult) {
        return storeToVec2(super.getAnisotropicFriction(null), storeResult);
    }

    public Vector2f getPhysicsLocation(Vector2f storeResult) {
        return storeToVec2(super.getPhysicsLocation(null), storeResult);
    }

    public double getPhysicsRotation() {
        return QuaternionUtil.getXRadians(getPhysicsRotation(null));
    }

    public Vector2f getScale(Vector2f storeResult) {
        return storeToVec2(super.getScale(null), storeResult);
    }

    public void setAnisotropicFriction(Vector2f components, int mode) {
        super.setAnisotropicFriction(components.toVec3(), mode);
    }

    public void setLocationAndBasis(Vector2f centerLocation, double radians) {
        super.setLocationAndBasis(centerLocation.toVec3(), QuaternionUtil.setXRadians(null, radians).toRotationMatrix());
    }

    // *************************************************************************
    // PhysicsRigidBody

    public void applyCentralForce(Vector2f force) {
        super.applyCentralForce(force.toVec3());
    }

    public void applyCentralImpulse(Vector2f impulse) {
        super.applyCentralImpulse(impulse.toVec3());
    }

    public void applyForce(Vector2f force, Vector2f offset) {
        super.applyForce(force.toVec3(), offset.toVec3());
    }

    public void applyImpulse(Vector2f impulse, Vector2f offset) {
        super.applyImpulse(impulse.toVec3(), offset.toVec3());
    }

    public void applyTorque(float torque) {
        super.applyTorque(new Vector3f(torque, 0f, 0f));
    }

    public void applyTorqueImpulse(float torqueImpulse) {
        super.applyTorqueImpulse(new Vector3f(torqueImpulse, 0f, 0f));
    }

    public float getAngularFactor() {
        return super.getAngularFactor(null).getX();
    }

    public float getAngularVelocity() {
        return super.getAngularVelocity(null).getX();
    }

    public float getAngularVelocityLocal() {
        return super.getAngularVelocityLocal(null).getX();
    }

    public Vector2f getInverseInertiaLocal(Vector2f storeResult) {
        return storeToVec2(super.getInverseInertiaLocal(null), storeResult);
    }

//    public Matrix3f getInverseInertiaWorld(Matrix3f storeResult) { //TODO: Convert to 2D?
//        return super.getInverseInertiaWorld(storeResult);
//    }

    public Vector2f getLinearFactor(Vector2f storeResult) {
        return storeToVec2(super.getLinearFactor(null), storeResult);
    }

    public Vector2f getLinearVelocity(Vector2f storeResult) {
        return storeToVec2(super.getLinearVelocity(null), storeResult);
    }

    @Override
    public double kineticEnergy() {
        assert isDynamic();

        double mv2 = mass * getSquaredSpeed();

        double xx = getAngularVelocityLocal();
        Vector2f invI = getInverseInertiaLocal((Vector2f) null);
        double iw2 = xx * xx / invI.x;

        double result = (mv2 + iw2) / 2.0;

        assert result >= 0.0 : result;
        return result;
    }

    public double mechanicalEnergy() {
        assert isDynamic();

        Vector2f gravity = getGravity((Vector2f) null);
        Vector2f location = getPhysicsLocation((Vector2f) null);
        double potentialEnergy = -mass * MyVector2f.dot(gravity, location);

        return potentialEnergy + kineticEnergy();
    }

    @Override
    public void setAngularFactor(float factor) {
        setAngularFactor(new Vector3f(factor, 0f, 0f));
    }

    public void setInverseInertiaLocal(Vector2f inverseInertia) {
        super.setInverseInertiaLocal(inverseInertia.toVec3());
    }

    public void setLinearFactor(Vector2f factor) {
        super.setLinearFactor(factor.toVec3());
    }

    public void setLinearVelocity(Vector2f velocity) {
        super.setLinearVelocity(velocity.toVec3());
    }

    public void setPhysicsRotation(double radians) {
        super.setPhysicsRotation(QuaternionUtil.setXRadians(null, radians));
    }

    public void setPhysicsScale(Vector2f newScale) {
        super.setPhysicsScale(newScale.toVec3());
    }

    public Vector2f totalAppliedForce(Vector2f storeResult) {
        return storeToVec2(super.totalAppliedForce(null), storeResult);
    }

    public float totalAppliedTorque() {
        return super.totalAppliedTorque(null).getX();
    }

    public Vector2f getGravity(Vector2f storeResult) {
        return storeToVec2(super.getGravity(null), storeResult);
    }

    public void setGravity(Vector2f acceleration) {
        super.setGravity(acceleration.toVec3());
    }

    public void setPhysicsLocation(Vector2f location) {
        super.setPhysicsLocation(location.toVec3());
    }
}
