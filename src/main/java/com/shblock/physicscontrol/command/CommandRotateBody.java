package com.shblock.physicscontrol.command;

import com.shblock.physicscontrol.client.InteractivePhysicsSimulator2D;
import com.shblock.physicscontrol.physics.user_obj.BodyUserObj;
import net.minecraft.nbt.CompoundNBT;
import org.jbox2d.dynamics.Body;

public class CommandRotateBody extends PhysicsCommandBase {
    private int bodyId;
    private float angle;
    private boolean isFirst;

    public CommandRotateBody() {}

    public CommandRotateBody(Body body, float angle, boolean isFirst) {
        super(null);
        this.bodyId = ((BodyUserObj) body.getUserData()).getId();
        this.angle = angle;
        this.isFirst = isFirst;
    }

    @Override
    public void execute() {
        Body body = InteractivePhysicsSimulator2D.getInstance().getBodyFromId(this.bodyId);
        body.setTransform(body.getPosition(), this.angle);
    }

    @Override
    public boolean mergeWith(AbstractCommand command) {
        if (command instanceof CommandRotateBody) {
            CommandRotateBody cmd = (CommandRotateBody) command;
            if (this.isFirst && !cmd.isFirst && this.bodyId == cmd.bodyId) { // check bodyId just to make sure
                this.angle = cmd.angle;
                return true;
            }
        }
        return false;
    }

    @Override
    public String getName() {
        return "rotate_body";
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = super.serializeNBT();
        nbt.putInt("body_id", this.bodyId);
        nbt.putFloat("angle", this.angle);
        nbt.putBoolean("is_first", this.isFirst);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        super.deserializeNBT(nbt);
        this.bodyId = nbt.getInt("body_id");
        this.angle = nbt.getFloat("angle");
        this.isFirst = nbt.getBoolean("is_first");
    }
}
