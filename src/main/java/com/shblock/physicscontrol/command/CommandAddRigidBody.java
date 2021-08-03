package com.shblock.physicscontrol.command;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.objects.PhysicsRigidBody;

public class CommandAddRigidBody extends PhysicsCommandBase {
    private PhysicsRigidBody body;

    public CommandAddRigidBody(PhysicsSpace space, PhysicsRigidBody body) {
        super(space);
        this.body = body;
    }

    @Override
    public void execute() {
        getSpace().addCollisionObject(body);
    }

    @Override
    public String getName() {
        return "add_rigid_body";
    }
}
