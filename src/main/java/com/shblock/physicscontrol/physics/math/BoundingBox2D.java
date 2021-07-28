package com.shblock.physicscontrol.physics.math;

import com.jme3.bounding.BoundingBox;
import com.jme3.math.FastMath;

public class BoundingBox2D {
    final private Vector2f center = new Vector2f();
    /**
     * the X-extent of the box (>=0, may be +Infinity)
     */
    float xExtent;
    /**
     * the Y-extent of the box (>=0, may be +Infinity)
     */
    float yExtent;

    /**
     * Instantiate a <code>BoundingBox</code> without initializing it.
     */
    public BoundingBox2D() {
    }

    /**
     * Instantiate a <code>BoundingBox</code> with given center and extents.
     *
     * @param c the coordinates of the center of the box (not null, not altered)
     * @param x the X-extent of the box (0 or greater, may be +Infinity)
     * @param y the Y-extent of the box (0 or greater, may be +Infinity)
     */
    public BoundingBox2D(Vector2f c, float x, float y) {
        this.center.set(c);
        this.xExtent = x;
        this.yExtent = y;
    }

    public BoundingBox2D(Vector2f min, Vector2f max) {
        setMinMax(min, max);
    }

    public BoundingBox2D(BoundingBox boundingBox) {
        this(new Vector2f(boundingBox.getMin(null)), new Vector2f(boundingBox.getMax(null)));
    }

    /**
     * Convert this object to a {@link BoundingBox} object (with z=0)
     * @return the {@link BoundingBox} object
     */
    public BoundingBox toBox3D() {
        return new BoundingBox(getMin(null).toVec3(), getMax(null).toVec3());
    }

    /**
     * Query extent.
     *
     * @param store
     *            where extent gets stored - null to return a new vector
     * @return store / new vector
     */
    public Vector2f getExtent(Vector2f store) {
        if (store == null) {
            store = new Vector2f();
        }
        store.set(xExtent, yExtent);
        return store;
    }

    public float getXExtent() {
        return xExtent;
    }

    public float getYExtent() {
        return yExtent;
    }

    public void setXExtent(float xExtent) {
        if (xExtent < 0) {
            throw new IllegalArgumentException();
        }

        this.xExtent = xExtent;
    }

    public void setYExtent(float yExtent) {
        if (yExtent < 0) {
            throw new IllegalArgumentException();
        }

        this.yExtent = yExtent;
    }

    public Vector2f getMin(Vector2f store) {
        if (store == null) {
            store = new Vector2f();
        }
        store.set(center).subtractLocal(xExtent, yExtent);
        return store;
    }

    public Vector2f getMax(Vector2f store) {
        if (store == null) {
            store = new Vector2f();
        }
        store.set(center).addLocal(xExtent, yExtent);
        return store;
    }

    public void setMinMax(Vector2f min, Vector2f max) {
        this.center.set(max).addLocal(min).multLocal(0.5f);
        xExtent = FastMath.abs(max.x - center.x);
        yExtent = FastMath.abs(max.y - center.y);
    }
}
