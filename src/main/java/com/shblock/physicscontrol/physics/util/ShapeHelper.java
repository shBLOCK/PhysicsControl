package com.shblock.physicscontrol.physics.util;

import com.shblock.physicscontrol.PhysicsControl;
import org.apache.logging.log4j.Level;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Settings;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.FixtureDef;

import java.util.ArrayList;
import java.util.List;

public class ShapeHelper {
    public static double getSurfaceArea2D(Shape shape) {
        if (shape instanceof CircleShape) {
            CircleShape circle = (CircleShape) shape;
            return Settings.PI * shape.m_radius * shape.m_radius;
        } else if (shape instanceof PolygonShape) {
            return PolygonHelper.calculateArea((PolygonShape) shape);
        }
        return Double.NaN;
    }

    public static List<PolygonShape> buildPolygonShape(List<Vec2> vertexes) {
        List<PolygonShape> results = new ArrayList<>();
        List<Integer> tri_list = PolygonHelper.cutEar(vertexes.toArray(new Vec2[0]));
        if (tri_list == null) {
            results.add(new PolygonShape());
            results.get(0).set(vertexes.toArray(new Vec2[0]), vertexes.size());
            return results;
        }
        for (int i=0; i<tri_list.size()/3; i++) {
            PolygonShape shape = new PolygonShape();
            try {
                shape.set(new Vec2[]{
                        vertexes.get(tri_list.get(i * 3)),
                        vertexes.get(tri_list.get(i * 3 + 1)),
                        vertexes.get(tri_list.get(i * 3 + 2))
                }, 3);
                results.add(shape);
            } catch (AssertionError error) {
                PhysicsControl.log(Level.WARN, "Failed to build a triangle shape for polygon, did you make your polygon too small?");
                return null;
            }
        }
        return results;
    }
}
