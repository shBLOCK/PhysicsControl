package com.shblock.physicscontrol.command;

import com.shblock.physicscontrol.client.InteractivePhysicsSimulator2D;
import net.minecraft.nbt.CompoundNBT;
import org.jbox2d.dynamics.joints.Joint;

public class CommandDeleteBearing extends PhysicsCommandBase {
    private int id;

    public CommandDeleteBearing() {}

    public CommandDeleteBearing(int id) {
        super(null);
        this.id = id;
    }

    @Override
    public void execute() {
        Joint joint = InteractivePhysicsSimulator2D.getInstance().getSpace().getJointList();
        for (int i=0; i<id; i++) {
            joint = joint.getNext();
        }
        InteractivePhysicsSimulator2D.getInstance().deleteJoint(joint);
    }

    @Override
    public String getName() {
        return "delete_bearing";
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = super.serializeNBT();

        nbt.putInt("id", this.id);

        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        super.deserializeNBT(nbt);
        this.id = nbt.getInt("id");
    }
}
