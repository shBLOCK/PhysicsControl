package com.shblock.physicscontrol.physics.util;

import com.jme3.math.Vector3f;
import jme3utilities.Validate;
import jme3utilities.math.MyMath;

import javax.annotation.Nullable;
import java.util.logging.Logger;

/**
 * Modified from {@link jme3utilities.math.MyVector3f}
 * Utility methods for 2-D vectors.
 */
public class MyVector2f {
    /**
     * number of axes in the coordinate system
     */
    final public static int numAxes = 2;
    /**
     * index of the X axis
     */
    final public static int xAxis = 0;
    /**
     * index of the Y axis
     */
    final public static int yAxis = 1;
    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(MyVector2f.class.getName());

    /**
     * A private constructor to inhibit instantiation of this class.
     */
    private MyVector2f() {
    }

    /**
     * Accumulate maximum coordinates.
     *
     * @param maxima the highest coordinate so far for each axis (not null,
     * modified)
     * @param input vector to compare (not null, unaffected)
     */
    public static void accumulateMaxima(Vector2f maxima, Vector2f input) {
        if (input.x > maxima.x) {
            maxima.x = input.x;
        }
        if (input.y > maxima.y) {
            maxima.y = input.y;
        }
    }

    /**
     * Accumulate minimum coordinates.
     *
     * @param minima the lowest coordinate so far for each axis (not null,
     * modified)
     * @param input vector to compare (not null, unaffected)
     */
    public static void accumulateMinima(Vector2f minima, Vector2f input) {
        if (input.x < minima.x) {
            minima.x = input.x;
        }
        if (input.y < minima.y) {
            minima.y = input.y;
        }
    }

    /**
     * Determine the dot (scalar) product of 2 vectors. Unlike
     * {@link Vector2f#dot(Vector2f)}, this method returns a
     * double-precision value for precise calculation of angles.
     *
     * @param vector1 the first input vector (not null, unaffected)
     * @param vector2 the 2nd input vector (not null, unaffected)
     * @return the dot product
     */
    public static double dot(Vector2f vector1, Vector2f vector2) {
        double x1 = vector1.x;
        double x2 = vector2.x;
        double y1 = vector1.y;
        double y2 = vector2.y;
        double product = x1 * x2 + y1 * y2;

        return product;
    }

//    /**
//     * Generate an orthonormal basis that includes the specified vector.
//     *
//     * @param in1 input direction for the first basis vector (not null, not
//     * zero, modified)
//     * @param store2 storage for the 2nd basis vector (not null, modified)
//     * @param store3 storage for the 3nd basis vector (not null, modified)
//     */
//    public static void generateBasis(Vector3f in1, Vector3f store2,
//                                     Vector3f store3) {
//        assert Validate.nonZero(in1, "starting direction");
//        assert Validate.nonNull(store2, "2nd basis vector");
//        assert Validate.nonNull(store3, "3nd basis vector");
//
//        normalizeLocal(in1);
//        /*
//         * Pick a direction that's not parallel (or anti-parallel) to
//         * the input direction.
//         */
//        float x = Math.abs(in1.x);
//        float y = Math.abs(in1.y);
//        float z = Math.abs(in1.z);
//        if (x <= y && x <= z) {
//            store3.set(1f, 0f, 0f);
//        } else if (y <= z) {
//            store3.set(0f, 1f, 0f);
//        } else {
//            store3.set(0f, 0f, 1f);
//        }
//        /*
//         * Use cross products to generate unit vectors orthogonal
//         * to the input vector.
//         */
//        in1.cross(store3, store2);
//        normalizeLocal(store2);
//        in1.cross(store2, store3);
//        normalizeLocal(store3);
//    }
//TODO: Convert to 2D?

