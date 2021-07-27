package com.shblock.physicscontrol.physics.physics2d;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.SolverType;
import com.shblock.physicscontrol.physics.math.Vector2f;

public class CustomWorld2D extends PhysicsSpace {
    private CustomWorld2D(Vector2f worldMin, Vector2f worldMax, BroadphaseType broadphaseType, SolverType solverType) {
        super(worldMin.toVec3(), worldMax.toVec3(), broadphaseType, solverType);
    }

    public CustomWorld2D of(Vector2f worldMin, Vector2f worldMax) {
        return new CustomWorld2D(worldMin, worldMax, BroadphaseType.DBVT, SolverType.SI); //TODO: choose BroadphaseType and SolverType through config file
    }


}
