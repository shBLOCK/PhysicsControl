package com.shblock.physicscontrol.physics.util;

import com.shblock.physicscontrol.physics.physics.BodyUserObj;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.FloatNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.collision.shapes.ShapeType;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;

import java.util.Arrays;

public class NBTSerializer {
    public static CompoundNBT toNBT(Vec2 vec) {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putFloat("x", vec.x);
        nbt.putFloat("y", vec.y);
        return nbt;
    }

    public static Vec2 vec2FromNBT(CompoundNBT nbt) {
        return new Vec2(nbt.getFloat("x"), nbt.getFloat("y"));
    }

    public static INBT toNBT(Vec2[] list) {
        ListNBT nbt = new ListNBT();
        for (Vec2 vec : list) {
            nbt.add(toNBT(vec));
        }
        return nbt;
    }

    public static Vec2[] vec2listFromNBT(INBT nbt) {
        ListNBT ln = (ListNBT) nbt;
        Vec2[] list = new Vec2[ln.size()];
        for (int i=0; i<ln.size(); i++) {
            list[i] = vec2FromNBT(ln.getCompound(i));
        }
        return list;
    }

    public static INBT toNBT(float[] array) {
        ListNBT nbt = new ListNBT();
        for (float f : array) {
            nbt.add(FloatNBT.valueOf(f));
        }
        return nbt;
    }

    public static float[] floatArrayFromNBT(INBT nbt) {
        ListNBT list = (ListNBT) nbt;
        float[] array = new float[list.size()];
        for (int i=0; i<list.size(); i++) {
            array[i] = list.getFloat(i);
        }
        return array;
    }

    public static CompoundNBT toNBT(Filter filter) {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putInt("category_bits", filter.categoryBits);
        nbt.putInt("mask_bits", filter.maskBits);
        nbt.putInt("group_index", filter.groupIndex);
        return nbt;
    }

    public static Filter filterFromNBT(CompoundNBT nbt) {
        Filter filter = new Filter();
        filter.categoryBits = nbt.getInt("category_bits");
        filter.maskBits = nbt.getInt("mask_bits");
        filter.groupIndex = nbt.getInt("group_index");
        return filter;
    }

    //TODO: joint

    public static CompoundNBT toNBT(Shape shape) {
        CompoundNBT nbt = new CompoundNBT();

        nbt.putFloat("radius", shape.getRadius());
        nbt.putInt("type", shape.m_type.ordinal());
        switch (shape.m_type) {
            case CIRCLE:
                CircleShape circle = (CircleShape) shape;
                nbt.put("center", toNBT(circle.m_p));
                return nbt;
            case POLYGON:
                PolygonShape poly = (PolygonShape) shape;
                nbt.put("center", toNBT(poly.m_centroid));
                nbt.putInt("count", poly.m_count);
                nbt.put("vertices", toNBT(Arrays.stream(poly.m_vertices).limit(poly.m_count).toArray(Vec2[]::new)));
                nbt.put("normals", toNBT(Arrays.stream(poly.m_normals).limit(poly.m_count).toArray(Vec2[]::new)));
                return nbt;
            default:
                assert false : shape;
        }
        return null;
    }

    public static Shape shapeFromNBT(CompoundNBT nbt) {
        switch (ShapeType.values()[nbt.getInt("type")]) {
            case CIRCLE:
                CircleShape circle = new CircleShape();
                circle.m_p.set(vec2FromNBT(nbt.getCompound("center")));
                circle.m_radius = nbt.getFloat("radius");
                return circle;
            case POLYGON:
                PolygonShape poly = new PolygonShape();
                poly.m_centroid.set(vec2FromNBT(nbt.getCompound("center")));
                poly.m_count = nbt.getInt("count");
                Vec2[] vertices = vec2listFromNBT(nbt.get("vertices"));
                Vec2[] normals = vec2listFromNBT(nbt.get("normals"));
                for (int i=0; i<nbt.getInt("count"); i++) {
                    poly.m_vertices[i] = vertices[i];
                    poly.m_normals[i] = normals[i];
                }
                return poly;
            default:
                assert false : ShapeType.values()[nbt.getInt("type")];
        }
        return null;
    }

