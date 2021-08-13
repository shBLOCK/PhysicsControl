package com.shblock.physicscontrol.physics.util;

import org.jbox2d.collision.AABB;
import org.jbox2d.common.Vec2;

public class AABBHelper {
    private static boolean isHigherInAllAxis(Vec2 a, Vec2 b) {
        return a.x > b.x && a.y > b.y;
    }

    private static boolean isLowerInAllAxis(Vec2 a, Vec2 b) {
        return a.x < b.x && a.y < b.y;
    }

    public static boolean isOverlapping2D(AABB a, AABB b) {
        return isHigherInAllAxis(a.upperBound, b.lowerBound) && isLowerInAllAxis(a.lowerBound, b.upperBound);
    }
}
