package com.shblock.physicscontrol.command;

import com.jme3.bullet.PhysicsSpace;
import com.shblock.physicscontrol.client.InteractivePhysicsSimulator;
import com.shblock.physicscontrol.physics.util.NBTSerializer;
import net.minecraft.nbt.CompoundNBT;

public abstract class PhysicsCommandBase extends AbstractCommand {
    protected CompoundNBT old_space;

    // Just give it the real PhysicsSpace, because it will convert it to NBT to save it
    public PhysicsCommandBase(PhysicsSpace space) {
        this.old_space = NBTSerializer.toNBT(space);
    }

    /**
     * get current {@link PhysicsSpace} object from {@link com.shblock.physicscontrol.client.InteractivePhysicsSimulator}.
     * @return the {@link PhysicsSpace} object
     */
    public static PhysicsSpace getSpace() {
        return InteractivePhysicsSimulator.getInstance().getSpace();
    }

    /**
     * set the current {@link PhysicsSpace} in the current {@link com.shblock.physicscontrol.client.InteractivePhysicsSimulator} to a new {@link PhysicsSpace}.
     * @param new_space the new {@link PhysicsSpace}
     */
    public static void setSpace(PhysicsSpace new_space) {
        InteractivePhysicsSimulator.getInstance().setSpace(new_space);
    }

//    //Must be called after all the changes to the space has been made
//    @Override
//    public void execute() {
//        this.new_space = NBTSerializer.toNBT(getSpace());
//    }

    @Override
    public void undo() {
        setSpace(NBTSerializer.physicsSpaceFromNBT(this.old_space));
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = super.serializeNBT();
        nbt.put("space", this.old_space);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        super.deserializeNBT(nbt);
        this.old_space = nbt.getCompound("space");
    }
}