    public static CompoundNBT toNBT(FixtureDef fixture) {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putFloat("density", fixture.density);
        nbt.putFloat("friction", fixture.friction);
        nbt.putFloat("restitution", fixture.restitution);
        nbt.putBoolean("sensor", fixture.isSensor);
        nbt.put("shape", toNBT(fixture.shape));
        nbt.put("filter", toNBT(fixture.filter));
        return nbt;
    }

    public static CompoundNBT toNBT(Fixture fixture) {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putFloat("density", fixture.m_density);
        nbt.putFloat("friction", fixture.m_friction);
        nbt.putFloat("restitution", fixture.m_restitution);
        nbt.putBoolean("sensor", fixture.m_isSensor);
        nbt.put("shape", toNBT(fixture.m_shape));
        nbt.put("filter", toNBT(fixture.m_filter));
        return nbt;
    }

    public static FixtureDef fixtureFromNBT(CompoundNBT nbt) {
        FixtureDef fixture = new FixtureDef();
        fixture.density = nbt.getFloat("density");
        fixture.friction = nbt.getFloat("friction");
        fixture.restitution = nbt.getFloat("restitution");
        fixture.isSensor = nbt.getBoolean("sensor");
        fixture.shape = shapeFromNBT(nbt.getCompound("shape"));
        fixture.filter = filterFromNBT(nbt.getCompound("filter"));
        return fixture;
    }

    public static CompoundNBT toNBT(BodyDef body) {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putInt("type", body.getType().ordinal());
        nbt.put("position", toNBT(body.getPosition()));
        nbt.putFloat("angle", body.getAngle());
        nbt.put("linear_vel", toNBT(body.getLinearVelocity()));
        nbt.putFloat("angular_vel", body.getAngularVelocity());
        nbt.putFloat("linear_damping", body.getLinearDamping());
        nbt.putFloat("angular_damping", body.getAngularDamping());
        nbt.putFloat("gravity_scale", body.getGravityScale());
        nbt.putBoolean("bullet", body.isBullet());
        nbt.putBoolean("allow_sleep", body.isAllowSleep());
        nbt.putBoolean("awake", body.isAwake());
        nbt.putBoolean("active", body.isActive());
        nbt.putBoolean("fixed_rotation", body.isFixedRotation());

        if (body.getUserData() instanceof BodyUserObj) {
            nbt.put("user_obj", toNBT((BodyUserObj) body.getUserData()));
        }
        return nbt;
    }

    public static CompoundNBT toNBT(Body body) {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putInt("type", body.getType().ordinal());
        nbt.put("position", toNBT(body.getPosition()));
        nbt.putFloat("angle", body.getAngle());
        nbt.put("linear_vel", toNBT(body.getLinearVelocity()));
        nbt.putFloat("angular_vel", body.getAngularVelocity());
        nbt.putFloat("linear_damping", body.getLinearDamping());
        nbt.putFloat("angular_damping", body.getAngularDamping());
        nbt.putFloat("gravity_scale", body.getGravityScale());
        nbt.putBoolean("bullet", body.isBullet());
        nbt.putBoolean("allow_sleep", body.isSleepingAllowed());
        nbt.putBoolean("awake", body.isAwake());
        nbt.putBoolean("active", body.isActive());
        nbt.putBoolean("fixed_rotation", body.isFixedRotation());

        ListNBT list = new ListNBT();
        Fixture fixture = body.getFixtureList();
        while (fixture != null) {
            list.add(toNBT(fixture));
            fixture = fixture.m_next;
        }
        nbt.put("fixture_list", list);

        if (body.getUserData() instanceof BodyUserObj) {
            nbt.put("user_obj", toNBT((BodyUserObj) body.getUserData()));
        }
        return nbt;
    }

