package com.shblock.physicscontrol.physics.util;

import com.jme3.bullet.collision.shapes.Box2dShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.GImpactCollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.math.Vector3f;

public class ShapeHelper {
    public static double getSurfaceArea2D(CollisionShape shape) {
        if (shape instanceof SphereCollisionShape) {
            return Math.sqrt(((SphereCollisionShape) shape).getRadius()) * Math.PI;
        } else if (shape instanceof Box2dShape) {
            Vector3f halfExtents = ((Box2dShape) shape).getHalfExtents(null);
            return halfExtents.x * 2 * halfExtents.y * 2;
        } else if (shape instanceof GImpactCollisionShape) {
            return 1D; //TODO
        }
        return Double.NaN;
    }
}
