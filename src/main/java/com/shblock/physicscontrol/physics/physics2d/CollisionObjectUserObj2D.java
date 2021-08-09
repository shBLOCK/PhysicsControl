package com.shblock.physicscontrol.physics.physics2d;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.shblock.physicscontrol.physics.UserObjBase;
import com.shblock.physicscontrol.physics.util.ShapeHelper;
import net.minecraft.nbt.CompoundNBT;

public class CollisionObjectUserObj2D extends UserObjBase {
    private String name;
    private int zLevel;
    public int r = RANDOM.nextInt(156) + 50;
    public int g = RANDOM.nextInt(156) + 50;
    public int b = RANDOM.nextInt(156) + 50;
    public int alpha = 255;

//    private Material material;
    private double surfaceArea;
    private double density;

    /**
     * Dummy constructor for fromNBT, DON'T USE THIS!
     */
    public CollisionObjectUserObj2D() {
        super(-1);
    }

    public CollisionObjectUserObj2D(int id, String name, PhysicsCollisionObject pco) {
        super(id);
        this.zLevel = id;
        this.name = name;
        this.surfaceArea = ShapeHelper.getSurfaceArea2D(pco.getCollisionShape());
        this.density = 2D;
        if (pco instanceof PhysicsRigidBody) {
            ((PhysicsRigidBody) pco).setMass((float) (this.density * this.surfaceArea));
        }
    }

    public void moveZLevelUp(PhysicsSpace space) {
        if (this.zLevel >= space.countCollisionObjects() - 1) {
            return;
        }

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
        if (this.zLevel <= 0) {
            return;
        }

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
        nbt.putDouble("surface_area", this.surfaceArea);
        nbt.putDouble("density", this.density);
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
        this.surfaceArea = nbt.getDouble("surface_area");
        this.density = nbt.getDouble("density");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getSurfaceArea() {
        return surfaceArea;
    }

    public double getDensity() {
        return density;
    }

    public void setDensity(double density) {
        this.density = density;
    }

    public double calculateMass() {
        return getSurfaceArea() * getDensity();
    }
}
