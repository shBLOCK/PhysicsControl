package com.shblock.physicscontrol.client;

import com.jme3.bullet.collision.shapes.Box2dShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.GImpactCollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;

public class I18nHelper {
    public static String getCollisionShapeName(CollisionShape shape) {
        if (shape instanceof SphereCollisionShape) {
            return "physicscontrol.gui.sim.name.sphere";
        } else if (shape instanceof Box2dShape) {
            return "physicscontrol.gui.sim.name.box";
        } else if (shape instanceof GImpactCollisionShape) {
            return "physicscontrol.gui.sim.name.polygon";
        }
        return null;
    }
}
