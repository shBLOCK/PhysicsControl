package com.shblock.physicscontrol.physics.util;

import com.jme3.math.Quaternion;

import javax.annotation.Nullable;

public class QuaternionUtil {
    /**
     * get the rotation along z-axis (in radians)
     * @param quaternion the <code>Quaternion</code> object to get from
     * @return the rotation along z-axis (in radians)
     */
    public static double getZRadians(Quaternion quaternion) {
        return quaternion.getY() * Math.acos(quaternion.getW()) * 2;
    }

    /**
     * set the <code>Quaternion</code> object's value to be only rotate {@param radians} along z-axis
     * @param storeResult the <code>Quaternion</code> object to store the result (can be null)
     * @param radians the rotation along z-axis (in radians)
     * @return the changed <code>Quaternion</code> object, or a new object if {@param storeResult} is null
     */
    public static Quaternion setZRadians(@Nullable Quaternion storeResult, double radians) {
        if (storeResult == null) {
            storeResult = new Quaternion();
        }
        storeResult.set(0f, 0f, 1f, (float) Math.cos(radians / 2));
        return storeResult;
    }
}
