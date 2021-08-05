package com.shblock.physicscontrol.physics.physics2d;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.shblock.physicscontrol.physics.UserObjBase;
import net.minecraft.nbt.CompoundNBT;

public class CollisionObjectUserObj2D extends UserObjBase {
    private int zLevel;
    public int r = RANDOM.nextInt(156) + 50;
    public int g = RANDOM.nextInt(156) + 50;
    public int b = RANDOM.nextInt(156) + 50;
    public int alpha = 255;

    public CollisionObjectUserObj2D(int id) {
        super(id);
        this.zLevel = id;
    }

    public void moveZLevelUp(PhysicsSpace space) {
        CollisionObjectUserObj2D closest = null;
        for (PhysicsCollisionObject obj : space.getPcoList()) {
            CollisionObjectUserObj2D u_obj = (CollisionObjectUserObj2D) obj.getUserObject();
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
        if (closest != null) {
            int old = this.zLevel;
            this.zLevel = closest.zLevel;
            closest.zLevel = old;
        } else {
            this.zLevel++;
        }
    }

    public void moveZLevelDown(PhysicsSpace space) {
        CollisionObjectUserObj2D closest = null;
        for (PhysicsCollisionObject obj : space.getPcoList()) {
            CollisionObjectUserObj2D u_obj = (CollisionObjectUserObj2D) obj.getUserObject();
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

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = super.serializeNBT();
        nbt.putInt("z_level", zLevel);
        nbt.putInt("r", r);
        nbt.putInt("g", g);
        nbt.putInt("b", b);
        nbt.putInt("alpha", alpha);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        super.deserializeNBT(nbt);
        this.zLevel = nbt.getInt("z_level");
        this.r = nbt.getInt("r");
        this.g = nbt.getInt("g");
        this.b = nbt.getInt("b");
        this.alpha = nbt.getInt("alpha");
    }
}
