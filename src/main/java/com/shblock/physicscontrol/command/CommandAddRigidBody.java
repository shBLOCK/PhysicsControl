package com.shblock.physicscontrol.command;

import com.jme3.bullet.PhysicsSpace;

public class CommandAddRigidBody extends PhysicsCommandBase {
    public CommandAddRigidBody(PhysicsSpace space) {
        super(space);
    }
}
