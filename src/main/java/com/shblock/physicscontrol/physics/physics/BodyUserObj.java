package com.shblock.physicscontrol.physics.physics;

import com.shblock.physicscontrol.physics.UserObjBase;
import com.shblock.physicscontrol.physics.util.NBTSerializer;
import com.shblock.physicscontrol.physics.util.ShapeHelper;
import net.minecraft.nbt.CompoundNBT;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.World;

public class BodyUserObj extends UserObjBase {
    private String name;
    private int zLevel;
    public int r = RANDOM.nextInt(156) + 50;
    public int g = RANDOM.nextInt(156) + 50;
    public int b = RANDOM.nextInt(156) + 50;
    public int alpha = 255;

    private Vec2[] polygonVertexCache = null;

//    private Material material;

    /**
     * Dummy constructor for fromNBT, DON'T USE THIS!
     */
    public BodyUserObj() {
        super(-1);
    }

    public BodyUserObj(int id, String name) {
        super(id);
        this.zLevel = id;
        this.name = name;
    }

    public void moveZLevelUp(World space) {
        if (this.zLevel >= space.getBodyCount() - 1) {
            return;
        }

        BodyUserObj closest = null;
        Body body = space.getBodyList();
        while (body != null) {
            BodyUserObj u_obj = (BodyUserObj) body.getUserData();
            if (u_obj != null) {
                if (closest == null) {
                    if (u_obj.zLevel > this.zLevel) {
                        closest = u_obj;
                    }
                } else {
                    if (u_obj.zLevel > this.zLevel && u_obj.zLevel < closest.zLevel) {
                        closest = u_obj;
                    }
                }
            }
            body = body.m_next;
        }
        if (closest != null) {
            int old = this.zLevel;
            this.zLevel = closest.zLevel;
            closest.zLevel = old;
        } else {
            this.zLevel++;
        }
    }

    public void moveZLevelDown(World space) {
        if (this.zLevel <= 0) {
            return;
        }

        BodyUserObj closest = null;
        Body body = space.getBodyList();
        while (body != null) {
            BodyUserObj u_obj = (BodyUserObj) body.getUserData();
            if (u_obj != null) {
                if (closest == null) {
                    if (u_obj.zLevel < this.zLevel) {
                        closest = u_obj;
                    }
                } else {
                    if (u_obj.zLevel < this.zLevel && u_obj.zLevel > closest.zLevel) {
                        closest = u_obj;
                    }
                }
            }
            body = body.m_next;
        }
        if (closest != null) {
            int old = this.zLevel;
            this.zLevel = closest.zLevel;
            closest.zLevel = old;
        } else {
            this.zLevel--;
        }
    }

    public int getZLevel() {
        return this.zLevel;
    }

    public float getFloatR() {
        return this.r / 256F;
    }

    public float getFloatG() {
        return this.g / 256F;
    }

    public float getFloatB() {
        return this.b / 256F;
    }

    public float getFloatAlpha() {
        return this.alpha / 256F;
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = super.serializeNBT();
        nbt.putString("name", name);
        nbt.putInt("z_level", zLevel);
        nbt.putInt("r", r);
        nbt.putInt("g", g);
        nbt.putInt("b", b);
        nbt.putInt("alpha", alpha);
        if (this.polygonVertexCache != null) {
            nbt.put("polygon_vertex_cache", NBTSerializer.toNBT(this.polygonVertexCache));
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        super.deserializeNBT(nbt);
        this.name = nbt.getString("name");
        this.zLevel = nbt.getInt("z_level");
        this.r = nbt.getInt("r");
        this.g = nbt.getInt("g");
        this.b = nbt.getInt("b");
        this.alpha = nbt.getInt("alpha");
        if (nbt.contains("polygon_vertex_cache")) {
            this.polygonVertexCache = NBTSerializer.vec2listFromNBT(nbt.get("polygon_vertex_cache"));
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Vec2[] getPolygonVertexCache() {
        return polygonVertexCache;
    }

    public void setPolygonVertexCache(Vec2[] polygonVertexCache) {
        this.polygonVertexCache = polygonVertexCache;
    }
}
