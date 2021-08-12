package com.shblock.physicscontrol.physics.util;

import com.shblock.physicscontrol.physics.UserObjBase;
import com.shblock.physicscontrol.physics.physics.BodyUserObj;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.joints.Joint;
import org.jbox2d.serialization.JbSerializer;

public class IdSigner implements JbSerializer.ObjectSigner {
    @Override
    public Long getTag(World world) {
        return null;
    }

    @Override
    public Long getTag(Body body) {
        if (body.getUserData() instanceof UserObjBase) {
            return (long) ((UserObjBase) body.getUserData()).getId();
        }
        return null;
    }

    @Override
    public Long getTag(Shape shape) {
        return null;
    }

    @Override
    public Long getTag(Fixture fixture) {
        return null;
    }

    @Override
    public Long getTag(Joint joint) {
        if (joint.getUserData() instanceof UserObjBase) {
            return (long) ((UserObjBase) joint.getUserData()).getId();
        }
        return null;
    }
}
