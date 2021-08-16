package com.shblock.physicscontrol.command;

import com.shblock.physicscontrol.client.InteractivePhysicsSimulator2D;
import com.shblock.physicscontrol.physics.physics.BodyUserObj;
import net.minecraft.nbt.CompoundNBT;
import org.jbox2d.dynamics.Body;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CommandDeleteBodies extends PhysicsCommandBase {
    private List<Integer> objects = new ArrayList<>();

    public CommandDeleteBodies() {}

    public CommandDeleteBodies(List<Body> objects) {
        super(null);
        this.objects = objects.stream().map(pco -> ((BodyUserObj) pco.getUserData()).getId()).collect(Collectors.toList());
    }

    @Override
    public void execute() {
        InteractivePhysicsSimulator2D.getInstance().forEachBody(
            body -> {
                if (this.objects.contains(((BodyUserObj) body.getUserData()).getId())) {
                    InteractivePhysicsSimulator2D.getInstance().deleteBodyLocal(body);
                }
            }
        );
    }

    @Override
    public String getName() {
        return "delete_bodies";
    }

    @Override
    public CompoundNBT serializeNBT() {
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
