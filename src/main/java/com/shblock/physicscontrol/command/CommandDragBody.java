package com.shblock.physicscontrol.command;

import com.shblock.physicscontrol.client.InteractivePhysicsSimulator2D;
import com.shblock.physicscontrol.physics.user_obj.BodyUserObj;
import com.shblock.physicscontrol.physics.util.NBTSerializer;
import net.minecraft.nbt.CompoundNBT;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;

import javax.annotation.Nullable;

public class CommandDragBody extends PhysicsCommandBase {
    private boolean hasDragCompleted = false;
    private int bodyId;
    private Vec2 posAfter;
    private Vec2 velAfter;
    private float angleAfter;
    private float angularVelAfter;

    public CommandDragBody() {}

    /**
     * @param bodyAfter this value is null means the drag has started, a normal body means the drag has finished.
     */
    public CommandDragBody(@Nullable Body bodyAfter) {
        super(null);
        if (bodyAfter != null) {
            this.bodyId = ((BodyUserObj) bodyAfter.getUserData()).getId();
            this.hasDragCompleted = true;
            this.posAfter = bodyAfter.getPosition();
            this.velAfter = bodyAfter.getLinearVelocity();
            this.angleAfter = bodyAfter.getAngle();
            this.angularVelAfter = bodyAfter.getAngularVelocity();
        }
    }

    @Override
    public void execute() { }

    @Override
    public boolean undo() {
        if (!hasDragCompleted) {
            return false;
        }
        return super.undo();
    }

    @Override
    public void redo() {
        Body body = InteractivePhysicsSimulator2D.getInstance().getBodyFromId(this.bodyId);
        body.setTransform(this.posAfter, this.angleAfter);
        body.setLinearVelocity(this.velAfter);
        body.setAngularVelocity(this.angularVelAfter);
        InteractivePhysicsSimulator2D.getInstance().setSimulationRunning(false);
    }

    @Override
    public boolean mergeWith(AbstractCommand command) {
        if (command instanceof CommandDragBody) {
            CommandDragBody cmd = (CommandDragBody) command;
            if (!this.hasDragCompleted && cmd.hasDragCompleted) {
                this.hasDragCompleted = true;
                this.bodyId = cmd.bodyId;
                this.posAfter = cmd.posAfter;
                this.velAfter = cmd.velAfter;
                this.angleAfter = cmd.angleAfter;
                this.angularVelAfter = cmd.angularVelAfter;
                return true;
            }
        }
        return false;
    }

    @Override
    public String getName() {
        return "drag_body";
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = super.serializeNBT();
        nbt.putBoolean("completed", this.hasDragCompleted);
        if (this.hasDragCompleted) {
            nbt.put("pos", NBTSerializer.toNBT(this.posAfter));
            nbt.put("vel", NBTSerializer.toNBT(this.velAfter));
            nbt.putFloat("angle", this.angleAfter);
            nbt.putFloat("angular_vel", this.angularVelAfter);
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        super.deserializeNBT(nbt);
        this.hasDragCompleted = nbt.getBoolean("completed");
        if (this.hasDragCompleted) {
            this.posAfter = NBTSerializer.vec2FromNBT(nbt.getCompound("pos"));
            this.velAfter = NBTSerializer.vec2FromNBT(nbt.getCompound("vel"));
            this.angleAfter = nbt.getFloat("angle");
            this.angularVelAfter = nbt.getFloat("angular_vel");
        }
    }
}
