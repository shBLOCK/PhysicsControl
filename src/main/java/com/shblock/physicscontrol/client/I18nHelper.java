package com.shblock.physicscontrol.client;

import net.minecraft.client.resources.I18n;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;

public class I18nHelper {
    public static String getCollisionShapeName(Shape shape) {
        if (shape instanceof CircleShape) {
            return "physicscontrol.gui.sim.name.sphere";
        } else if (shape instanceof PolygonShape) {
            return "physicscontrol.gui.sim.name.polygon";
        }
        return null;
    }

    // For some reason, I18n will replace any format with %s, so I have to write & instead of % in the lang file and replace it back in the code.
    public static String localizeNumFormat(String key) {
        return I18n.get(key).replace('&', '%');
    }
}
