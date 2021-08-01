package com.shblock.physicscontrol.physics.util;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import net.minecraft.nbt.FloatNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.logging.Logger;

/**
 * Modified from {@link Vector3f}.
 */
public class Vector2f implements Cloneable, java.io.Serializable {
    static final long serialVersionUID = 1;
    private static final Logger logger = Logger.getLogger(Vector2f.class.getName());
    /**
     * shared instance of the all-zero vector (0,0,0) - Do not modify!
     */
    public final static Vector2f ZERO = new Vector2f(0, 0);
    /**
     * shared instance of the +X direction (1,0,0) - Do not modify!
     */
    public final static Vector2f UNIT_X = new Vector2f(1, 0);
    /**
     * shared instance of the +Y direction (0,1,0) - Do not modify!
     */
    public final static Vector2f UNIT_Y = new Vector2f(0, 1);
    /**
     * shared instance of the all-ones vector (1,1,1) - Do not modify!
     */
    public final static Vector2f UNIT_XY = new Vector2f(1, 1);
    /**
     * the x value of the vector.
     */
    public float x;
    /**
     * the y value of the vector.
     */
    public float y;

    /**
     * Constructor instantiates a new <code>Vector2f</code> with default
     * values of (0,0).
     *
     */
    public Vector2f() {
        x = y = 0;
    }

