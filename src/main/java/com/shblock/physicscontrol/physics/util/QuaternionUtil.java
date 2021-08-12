package com.shblock.physicscontrol.physics.util;

import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;

public class QuaternionUtil {
    /**
     * get the rotation along z-axis (in radians)
     * @param quaternion the <code>Quaternion</code> object to get from
     * @return the rotation along z-axis (in radians)
     */
    public static double getZRadians(Quaternion quaternion) {
        return quaternion.k() * Math.acos(quaternion.r()) * 2;
    }

    /**
     * set the <code>Quaternion</code> object's value to be only rotate {@param radians} along z-axis
     * @param radians the rotation along z-axis (in radians)
     * @return a new <code>Quaternion</code> object
     */
    public static Quaternion setZRadians(double radians) {
        return new Quaternion(Vector3f.ZP, (float) radians, false);
//        return new Quaternion(0f, 0f, 1f, (float) Math.cos(radians / 2));
    }
}
