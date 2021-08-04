package com.shblock.physicscontrol.command;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.shblock.physicscontrol.client.InteractivePhysicsSimulator;
import com.shblock.physicscontrol.physics.util.NBTSerializer;
import net.minecraft.nbt.CompoundNBT;

public class CommandAddRigidBody extends PhysicsCommandBase {
    private CompoundNBT body;

    public CommandAddRigidBody(PhysicsSpace space, PhysicsRigidBody body) {
        super(space);
        this.body = NBTSerializer.toNBT(body);
    }

    public CommandAddRigidBody(PhysicsRigidBody body) {
        this(InteractivePhysicsSimulator.getInstance().getSpace(), body);
    }

    @Override
    public void execute() {
        getSpace().addCollisionObject(NBTSerializer.bodyFromNBT(this.body));
    }

    @Override
    public String getName() {
        return "add_rigid_body";
    }
}
