package com.shblock.physicscontrol.command;

import com.jme3.bullet.PhysicsSpace;
import com.shblock.physicscontrol.client.InteractivePhysicsSimulator2D;
import com.shblock.physicscontrol.physics.util.NBTSerializer;
import net.minecraft.nbt.CompoundNBT;

public abstract class PhysicsCommandBase extends AbstractCommand {
    protected CompoundNBT old_simulator;

    public PhysicsCommandBase() {
        if (InteractivePhysicsSimulator2D.getInstance() != null) {
            this.old_simulator = NBTSerializer.toNBT(InteractivePhysicsSimulator2D.getInstance());
        }
    }

    /**
     * get current {@link PhysicsSpace} object from {@link InteractivePhysicsSimulator2D}.
     * @return the {@link PhysicsSpace} object
     */
    public static PhysicsSpace getSpace() {
        return InteractivePhysicsSimulator2D.getInstance().getSpace();
    }

    @Override
    public void undo() {
        InteractivePhysicsSimulator2D.getInstance().close();
        InteractivePhysicsSimulator2D.setInstance(NBTSerializer.simulator2DFromNBT(this.old_simulator));
    }

    @Override
    public void redo() {
        super.redo();
        InteractivePhysicsSimulator2D.getInstance().setSimulationRunning(false);
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = super.serializeNBT();
        nbt.put("old_simulator", this.old_simulator);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        super.deserializeNBT(nbt);
        this.old_simulator = nbt.getCompound("old_simulator");
    }
}
