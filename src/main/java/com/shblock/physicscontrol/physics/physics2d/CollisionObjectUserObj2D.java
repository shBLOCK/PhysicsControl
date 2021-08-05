package com.shblock.physicscontrol.physics.physics2d;

import com.shblock.physicscontrol.physics.UserObjBase;
import net.minecraft.nbt.CompoundNBT;

public class CollisionObjectUserObj2D extends UserObjBase {
    public int r = RANDOM.nextInt(156) + 50;
    public int g = RANDOM.nextInt(156) + 50;
    public int b = RANDOM.nextInt(156) + 50;
    public int alpha = 255;

    public CollisionObjectUserObj2D(int id) {
        super(id);
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = super.serializeNBT();
        nbt.putInt("r", r);
        nbt.putInt("g", g);
        nbt.putInt("b", b);
        nbt.putInt("alpha", alpha);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        this.r = nbt.getInt("r");
        this.g = nbt.getInt("g");
        this.b = nbt.getInt("b");
        this.alpha = nbt.getInt("alpha");
    }
}
