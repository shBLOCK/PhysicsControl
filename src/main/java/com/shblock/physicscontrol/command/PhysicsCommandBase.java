package com.shblock.physicscontrol.command;

import com.jme3.bullet.PhysicsSpace;
import com.shblock.physicscontrol.physics.util.NBTSerializer;
import net.minecraft.nbt.CompoundNBT;

import javax.annotation.Nullable;

public class PhysicsCommandBase extends AbstractCommand {
    protected CompoundNBT space;

    // Just give it the real PhysicsSpace, because it will convert it to NBT to save it
    public PhysicsCommandBase(PhysicsSpace space) {
        this.space = NBTSerializer.toNBT(space);
    }

    @Override
    public void execute() {

    }

    @Override
    public void undo() {

    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    protected CompoundNBT toNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.put("space", space);
        return nbt;
    }

    @Override
    protected AbstractCommand fromNBT(CompoundNBT nbt, @Nullable AbstractCommand cmd) {
        if (cmd instanceof PhysicsCommandBase) {
            ((PhysicsCommandBase) cmd).space = nbt.getCompound("space");
        }
        return cmd;
    }
}
