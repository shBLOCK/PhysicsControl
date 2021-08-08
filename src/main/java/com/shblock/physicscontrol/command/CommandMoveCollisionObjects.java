package com.shblock.physicscontrol.command;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.objects.PhysicsGhostObject;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Vector3f;
import com.shblock.physicscontrol.client.InteractivePhysicsSimulator2D;
import com.shblock.physicscontrol.physics.physics2d.CollisionObjectUserObj2D;
import com.shblock.physicscontrol.physics.util.NBTSerializer;
import com.shblock.physicscontrol.physics.util.Vector2f;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.Constants;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CommandMoveCollisionObjects extends PhysicsCommandBase {
    private List<Integer> objects;
    private Vector3f offset;
    private boolean isFirst;

    public CommandMoveCollisionObjects() {}

    public CommandMoveCollisionObjects(List<PhysicsCollisionObject> objects, Vector3f offset, boolean isFirst) {
        super(null);
        this.objects = objects.stream().map(pco -> ((CollisionObjectUserObj2D) pco.getUserObject()).getId()).collect(Collectors.toList());
        this.offset = offset;
        this.isFirst = isFirst;
    }

    @Override
    public void execute() {
        for (PhysicsCollisionObject pco : InteractivePhysicsSimulator2D.getInstance().getSpace().getPcoList()) {
            if (this.objects.contains(((CollisionObjectUserObj2D) pco.getUserObject()).getId())) {
                InteractivePhysicsSimulator2D.getInstance().movePco(pco, new Vector2f(this.offset));
            }
        }
    }

    @Override
    public boolean mergeWith(AbstractCommand command) {
        if (this.isFirst && command instanceof CommandMoveCollisionObjects && ( !((CommandMoveCollisionObjects) command).isFirst) ) {
            this.offset.addLocal(((CommandMoveCollisionObjects) command).offset);
            return true;
        }
        return false;
    }

    @Override
    public String getName() {
        return "move_collision_objects";
    }

    @Override
    public CompoundNBT serializeNBT() { //TODO
        CompoundNBT nbt = super.serializeNBT();
        nbt.putIntArray("objects", this.objects);
        nbt.put("offset", NBTSerializer.toNBT(this.offset));
        nbt.putBoolean("is_first", this.isFirst);
        return super.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        super.deserializeNBT(nbt);
        this.objects.clear();
        Arrays.stream(nbt.getIntArray("objects")).forEachOrdered(this.objects::add);
        this.offset = NBTSerializer.vec3FromNBT(nbt.getList("offset", Constants.NBT.TAG_FLOAT));
        this.isFirst = nbt.getBoolean("is_first");
    }
}
