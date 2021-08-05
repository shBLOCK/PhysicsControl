package com.shblock.physicscontrol.command;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Vector3f;
import com.shblock.physicscontrol.client.InteractivePhysicsSimulator2D;
import com.shblock.physicscontrol.physics.physics2d.CollisionObjectUserObj2D;
import com.shblock.physicscontrol.physics.util.NBTSerializer;
import net.minecraft.nbt.CompoundNBT;

public class CommandAddRigidBody extends PhysicsCommandBase {
    private CompoundNBT body;

    public CommandAddRigidBody() {}

    public CommandAddRigidBody(PhysicsSpace space, PhysicsRigidBody body) {
        super(space);
        body.setUserObject(new CollisionObjectUserObj2D(InteractivePhysicsSimulator2D.getInstance().nextId()));
        body.setLinearFactor(new Vector3f(1F, 1F, 0F));
        body.setAngularFactor(new Vector3f(1F, 0F, 0F));
        this.body = NBTSerializer.toNBT(body);
    }

    @Override
    public void execute() {
        InteractivePhysicsSimulator2D.getInstance().addRigidBody(NBTSerializer.bodyFromNBT(this.body));
    }

    @Override
    public String getName() {
        return "add_rigid_body";
    }
}
