package com.shblock.physicscontrol.command;

import com.shblock.physicscontrol.client.InteractivePhysicsSimulator2D;
import com.shblock.physicscontrol.physics.util.NBTSerializer;
import net.minecraft.nbt.CompoundNBT;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.joints.RevoluteJoint;
import org.jbox2d.dynamics.joints.RevoluteJointDef;

public class CommandPlaceBearing extends PhysicsCommandBase {
    private RevoluteJointDef jointDef;

    public CommandPlaceBearing() {}

    public CommandPlaceBearing(Body a, Body b, Vec2 pos, float size) {
        super(null);

        jointDef = new RevoluteJointDef();
        jointDef.bodyA = a;
        jointDef.bodyB = b;
        jointDef.localAnchorA = a.getLocalPoint(pos);
        if (b != null) {
            jointDef.localAnchorB = b.getLocalPoint(pos);
        }
        jointDef.userData = size;
    }

    @Override
    public void execute() {
        RevoluteJoint joint = (RevoluteJoint) InteractivePhysicsSimulator2D.getInstance().addJoint(jointDef);
    }

    @Override
    public String getName() {
        return "add_bearing";
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = super.serializeNBT();

        nbt.put("def", NBTSerializer.toNBT(jointDef));

        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        super.deserializeNBT(nbt);

        this.jointDef = NBTSerializer.bearingDefFromNBT(nbt.getCompound("def"));
    }
}
