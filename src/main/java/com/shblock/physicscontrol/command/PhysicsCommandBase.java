package com.shblock.physicscontrol.command;

import com.shblock.physicscontrol.client.InteractivePhysicsSimulator2D;
import com.shblock.physicscontrol.physics.util.NBTSerializer;
import net.minecraft.nbt.CompoundNBT;
import org.jbox2d.dynamics.World;

import javax.annotation.Nullable;

public abstract class PhysicsCommandBase extends AbstractCommand {
    protected CompoundNBT old_simulator;

    public PhysicsCommandBase() {}

    public PhysicsCommandBase(Object dummy) {
        this.old_simulator = InteractivePhysicsSimulator2D.getInstance().serializeNBT();
        this.old_simulator.remove("command_history");
    }

//    /**
//     * get current {@link PhysicsSpace} object from {@link InteractivePhysicsSimulator2D}.
//     * @return the {@link PhysicsSpace} object
//     */
//    public static PhysicsSpace getSpace() {
//        return InteractivePhysicsSimulator2D.getInstance().getSpace();
//    }

    @Override
    public boolean undo() {
        InteractivePhysicsSimulator2D.getInstance().deserializeNBT(this.old_simulator);
        return true;
    }

    @Override
    public void redo() {
        super.redo();
        InteractivePhysicsSimulator2D.getInstance().setSimulationRunning(false);
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.put("old_simulator", this.old_simulator);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        this.old_simulator = nbt.getCompound("old_simulator");
    }
}