    /**
     * Constructor instantiates a new <code>Vector2f</code> with provides
     * values.
     *
     * @param x   the x value of the vector.
     * @param y   the y value of the vector.
     */
    public Vector2f(float x, float y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Convert a <code>Vector3f</code> to a new <code>Vector2f</code> (ignore Z value)
     * @param vector3f   the <code>Vector3f</code> object to convert.
     */
    public Vector2f(Vector3f vector3f) {
        this.x = vector3f.x;
        this.y = vector3f.y;
    }

    /**
     * Convert this <code>Vector2f</code> object to a new <code>Vector3f</code> object (with Z=0)
     * @return the new <code>Vector3f</code> object
     */
    public Vector3f toVec3() {
        return new Vector3f(this.x, this.y, 0f);
    }

    /**
     * <code>set</code> sets the x,y values of the vector based on passed
     * parameters.
     *
     * @param x   the x value of the vector.
     * @param y   the y value of the vector.
     * @return this vector
     */
    public Vector2f set(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    /**
     * <code>set</code> sets the x,y values of the vector by copying the
     * supplied vector.
     *
     * @param vect
     *            the vector to copy.
     * @return this vector
     */
    public Vector2f set(Vector2f vect) {
        this.x = vect.x;
        this.y = vect.y;
        return this;
    }

    /**
     * <code>add</code> adds a provided vector to this vector creating a
     * resultant vector which is returned. If the provided vector is null, null
     * is returned.
     *
     * @param vec
     *            the vector to add to this.
     * @return the resultant vector.
     */
    public Vector2f add(Vector2f vec) {
        if (null == vec) {
            logger.warning("Provided vector is null, null returned.");
            return null;
        }
        return new Vector2f(x + vec.x, y + vec.y);
    }

    /**
     * <code>addLocal</code> adds a provided vector to this vector internally,
     * and returns a handle to this vector for easy chaining of calls. If the
     * provided vector is null, null is returned.
     *
     * @param vec
     *            the vector to add to this vector.
     * @return this
     */
    public Vector2f addLocal(Vector2f vec) {
        if (null == vec) {
            logger.warning("Provided vector is null, null returned.");
            return null;
        }
        x += vec.x;
        y += vec.y;
        return this;
    }

    /**
     * <code>addLocal</code> adds the provided values to this vector
     * internally, and returns a handle to this vector for easy chaining of
     * calls.
     *
     * @param addX
     *            value to add to x
     * @param addY
     *            value to add to y
     * @return this
     */
    public Vector2f addLocal(float addX, float addY) {
        x += addX;
        y += addY;
        return this;
    }

    /**
     * <code>dot</code> calculates the dot product of this vector with a
     * provided vector. If the provided vector is null, 0 is returned.
     *
     * @param vec
     *            the vector to dot with this vector.
     * @return the resultant dot product of this vector and a given vector.
     */
    public float dot(Vector2f vec) {
        if (null == vec) {
            logger.warning("Provided vector is null, 0 returned.");
            return 0;
        }
        return x * vec.x + y * vec.y;
    }

    /**
     * Returns true if this vector is a unit vector (length() ~= 1),
     * returns false otherwise.
     *
     * @return true if this vector is a unit vector (length() ~= 1),
     * or false otherwise.
     */
    public boolean isUnitVector() {
        float len = length();
        return 0.99f < len && len < 1.01f;
    }

    /**
     * <code>length</code> calculates the magnitude of this vector.
     *
     * @return the length or magnitude of the vector.
     */
    public float length() {
        /*
         * Use double-precision arithmetic to reduce the chance of overflow
         * (when lengthSquared > Float.MAX_VALUE) or underflow (when
         * lengthSquared is < Float.MIN_VALUE).
         */
        double xx = x;
        double yy = y;
        double lengthSquared = xx * xx + yy * yy;
        float result = (float) Math.sqrt(lengthSquared);

        return result;
    }

    /**
     * <code>lengthSquared</code> calculates the squared value of the
     * magnitude of the vector.
     *
     * @return the magnitude squared of the vector.
     */
    public float lengthSquared() {
        return x * x + y * y;
    }

    /**
     * <code>multLocal</code> multiplies this vector by a scalar internally,
     * and returns a handle to this vector for easy chaining of calls.
     *
     * @param scalar
     *            the value to multiply this vector by.
     * @return this
     */
    public Vector2f multLocal(float scalar) {
        x *= scalar;
        y *= scalar;
        return this;
    }

    /**
     * <code>multLocal</code> multiplies a provided vector to this vector
     * internally, and returns a handle to this vector for easy chaining of
     * calls. If the provided vector is null, null is returned.
     *
     * @param vec
     *            the vector to mult to this vector.
     * @return this
     */
    public Vector2f multLocal(Vector2f vec) {
        if (null == vec) {
            logger.warning("Provided vector is null, null returned.");
            return null;
        }
        x *= vec.x;
        y *= vec.y;
        return this;
    }

    /**
     * <code>multLocal</code> multiplies a provided vector to this vector
     * internally, and returns a handle to this vector for easy chaining of
     * calls. If the provided vector is null, null is returned.
     *
     * @param vec
     *            the vector to mult to this vector.
     * @param store result vector (null to create a new vector)
     * @return this
     */
    public Vector2f mult(Vector2f vec, Vector2f store) {
        if (null == vec) {
            logger.warning("Provided vector is null, null returned.");
            return null;
        }
        if (store == null) {
            store = new Vector2f();
        }
        return store.set(x * vec.x, y * vec.y);
    }

    /**
     * <code>divideLocal</code> divides this vector by a scalar internally,
     * and returns a handle to this vector for easy chaining of calls. Dividing
     * by zero will result in an exception.
     *
     * @param scalar
     *            the value to divides this vector by.
     * @return this
     */
    public Vector2f divideLocal(float scalar) {
        scalar = 1f / scalar;
        x *= scalar;
        y *= scalar;
        return this;
    }


    /**
     * <code>divideLocal</code> divides this vector by a scalar internally,
     * and returns a handle to this vector for easy chaining of calls. Dividing
     * by zero will result in an exception.
     *
     * @param scalar
     *            the value to divides this vector by.
     * @return this
     */
    public Vector2f divideLocal(Vector2f scalar) {
        x /= scalar.x;
        y /= scalar.y;
        return this;
    }

    /**
     * <code>subtract</code> subtracts the values of a given vector from those
     * of this vector creating a new vector object. If the provided vector is
     * null, null is returned.
     *
     * @param vec
     *            the vector to subtract from this vector.
     * @return the result vector.
     */
    public Vector2f subtract(Vector2f vec) {
        return new Vector2f(x - vec.x, y - vec.y);
    }

    /**
     * <code>subtract</code>
     *
     * @param vec
     *            the vector to subtract from this
     * @param result
     *            the vector to store the result in
     * @return result
     */
    public Vector2f subtract(Vector2f vec, Vector2f result) {
        if (result == null) {
            result = new Vector2f();
        }
        result.x = x - vec.x;
        result.y = y - vec.y;
        return result;
    }

    /**
     * <code>subtractLocal</code> subtracts the provided values from this vector
     * internally, and returns a handle to this vector for easy chaining of
     * calls.
     *
     * @param subtractX
     *            the x value to subtract.
     * @param subtractY
     *            the y value to subtract.
     * @return this
     */
    public Vector2f subtractLocal(float subtractX, float subtractY) {
        x -= subtractX;
        y -= subtractY;
        return this;
    }

    /**
     * <code>normalize</code> returns the unit vector of this vector.
     *
     * @return unit vector of this vector.
     */
    public Vector2f normalize() {
//        float length = length();
//        if (length != 0) {
//            return divide(length);
//        }
//
//        return divide(1);
        float length = x * x + y * y;
        if (length != 1f && length != 0f) {
            length = 1.0f / FastMath.sqrt(length);
            return new Vector2f(x * length, y * length);
        }
        return clone();
    }

    /**
     * <code>zero</code> resets this vector's data to zero internally.
     *
     * @return this
     */
    public Vector2f zero() {
        x = y = 0;
        return this;
    }

    /**
     * Check a vector... if it is null or its floats are NaN or infinite,
     * return false.  Else return true.
     *
     * @param vector the vector to check
     * @return true or false as stated above.
     */
    public static boolean isValidVector(Vector2f vector) {
        if (vector == null) {
            return false;
        }
        if (Float.isNaN(vector.x)
                || Float.isNaN(vector.y)) {
            return false;
        }
        if (Float.isInfinite(vector.x)
                || Float.isInfinite(vector.y)) {
            return false;
        }
        return true;
    }

    /**
     * Create a copy of this vector.
     *
     * @return a new instance, equivalent to this one
     */
    @Override
    public Vector2f clone() {
        try {
            return (Vector2f) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(); // can not happen
        }
    }

    /**
     * are these two vectors the same? they are is they both have the same x and y values.
     *
     * @param o   the object to compare for equality
     * @return true if they are equal
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Vector2f)) {
            return false;
        }

        if (this == o) {
            return true;
        }

        Vector2f comp = (Vector2f) o;
        if (Float.compare(x, comp.x) != 0) {
            return false;
        }
        if (Float.compare(y, comp.y) != 0) {
            return false;
        }
        return true;
    }

    /**
     * <code>hashCode</code> returns a unique code for this vector object based
     * on its values. If two vectors are logically equivalent, they will return
     * the same hash code value.
     *
     * @return the hash code value of this vector.
     */
    @Override
    public int hashCode() {
        int hash = 37;
        hash += 37 * hash + Float.floatToIntBits(x);
        hash += 37 * hash + Float.floatToIntBits(y);
        return hash;
    }

    /**
     * <code>toString</code> returns a string representation of this vector.
     * The format is:
     *
     * (XX.XXXX, YY.YYYY)
     *
     * @return the string representation of this vector.
     */
    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }

    /**
     * Determine the X component of this vector.
     *
     * @return x
     */
    public float getX() {
        return x;
    }

    /**
     * @param index 0 or 1
     * @return x value if index == 0 or y value if index == 1
     * @throws IllegalArgumentException
     *             if index is not one of 0, 1.
     */
    public float get(int index) {
        switch (index) {
            case 0:
                return x;
            case 1:
                return y;
        }
        throw new IllegalArgumentException("index must be either 0 or 1");
    }

    /**
     * @param index
     *            which field index in this vector to set.
     * @param value
     *            to set to one of x or y.
     * @throws IllegalArgumentException
     *             if index is not one of 0, 1.
     */
    public void set(int index, float value) {
        switch (index) {
            case 0:
                x = value;
                return;
            case 1:
                y = value;
                return;
        }
        throw new IllegalArgumentException("index must be either 0 or 1");
    }
}
