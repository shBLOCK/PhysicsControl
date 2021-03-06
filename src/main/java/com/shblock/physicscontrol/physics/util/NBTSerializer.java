package com.shblock.physicscontrol.physics.util;

import com.shblock.physicscontrol.client.InteractivePhysicsSimulator2D;
import com.shblock.physicscontrol.physics.user_obj.BodyUserObj;
import com.shblock.physicscontrol.physics.user_obj.ElasticGroupUserObj;
import com.shblock.physicscontrol.physics.user_obj.ElasticParticleUserObj;
import com.shblock.physicscontrol.physics.user_obj.UserObjBase;
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
import org.jbox2d.dynamics.joints.*;
import org.jbox2d.particle.*;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
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

    public static CompoundNBT toNBT(RevoluteJointDef def) {
        CompoundNBT nbt = new CompoundNBT();

        nbt.putInt("bodyA", ((UserObjBase) def.bodyA.getUserData()).getId());
        if (def.bodyB != null) {
            nbt.putInt("bodyB", ((UserObjBase) def.bodyB.getUserData()).getId());
        }
        nbt.put("anchorA", toNBT(def.localAnchorA));
        nbt.put("anchorB", toNBT(def.localAnchorB));
        nbt.putFloat("size", (Float) def.userData);

        return nbt;
    }

    public static RevoluteJointDef bearingDefFromNBT(CompoundNBT nbt) {
        RevoluteJointDef def = new RevoluteJointDef();

        World world = InteractivePhysicsSimulator2D.getInstance().getSpace();
        def.bodyA = findBodyById(world, nbt.getInt("bodyA"));
        if (nbt.contains("bodyB")) {
            def.bodyB = findBodyById(world, nbt.getInt("bodyB"));
        }
        def.localAnchorA = vec2FromNBT(nbt.getCompound("anchorA"));
        def.localAnchorB = vec2FromNBT(nbt.getCompound("anchorB"));
        def.userData = nbt.getFloat("size");

        return def;
    }

    public static CompoundNBT toNBT(Joint joint) {
        CompoundNBT nbt = new CompoundNBT();

        nbt.putInt("type", joint.getType().ordinal());
        nbt.putBoolean("collide_connected", joint.getCollideConnected());
        nbt.putInt("bodyA", ((UserObjBase) joint.getBodyA().getUserData()).getId());
        nbt.putInt("bodyB", ((UserObjBase) joint.getBodyB().getUserData()).getId());
//        Vec2 anc = new Vec2();
//        joint.getAnchorA(anc);
//        nbt.put("ancA", toNBT(anc));
//        anc = new Vec2();
//        joint.getAnchorB(anc);
//        nbt.put("ancA", toNBT(anc));

        switch (joint.getType()) {
            case REVOLUTE:
                RevoluteJoint rev = (RevoluteJoint) joint;
                nbt.put("local_anchor_A", toNBT(rev.getLocalAnchorA()));
                nbt.put("local_anchor_B", toNBT(rev.getLocalAnchorB()));
                nbt.putFloat("size", (Float) rev.getUserData());
                break;
            default:
                assert false;
        }

        return nbt;
    }

    private static Body findBodyById(World world, int id) {
        Body body = world.getBodyList();
        while (body != null) {
            UserObjBase obj = (UserObjBase) body.getUserData();
            if (obj.getId() == id) {
                return body;
            }
            body = body.m_next;
        }
        return null;
    }

    public static Joint jointFromNBT(CompoundNBT nbt, World world) {
        JointDef def = new JointDef(JointType.values()[nbt.getInt("type")]);
        switch (JointType.values()[nbt.getInt("type")]) {
            case REVOLUTE:
                def = new RevoluteJointDef();
                break;
            default:
                assert false;
        }

//        JointDef def = new JointDef(JointType.values()[nbt.getInt("type")]);
        def.collideConnected = nbt.getBoolean("collide_connected");
        def.bodyA = findBodyById(world, nbt.getInt("bodyA"));
        def.bodyB = findBodyById(world, nbt.getInt("bodyB"));

        Joint joint = world.createJoint(def);

        switch (def.type) {
            case REVOLUTE:
                RevoluteJoint rev = (RevoluteJoint) joint;
                rev.getLocalAnchorA().set(vec2FromNBT(nbt.getCompound("local_anchor_A")));
                rev.getLocalAnchorB().set(vec2FromNBT(nbt.getCompound("local_anchor_B")));
                rev.setUserData(nbt.getFloat("size"));
                break;
            default:
                assert false;
        }

        return joint;
    }

    public static CompoundNBT serializeAllJoint(World world) {
        CompoundNBT nbt = new CompoundNBT();
        ListNBT list = new ListNBT();

        Joint joint = world.getJointList();
        while (joint != null) {
            list.add(toNBT(joint));
            joint = joint.getNext();
        }

        nbt.put("list", list);
        return nbt;
    }

    public static void deserializeAllJoint(CompoundNBT nbt, World world) {
        ListNBT list = nbt.getList("list", Constants.NBT.TAG_COMPOUND);
        for (int i=0; i<list.size();i++) {
            CompoundNBT compound = list.getCompound(i);
            jointFromNBT(compound, world);
        }
    }

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

    public static CompoundNBT toNBT(ParticleColor particleColor) {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putByte("r", particleColor.r);
        nbt.putByte("g", particleColor.g);
        nbt.putByte("b", particleColor.b);
        nbt.putByte("a", particleColor.a);
        return nbt;
    }

    public static ParticleColor particleColorFromNBT(CompoundNBT nbt) {
        return new ParticleColor(
                nbt.getByte("r"),
                nbt.getByte("g"),
                nbt.getByte("b"),
                nbt.getByte("a")
        );
    }

    public static CompoundNBT toNBT(ParticleDef particleDef) {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putInt("flags", particleDef.flags);
        nbt.put("pos", toNBT(particleDef.position));
        nbt.put("vel", toNBT(particleDef.velocity));
        nbt.put("color", toNBT(particleDef.color));
        if (particleDef.userData != null) {
            CompoundNBT userObj = null;
            String type = null;
            if (particleDef.userData instanceof ElasticParticleUserObj) {
                userObj = ((ElasticParticleUserObj) particleDef.userData).serializeNBT();
                type = "elastic";
            }
            if (userObj != null) {
                userObj.putString("type", type);
                nbt.put("user_obj", userObj);
            }
        }
        return nbt;
    }

    public static ParticleDef particleDefFromNBT(CompoundNBT nbt) {
        ParticleDef def = new ParticleDef();
        def.flags = nbt.getInt("flags");
        def.position.set(vec2FromNBT(nbt.getCompound("pos")));
        def.velocity.set(vec2FromNBT(nbt.getCompound("vel")));
        def.color = particleColorFromNBT(nbt.getCompound("color"));
        if (nbt.contains("user_obj")) {
            CompoundNBT userObj = nbt.getCompound("user_obj");
            switch (userObj.getString("type")) {
                case "elastic":
                    ElasticParticleUserObj obj = new ElasticParticleUserObj();
                    obj.deserializeNBT(userObj);
                    def.userData = obj;
                    break;
            }
        }
        return def;
    }

    public static CompoundNBT toNBT(ParticleSystem.Pair pair, int indexOffset) {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putInt("a", pair.indexA - indexOffset);
        nbt.putInt("b", pair.indexB - indexOffset);
        nbt.putFloat("distance", pair.distance);
        nbt.putFloat("strength", pair.strength);
        nbt.putInt("flags", pair.flags);
        return nbt;
    }

    public static ParticleSystem.Pair pairFromNBT(CompoundNBT nbt) {
        ParticleSystem.Pair pair = new ParticleSystem.Pair();
        pair.indexA = nbt.getInt("a");
        pair.indexB = nbt.getInt("b");
        pair.distance = nbt.getFloat("distance");
        pair.strength = nbt.getFloat("strength");
        pair.flags = nbt.getInt("flags");
        return pair;
    }

    public static CompoundNBT toNBT(World world, ParticleGroup group) {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putInt("group_flags", group.getGroupFlags());
        nbt.put("pos", toNBT(group.getPosition()));
        nbt.putFloat("angle", group.getAngle());
//        nbt.put("linear_vel", toNBT(group.getLinearVelocity()));
//        nbt.putFloat("angular_vel", group.getAngularVelocity());

        float strength = 0F;
        try {
            Field field = group.getClass().getDeclaredField("m_strength");
            field.setAccessible(true);
            strength = field.getFloat(group);
            field.setAccessible(false);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
            assert false;
        }
        nbt.putFloat("strength", strength);

        boolean destroyAutomatically = true;
        try {
            Field field = group.getClass().getDeclaredField("m_destroyAutomatically");
            field.setAccessible(true);
            destroyAutomatically = field.getBoolean(group);
            field.setAccessible(false);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
            assert false;
        }
        nbt.putBoolean("destroy_automatically", destroyAutomatically);

        CompoundNBT objNBT = null;
        String type = null;
        if (group.getUserData() instanceof ElasticGroupUserObj) {
            ElasticGroupUserObj elastic = (ElasticGroupUserObj) group.getUserData();
            objNBT = elastic.serializeNBT();
            type = "elastic";
        }
        if (objNBT != null) {
            objNBT.putString("type", type);
            nbt.put("user_obj", objNBT);
        }

        nbt.put("particles", saveParticlesInGroup(world, group));

        return nbt;
    }

    public static ParticleGroupDef particleGroupDefFromNBT(CompoundNBT nbt) {
        ParticleGroupDef def = new ParticleGroupDef();
        def.groupFlags = nbt.getInt("group_flags");
        def.position.set(vec2FromNBT(nbt.getCompound("pos")));
        def.angle = nbt.getFloat("angle");
//        def.linearVelocity.set(vec2FromNBT(nbt.getCompound("linear_vel")));
//        def.angularVelocity = nbt.getFloat("angular_vel");
        def.strength = nbt.getFloat("strength");
        def.destroyAutomatically = nbt.getBoolean("destroy_automatically");

        if (nbt.contains("user_obj")) {
            CompoundNBT userNBT = nbt.getCompound("user_obj");
            switch (userNBT.getString("type")) {
                case "elastic":
                    ElasticGroupUserObj elastic = new ElasticGroupUserObj();
                    elastic.deserializeNBT(userNBT);
                    def.userData = elastic;
                    break;
            }
        }

        return def;
    }

    public static void loadParticleGroup(World world, CompoundNBT nbt) {
        ParticleDef[] defs = getParticleDefList(nbt.getCompound("particles"));
        ParticleGroupDef groupDef = particleGroupDefFromNBT(nbt);
        ParticleSystem.Pair[] pairs = new ParticleSystem.Pair[0];
        if (nbt.getCompound("particles").contains("pairs")) {
            ListNBT pairsNBT = nbt.getCompound("particles").getList("pairs", Constants.NBT.TAG_COMPOUND);
            pairs = new ParticleSystem.Pair[pairsNBT.size()];
            for (int i=0; i<pairsNBT.size(); i++) {
                pairs[i] = pairFromNBT(pairsNBT.getCompound(i));
            }
        }
        world.createParticleGroupForDeserialize(groupDef, defs, pairs);
    }

    public static CompoundNBT saveParticlesInGroup(World world, @Nullable ParticleGroup group) {
        CompoundNBT nbt = new CompoundNBT();

        int start = 0, end = world.getParticleCount();
        if (group != null) {
            start = group.getBufferIndex();
            end = start + group.getParticleCount();
        }

        ListNBT list = new ListNBT();
        ParticleDef tmp = new ParticleDef();
        for (int i=start; i<end; i++) {
            if (world.getParticleGroupBuffer()[i] == group) {
                tmp.flags = world.getParticleFlagsBuffer()[i];
                tmp.position.set(world.getParticlePositionBuffer()[i]);
                tmp.velocity.set(world.getParticleVelocityBuffer()[i]);
                tmp.color = world.getParticleColorBuffer()[i];
                tmp.userData = world.getParticleUserDataBuffer()[i];
                list.add(toNBT(tmp));
            }
        }
        nbt.put("list", list);

        ParticleSystem system = world.getParticleSystem();
        if (system.getPairBuffer() != null) {
            if (group != null) {
                ListNBT pairs = new ListNBT();
                for (ParticleSystem.Pair pair : system.getPairBuffer()) {
                    if (MathUtil.isInRange(pair.indexA, start, end) && MathUtil.isInRange(pair.indexB, start, end)) {
                        pairs.add(toNBT(pair, start));
                    }
                }
                nbt.put("pairs", pairs);
            }
        }

        return nbt;
    }

    public static ParticleDef[] getParticleDefList(CompoundNBT nbt) {
        ListNBT list = nbt.getList("list", Constants.NBT.TAG_COMPOUND);
        ParticleDef[] array = new ParticleDef[list.size()];
        for (int i=0; i<list.size(); i++) {
            array[i] = particleDefFromNBT(list.getCompound(i));
        }
        return array;
    }

    public static void createAllParticles(World world, ParticleDef[] defs) {
        for (ParticleDef def : defs) {
            world.createParticle(def);
        }
    }

    public static CompoundNBT toNBT(World space) {
        CompoundNBT nbt = new CompoundNBT();
        nbt.put("gravity", toNBT(space.getGravity()));
        nbt.putBoolean("auto_clear_forces", space.getAutoClearForces());
        nbt.putBoolean("allow_sleep", space.isAllowSleep());
        nbt.putBoolean("continuous_physics", space.isContinuousPhysics());
        nbt.putBoolean("warm_starting", space.isWarmStarting());
        nbt.putBoolean("sub_stepping", space.isSubStepping());
        nbt.putFloat("particle_radius", space.getParticleRadius());
        nbt.putFloat("particle_density", space.getParticleDensity());
        nbt.putFloat("particle_damping", space.getParticleDamping());
        nbt.putFloat("particle_gravity_scale", space.getParticleGravityScale());

        nbt.put("body_list", bodyListToNBT(space.getBodyList()));

        nbt.put("joints", serializeAllJoint(space));

        nbt.put("particles", saveParticlesInGroup(space, null));

        ListNBT groupsNBT = new ListNBT();
        if (space.getParticleGroupList() != null) {
            ParticleGroup group = space.getParticleGroupList();
            while (group != null) {
                groupsNBT.add(toNBT(space, group));
                group = group.getNext();
            }
        }
        nbt.put("groups", groupsNBT);

        return nbt;
    }

    public static World spaceFromNBT(CompoundNBT nbt) {
        World space = new World(vec2FromNBT(nbt.getCompound("gravity")));

        space.setAutoClearForces(nbt.getBoolean("auto_clear_forces"));
        space.setAllowSleep(nbt.getBoolean("allow_sleep"));
        space.setContinuousPhysics(nbt.getBoolean("continuous_physics"));
        space.setWarmStarting(nbt.getBoolean("warm_starting"));
        space.setSubStepping(nbt.getBoolean("sub_stepping"));
        space.setParticleRadius(nbt.getFloat("particle_radius"));
        space.setParticleDensity(nbt.getFloat("particle_density"));
        space.setParticleDamping(nbt.getFloat("particle_damping"));
        space.setParticleGravityScale(nbt.getFloat("particle_gravity_scale"));

        ListNBT list = nbt.getCompound("body_list").getList("list", Constants.NBT.TAG_COMPOUND);
        BodyDef[] defs = bodyListFromNBT(nbt.getCompound("body_list"));
        for (int i=0; i<defs.length; i++) {
            Body result = space.createBody(defs[i]);
            applyFixture(result, list.getCompound(i));
        }

        deserializeAllJoint(nbt.getCompound("joints"), space);

        createAllParticles(space, getParticleDefList(nbt.getCompound("particles")));

        ListNBT groupsNBT = nbt.getList("groups", Constants.NBT.TAG_COMPOUND);
        for (int i=0; i<groupsNBT.size(); i++) {
            loadParticleGroup(space, groupsNBT.getCompound(i));
        }

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