    public static BodyDef bodyFromNBT(CompoundNBT nbt) {
        BodyDef body = new BodyDef();
        body.type = BodyType.values()[nbt.getInt("type")];
        body.position = vec2FromNBT(nbt.getCompound("position"));
        body.angle = nbt.getFloat("angle");
        body.linearVelocity = vec2FromNBT(nbt.getCompound("linear_vel"));
        body.angularVelocity = nbt.getFloat("angular_vel");
        body.linearDamping = nbt.getFloat("linear_dumping");
        body.angularDamping = nbt.getFloat("angular_dumping");
        body.gravityScale = nbt.getFloat("gravity_scale");
        body.bullet = nbt.getBoolean("bullet");
        body.allowSleep = nbt.getBoolean("allow_sleep");
        body.awake = nbt.getBoolean("awake");
        body.active = nbt.getBoolean("active");
        body.fixedRotation = nbt.getBoolean("fixed_rotation");

        if (nbt.contains("user_obj")) {
            body.userData = bodyUserObjFromNBT(nbt.getCompound("user_obj"));
        }
        return body;
    }

    public static void applyFixture(Body body, CompoundNBT nbt) {
        ListNBT list = nbt.getList("fixture_list", Constants.NBT.TAG_COMPOUND);
        for (int i=0; i<list.size(); i++) {
            body.createFixture(fixtureFromNBT(list.getCompound(i)));
        }
    }

    public static CompoundNBT bodyListToNBT(Body body) {
        CompoundNBT nbt = new CompoundNBT();
        ListNBT list = new ListNBT();
        int i = 0;
        while (body != null) {
            if (body.getUserData() instanceof BodyUserObj) {
                list.add(toNBT(body));
                i++;
            }
            body = body.m_next;
        }
        nbt.put("list", list);
        nbt.putInt("count", i);
        return nbt;
    }

    public static BodyDef[] bodyListFromNBT(CompoundNBT nbt) {
        ListNBT ln = nbt.getList("list", Constants.NBT.TAG_COMPOUND);
        BodyDef[] list = new BodyDef[nbt.getInt("count")];
        for (int i=0; i<nbt.getInt("count"); i++) {
            list[i] = bodyFromNBT(ln.getCompound(i));
        }
        return list;
    }

    public static CompoundNBT toNBT(World space) {
        CompoundNBT nbt = new CompoundNBT();
        nbt.put("gravity", toNBT(space.getGravity()));
        nbt.putBoolean("auto_clear_forces", space.getAutoClearForces());
        nbt.putBoolean("allow_sleep", space.isAllowSleep());
        nbt.putBoolean("continuous_physics", space.isContinuousPhysics());
        nbt.putBoolean("warm_starting", space.isWarmStarting());
        nbt.putBoolean("sub_stepping", space.isSubStepping());

        nbt.put("body_list", bodyListToNBT(space.getBodyList()));
        //TODO: joint
        return nbt;
    }

    public static World spaceFromNBT(CompoundNBT nbt) {
        World space = new World(vec2FromNBT(nbt.getCompound("gravity")));

        space.setAutoClearForces(nbt.getBoolean("auto_clear_forces"));
        space.setAllowSleep(nbt.getBoolean("allow_sleep"));
        space.setContinuousPhysics(nbt.getBoolean("continuous_physics"));
        space.setWarmStarting(nbt.getBoolean("warm_starting"));
        space.setSubStepping(nbt.getBoolean("sub_stepping"));

        ListNBT list = nbt.getCompound("body_list").getList("list", Constants.NBT.TAG_COMPOUND);
        BodyDef[] defs = bodyListFromNBT(nbt.getCompound("body_list"));
        for (int i=0; i<defs.length; i++) {
            Body result = space.createBody(defs[i]);
            applyFixture(result, list.getCompound(i));
        }
        //TODO: joint
        return space;
    }

    public static CompoundNBT toNBT(BodyUserObj obj) {
        return obj.serializeNBT();
    }

    public static BodyUserObj bodyUserObjFromNBT(CompoundNBT nbt) {
        BodyUserObj obj = new BodyUserObj();
        obj.deserializeNBT(nbt);
        return obj;
    }
}
