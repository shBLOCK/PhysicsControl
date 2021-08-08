package com.shblock.physicscontrol.command;

import com.shblock.physicscontrol.client.InteractivePhysicsSimulator2D;
import net.minecraft.nbt.CompoundNBT;

public class CommandSingleStep extends PhysicsCommandBase {
    private int steps;

    public CommandSingleStep() {}

    public CommandSingleStep(int steps) {
        super(null);
        this.steps = steps;
    }

    @Override
    public void execute() {
        InteractivePhysicsSimulator2D.getInstance().singleStep(this.steps);
    }

    @Override
    public String getName() {
        return "single_step";
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = super.serializeNBT();
        nbt.putInt("steps", this.steps);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        super.deserializeNBT(nbt);
        this.steps = nbt.getInt("steps");
    }
}
