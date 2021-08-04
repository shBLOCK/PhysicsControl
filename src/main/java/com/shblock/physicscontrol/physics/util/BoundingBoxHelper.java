package com.shblock.physicscontrol.physics.util;

import com.jme3.bounding.BoundingBox;
import com.jme3.math.Vector3f;

public class BoundingBoxHelper {
    private static boolean isHigherInAllAxis(Vector3f a, Vector3f b) {
        return a.x > b.x && a.y > b.y && a.z > b.z;
    }

    private static boolean isLowerInAllAxis(Vector3f a, Vector3f b) {
        return a.x < b.x && a.y < b.y && a.z < b.z;
    }

    public static boolean isOverlapping(BoundingBox a, BoundingBox b) {
        return isHigherInAllAxis(a.getMax(null), b.getMin(null)) && isLowerInAllAxis(a.getMin(null), b.getMax(null));
    }

    private static boolean isHigherIn2DAxis(Vector3f a, Vector3f b) {
        return a.x > b.x && a.y > b.y;
    }

    private static boolean isLowerIn2DAxis(Vector3f a, Vector3f b) {
        return a.x < b.x && a.y < b.y;
    }

    public static boolean isOverlapping2D(BoundingBox a, BoundingBox b) {
        return isHigherIn2DAxis(a.getMax(null), b.getMin(null)) && isLowerIn2DAxis(a.getMin(null), b.getMax(null));
    }

    public static String toString(BoundingBox bb) {
        return "BoundingBox( min: " + bb.getMin(null) + ", max: " + bb.getMax(null) + " )";
    }
}
