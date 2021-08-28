package com.shblock.physicscontrol.command;

import com.shblock.physicscontrol.client.InteractivePhysicsSimulator2D;
import com.shblock.physicscontrol.physics.user_obj.BodyUserObj;
import com.shblock.physicscontrol.physics.util.NBTSerializer;
import net.minecraft.nbt.CompoundNBT;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;

public class CommandGiveForce extends PhysicsCommandBase {
    private int bodyId;
    private Vec2 point;
    private Vec2 force;

    public CommandGiveForce() {}

    public CommandGiveForce(Body body, Vec2 point, Vec2 force) {
        super(null);
        this.bodyId = ((BodyUserObj) body.getUserData()).getId();
        this.point = point;
        this.force = force;
    }

    @Override
    public void execute() {
        Body body = InteractivePhysicsSimulator2D.getInstance().getBodyFromId(this.bodyId);
        body.applyLinearImpulse(force, body.getWorldPoint(point), true);
    }

    @Override
    public String getName() {
        return "give_force";
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = super.serializeNBT();
        nbt.putInt("body_id", this.bodyId);
        nbt.put("point", NBTSerializer.toNBT(this.point));
        nbt.put("force", NBTSerializer.toNBT(this.force));
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        super.deserializeNBT(nbt);
        this.bodyId = nbt.getInt("body_id");
        this.point = NBTSerializer.vec2FromNBT(nbt.getCompound("point"));
        this.force = NBTSerializer.vec2FromNBT(nbt.getCompound("force"));
    }
}