    /**
     * Test whether all components of a vector are all non-negative: in other
     * words, whether it's in the first octant or the boundaries thereof.
     *
     * @param vector input (not null, unaffected)
     * @return true if all components are non-negative, false otherwise
     */
    public static boolean isAllNonNegative(Vector2f vector) {
        if (vector.x >= 0f && vector.y >= 0f) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Test whether all components of a vector are all positive: in other words,
     * whether it's strictly inside the first octant.
     *
     * @param vector input (not null, unaffected)
     * @return true if all components are positive, false otherwise
     */
    public static boolean isAllPositive(Vector2f vector) {
        if (vector.x > 0f && vector.y > 0f) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Test for a scale identity.
     *
     * @param vector input (not null, unaffected)
     * @return true if the vector equals
     * {@link Vector2f#UNIT_XY}, false otherwise
     */
    public static boolean isScaleIdentity(Vector2f vector) {
        if (vector.x == 1f && vector.y == 1f) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Test for a uniform scaling vector.
     *
     * @param vector input (not null, unaffected)
     * @return true if all 3 components are equal, false otherwise
     */
    public static boolean isScaleUniform(Vector2f vector) {
        if (vector.x == vector.y) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Test for a zero vector or translation identity.
     *
     * @param vector input (not null, unaffected)
     * @return true if the vector equals (0,0,0), false otherwise
     */
    public static boolean isZero(Vector2f vector) {
        if (vector.x == 0f && vector.y == 0f) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Determine the squared length of a vector. Unlike
     * {@link Vector2f#lengthSquared()}, this method returns a
     * double-precision value for precise comparison of lengths.
     *
     * @param vector input (not null, unaffected)
     * @return the squared length (&ge;0)
     */
    public static double lengthSquared(Vector2f vector) {
        double result = MyMath.sumOfSquares(vector.x, vector.y);
        return result;
    }

    /**
     * Interpolate between (or extrapolate from) 2 vectors using linear (Lerp)
     * *polation. No rounding error is introduced when v1==v2.
     *
     * @param t descaled parameter value (0&rarr;v0, 1&rarr;v1)
     * @param v0 function value at t=0 (not null, unaffected unless it's also
     * storeResult)
     * @param v1 function value at t=1 (not null, unaffected unless it's also
     * storeResult)
     * @param storeResult storage for the result (modified if not null, may be
     * v0 or v1)
     * @return an interpolated vector (either storeResult or a new instance)
     */
    public static Vector2f lerp(float t, Vector2f v0, Vector2f v1,
                                Vector2f storeResult) {
        assert Validate.nonNull(v0, "v0");
        assert Validate.nonNull(v1, "v1");
        Vector2f result = (storeResult == null) ? new Vector2f() : storeResult;

        result.x = MyMath.lerp(t, v0.x, v1.x);
        result.y = MyMath.lerp(t, v0.y, v1.y);

        return result;
    }

    /**
     * Test whether 2 vectors are distinct, without distinguishing 0 from -0.
     *
     * @param v1 the first input vector (not null, unaffected)
     * @param v2 the 2nd input vector (not null, unaffected)
     * @return true if distinct, otherwise false
     */
    public static boolean ne(Vector2f v1, Vector2f v2) {
        assert Validate.nonNull(v1, "first input vector");
        assert Validate.nonNull(v2, "2nd input vector");

        boolean result = v1.x != v2.x || v1.y != v2.y;
        return result;
    }

    /**
     * Normalize the specified vector in place.
     *
     * @param input (not null, modified)
     */
    public static void normalizeLocal(Vector2f input) {
        assert Validate.nonNull(input, "input");

        double lengthSquared = lengthSquared(input);
        double dScale = Math.sqrt(lengthSquared);
        float fScale = (float) dScale;
        if (fScale != 0f && fScale != 1f) {
            input.divideLocal(fScale);
        }
    }

    /**
     * Standardize a vector in preparation for hashing.
     *
     * @param input (not null, unaffected unless it's also storeResult)
     * @param storeResult storage for the result (modified if not null, may be
     * input)
     * @return an equivalent vector without any negative zero components (either
     * storeResult or a new instance)
     */
    public static Vector2f standardize(Vector2f input, Vector2f storeResult) {
        assert Validate.nonNull(input, "input vector");
        Vector2f result = (storeResult == null) ? new Vector2f() : storeResult;

        result.x = MyMath.standardize(input.x);
        result.y = MyMath.standardize(input.y);

        return result;
    }

    /**
     * Store <code>Vector3f</code>'s x and y value to a <code>Vector2f</code> object and return it.
     * @param result <code>Vector3f</code> object to store from.
     * @param storeResult <code>Vector2f</code> object to store to (can be null).
     * @return the stored <code>Vector2f</code> object, or a new object if storeResult is null
     */
    public static Vector2f storeToVec2(Vector3f result, @Nullable Vector2f storeResult) {
        if (storeResult == null) {
            storeResult = new Vector2f();
        }
        storeResult.x = result.x;
        storeResult.y = result.y;
        return storeResult;
    }

    public static double angle(Vector2f a, Vector2f b) {
        return Math.acos((a.x * b.x + a.y * b.y) / (a.length() * b.length()));
    }
}
