package com.shblock.physicscontrol.physics.user_obj;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Random;

public class UserObjBase implements INBTSerializable<CompoundNBT> {
    private int id;

    public UserObjBase(int id) {
        this.id = id;
    }

//    protected void setId(int id) {
//        this.id = id;
//    }

    public int getId() {
        return this.id;
    }

    /**
     * Only used in some serialization, DON'T CHANGE THE ID!!!
     */
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putInt("id", this.id);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        this.id = nbt.getInt("id");
    }
}
