package com.shblock.physicscontrol.physics.user_obj;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;

public class ElasticParticleUserObj implements INBTSerializable<CompoundNBT> {
    public float u, v;

    public int uvIndex = -1;

    public ElasticParticleUserObj() {}

    public ElasticParticleUserObj(float u, float v) {
        this.u = u;
        this.v = v;
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putFloat("u", this.u);
        nbt.putFloat("v", this.v);
        nbt.putInt("uv_index", this.uvIndex);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        this.u = nbt.getFloat("u");
        this.v = nbt.getFloat("v");
        this.uvIndex = nbt.getInt("uv_index");
    }
}
