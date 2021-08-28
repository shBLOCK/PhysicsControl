package com.shblock.physicscontrol.command;

import com.shblock.physicscontrol.client.InteractivePhysicsSimulator2D;
import com.shblock.physicscontrol.physics.user_obj.BodyUserObj;
import net.minecraft.nbt.CompoundNBT;
import org.jbox2d.dynamics.Body;

public class CommandEditBodyProperty extends PhysicsCommandBase {
    private int pcoId;
    private EditOperations2D.EditOperationBase operation;

    public CommandEditBodyProperty() {}

    public CommandEditBodyProperty(int pcoId, EditOperations2D.EditOperationBase operation) {
        super(null);
        this.pcoId = pcoId;
        this.operation = operation;
    }

    @Override
    public void execute() {
        Body body = InteractivePhysicsSimulator2D.getInstance().getBodyFromId(pcoId);
        this.operation.execute(body, (BodyUserObj) body.getUserData());
    }

    @Override
    public boolean mergeWith(AbstractCommand command) {
        if (command instanceof CommandEditBodyProperty) {
            return operation.mergeWith(((CommandEditBodyProperty) command).operation);
        }
        return false;
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = super.serializeNBT();
        nbt.put("operation", EditOperations2D.toNBT(this.operation));
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        super.deserializeNBT(nbt);
        this.operation = EditOperations2D.fromNBT(nbt.getCompound("operation"));
    }

    @Override
    public String getName() {
        return "edit_body_property";
    }
}
