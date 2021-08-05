package com.shblock.physicscontrol.command;

import com.jme3.bullet.PhysicsSpace;
import com.shblock.physicscontrol.client.InteractivePhysicsSimulator2D;
import com.shblock.physicscontrol.physics.util.NBTSerializer;
import net.minecraft.nbt.CompoundNBT;

public abstract class PhysicsCommandBase extends AbstractCommand {
    protected CompoundNBT old_space;

    public PhysicsCommandBase() {}

    public PhysicsCommandBase(PhysicsSpace space) {
        this.old_space = NBTSerializer.toNBT(space);
    }

//    /**
//     * get current {@link PhysicsSpace} object from {@link InteractivePhysicsSimulator2D}.
//     * @return the {@link PhysicsSpace} object
//     */
//    public static PhysicsSpace getSpace() {
//        return InteractivePhysicsSimulator2D.getInstance().getSpace();
//    }

    @Override
    public void undo() {
        InteractivePhysicsSimulator2D.getInstance().setSpace(NBTSerializer.physicsSpaceFromNBT(old_space));
    }

    @Override
    public void redo() {
        super.redo();
        InteractivePhysicsSimulator2D.getInstance().setSimulationRunning(false);
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = super.serializeNBT();
        nbt.put("old_space", this.old_space);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        super.deserializeNBT(nbt);
        this.old_space = nbt.getCompound("old_space");
    }
}
