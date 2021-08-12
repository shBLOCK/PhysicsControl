package com.shblock.physicscontrol.command;

import com.shblock.physicscontrol.client.InteractivePhysicsSimulator2D;
import com.shblock.physicscontrol.physics.util.NBTSerializer;
import net.minecraft.nbt.CompoundNBT;
import org.jbox2d.dynamics.World;

import javax.annotation.Nullable;

public abstract class PhysicsCommandBase extends AbstractCommand {
    protected CompoundNBT old_space;

    public PhysicsCommandBase() {}

    public PhysicsCommandBase(@Nullable World space) {
        if (space == null) {
            space = InteractivePhysicsSimulator2D.getInstance().getSpace();
        }
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
        InteractivePhysicsSimulator2D.getInstance().setSpace(NBTSerializer.spaceFromNBT(old_space));
    }

    @Override
    public void redo() {
        super.redo();
        InteractivePhysicsSimulator2D.getInstance().setSimulationRunning(false);
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.put("old_space", this.old_space);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        this.old_space = nbt.getCompound("old_space");
    }
}
