package com.shblock.physicscontrol.command;

import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.shblock.physicscontrol.client.InteractivePhysicsSimulator2D;
import com.shblock.physicscontrol.physics.physics2d.CollisionObjectUserObj2D;
import net.minecraft.nbt.CompoundNBT;

public class CommandEditPcoProperty extends PhysicsCommandBase {
    private int pcoId;
    private EditOperations2D.EditOperationBase operation;

    public CommandEditPcoProperty() {}

    public CommandEditPcoProperty(int pcoId, EditOperations2D.EditOperationBase operation) {
        super(null);
        this.pcoId = pcoId;
        this.operation = operation;
    }

    @Override
    public void execute() {
        PhysicsCollisionObject pco = InteractivePhysicsSimulator2D.getInstance().getPcoFromId(pcoId);
        this.operation.execute(pco, (CollisionObjectUserObj2D) pco.getUserObject());
    }

    @Override
    public boolean mergeWith(AbstractCommand command) {
        if (command instanceof CommandEditPcoProperty) {
            return operation.mergeWith(((CommandEditPcoProperty) command).operation);
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
        return "edit_pco_property";
    }
}
