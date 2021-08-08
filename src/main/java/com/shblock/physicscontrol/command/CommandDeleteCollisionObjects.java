package com.shblock.physicscontrol.command;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.shblock.physicscontrol.client.InteractivePhysicsSimulator2D;
import com.shblock.physicscontrol.physics.physics2d.CollisionObjectUserObj2D;
import net.minecraft.nbt.CompoundNBT;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CommandDeleteCollisionObjects extends PhysicsCommandBase {
    private List<Integer> objects;

    public CommandDeleteCollisionObjects() {}

    public CommandDeleteCollisionObjects(List<PhysicsCollisionObject> objects) {
        super(null);
        this.objects = objects.stream().map(pco -> ((CollisionObjectUserObj2D) pco.getUserObject()).getId()).collect(Collectors.toList());
    }

    @Override
    public void execute() {
        for (PhysicsCollisionObject pco : InteractivePhysicsSimulator2D.getInstance().getSpace().getPcoList()) {
            if (this.objects.contains(((CollisionObjectUserObj2D) pco.getUserObject()).getId())) {
                InteractivePhysicsSimulator2D.getInstance().deletePco(pco);
            }
        }
    }

    @Override
    public String getName() {
        return "delete_collision_objects";
    }

    @Override
    public CompoundNBT serializeNBT() { //TODO
        CompoundNBT nbt = super.serializeNBT();
        nbt.putIntArray("objects", this.objects);
        return super.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        super.deserializeNBT(nbt);
        this.objects.clear();
        Arrays.stream(nbt.getIntArray("objects")).forEachOrdered(this.objects::add);
    }
}
