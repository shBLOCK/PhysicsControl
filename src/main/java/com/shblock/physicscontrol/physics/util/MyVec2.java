package com.shblock.physicscontrol.physics.util;

import org.jbox2d.common.Vec2;

public class MyVec2 {
    public static double angle(Vec2 a, Vec2 b) {
        return Math.acos((a.x * b.x + a.y * b.y) / (a.length() * b.length()));
    }

    public static Vec2 divide(Vec2 a, Vec2 b) {
        return new Vec2(a.x / b.x, a.y / b.y);
    }

    public static Vec2 divideLocal(Vec2 a, Vec2 b) {
        return a.set(a.x / b.x, a.y / b.y);
    }

    public static Vec2 divide(Vec2 a, float b) {
        return new Vec2(a.x / b, a.y / b);
    }

    public static Vec2 divideLocal(Vec2 a, float b) {
        return a.set(a.x / b, a.y / b);
    }

    public static Vec2 divide(Vec2 a, double b) {
        return new Vec2(a.x / b, a.y / b);
    }

    public static Vec2 divideLocal(Vec2 a, double b) {
        return a.set((float) (a.x / b), (float) (a.y / b));
    }
}
