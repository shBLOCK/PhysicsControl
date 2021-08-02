package com.shblock.physicscontrol.physics.util;

import com.jme3.bullet.PhysicsSpace;

public class Cloner {
    public PhysicsSpace clonePhysicsSpace(PhysicsSpace space) { //TODO: don't convert to NBT to improve performance (use NBT to clone is just for testing, will rewrite later)
        return NBTSerializer.physicsSpaceFromNBT(NBTSerializer.toNBT(space));
    }
}
