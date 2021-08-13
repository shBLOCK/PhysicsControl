package com.shblock.physicscontrol.client.gui.PhysicsSimulator;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;

public class ToolConfig implements INBTSerializable<CompoundNBT> {
    //Drag Tool
    public float dragToolMaxForce = 1E6F;
    public float dragToolDampingRatio = 0.7F;
    public boolean dragToolDisableRotation = false;
    public boolean dragToolDragCenter = false;
    public float dragToolFrequency = 10F;

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putFloat("dragToolMaxForce", this.dragToolMaxForce);
        nbt.putFloat("dragToolDampingRatio", this.dragToolDampingRatio);
        nbt.putBoolean("dragToolDisableRotation", this.dragToolDisableRotation);
        nbt.putBoolean("dragToolDragCenter", this.dragToolDragCenter);
        nbt.putFloat("dragToolFrequency", this.dragToolFrequency);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        this.dragToolMaxForce = nbt.getFloat("dragToolMaxForce");
        this.dragToolDampingRatio = nbt.getFloat("dragToolDampingRatio");
        this.dragToolDisableRotation = nbt.getBoolean("dragToolDisableRotation");
        this.dragToolDragCenter = nbt.getBoolean("dragToolDragCenter");
        this.dragToolFrequency = nbt.getFloat("dragToolFrequency");
    }
}