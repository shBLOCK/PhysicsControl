package com.shblock.physicscontrol.physics.util;

import org.jbox2d.collision.AABB;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;
import org.jbox2d.particle.ParticleColor;
import org.jbox2d.particle.ParticleDef;
import org.jbox2d.particle.ParticleGroup;
import org.jbox2d.particle.ParticleType;

import java.util.HashSet;
import java.util.Set;

public class ParticleHelper {
    public static Set<Integer> getParticlesInShapes(World world, Shape[] shapes, Transform transform) {
        assert shapes.length != 0;

        AABB aabb = null;
        AABB tmp = new AABB();
        for (Shape shape : shapes) {
            if (aabb == null) {
                aabb = new AABB();
                shape.computeAABB(aabb, transform, 0);
            } else {
                shape.computeAABB(tmp, transform, 0);
                aabb.combine(tmp);
            }
        }

        Set<Integer> result = new HashSet<>();
        Vec2[] buf = world.getParticlePositionBuffer();
        int[] flagsBuf = world.getParticleFlagsBuffer();
        for (int i=0; i<world.getParticleCount(); i++) {
            if ((flagsBuf[i] & ParticleType.b2_zombieParticle) != 0)
                continue;

            Vec2 pos = buf[i];
            if (AABBHelper.isPointIn(pos, aabb)) {
                for (Shape shape : shapes) {
                    if (shape.testPoint(transform, world.getParticlePositionBuffer()[i])) {
                        result.add(i);
                    }
                }
            }
        }

        return result;
    }

    public static Set<Integer> getParticlesInShape(World world, Shape shape, Transform transform) {
        return getParticlesInShapes(world, new Shape[]{shape}, transform);
    }

    public static boolean particleColorEquals(ParticleColor a, ParticleColor b) {
        if (a == null && b == null) return true;
        if ((a == null) != (b == null)) return false;
        return a.r == b.r && a.g == b.g && a.b == b.b && a.a == b.a;
    }

    public static boolean particleDefEquals(ParticleDef a, ParticleDef b, boolean checkPos, boolean checkVel) {
        if (a == null && b == null) return true;
        if ((a == null) != (b == null)) return false;
        if (a.flags != b.flags) return false;
        if (!particleColorEquals(a.color, b.color)) return false;
        if (checkPos && !a.position.equals(b.position)) return false;
        if (checkVel && !a.velocity.equals(b.velocity)) return false;
        if (a.userData == null && b.userData != null) return false;
        if (a.userData != null && b.userData == null) return false;
        if (a.userData != null) {
            return a.userData.equals(b.userData);
        }
        return true;
    }

    public static boolean particleDefEquals(ParticleDef a, ParticleDef b) {
        return particleDefEquals(a, b, false, false);
    }

    public static ParticleColor particleColorFromFloat4(float r, float g, float b, float a) {
        return new ParticleColor(
                (byte) (r * 255F - 128F),
                (byte) (g * 255F - 128F),
                (byte) (b * 255F - 128F),
                (byte) (a * 255F - 128F)
        );
    }

    public static ParticleColor particleColorFromFloat4(float[] color) {
        return particleColorFromFloat4(
                color[0],
                color[1],
                color[2],
                color[3]
        );
    }

    public static float[] particleColorToFloat4(ParticleColor color) {
        return new float[] {
                (color.r + 128) / 255F,
                (color.g + 128) / 255F,
                (color.b + 128) / 255F,
                (color.a + 128) / 255F
        };
    }

    public static AABB getGroupAABB(Vec2[] posBuf, ParticleGroup group) {
        Vec2 lower = null;
        Vec2 upper = null;
        for (int i=group.getBufferIndex(); i<group.getBufferIndex() + group.getParticleCount(); i++) {
            Vec2 pos = posBuf[i];
            if (lower == null) {
                lower = pos.clone();
                upper = pos.clone();
            } else {
                if (pos.x < lower.x)
                    lower.x = pos.x;
                if (pos.y < lower.y)
                    lower.y = pos.y;
                if (pos.x > upper.x)
                    upper.x = pos.x;
                if (pos.y > upper.y)
                    upper.y = pos.y;
            }
        }
        if (lower == null) {
            return null;
        }
        return new AABB(lower, upper);
    }
}
