package com.shblock.physicscontrol.command;

import com.shblock.physicscontrol.client.InteractivePhysicsSimulator2D;
import com.shblock.physicscontrol.physics.physics.BodyUserObj;
import com.shblock.physicscontrol.physics.util.NBTSerializer;
import net.minecraft.nbt.CompoundNBT;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CommandSetBodyPos extends PhysicsCommandBase {
    private List<Integer> objects;
    private Vec2 offset;
    private boolean isFirst;

    public CommandSetBodyPos() {}

    public CommandSetBodyPos(List<Body> objects, Vec2 offset, boolean isFirst) {
        super(null);
        this.objects = objects.stream().map(pco -> ((BodyUserObj) pco.getUserData()).getId()).collect(Collectors.toList());
        this.offset = offset;
        this.isFirst = isFirst;
    }

    @Override
    public void execute() {
        InteractivePhysicsSimulator2D.getInstance().forEachBody(
                body -> {
                    if (this.objects.contains(((BodyUserObj) body.getUserData()).getId())) {
                        InteractivePhysicsSimulator2D.getInstance().moveBodyLocal(body, new Vec2(this.offset));
                    }
                }
        );
    }

    @Override
    public boolean mergeWith(AbstractCommand command) {
        if (this.isFirst && command instanceof CommandSetBodyPos && ( !((CommandSetBodyPos) command).isFirst) ) {
            this.offset.addLocal(((CommandSetBodyPos) command).offset);
            return true;
        }
        return false;
    }

    @Override
    public String getName() {
        return "move_body";
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
        this.offset = NBTSerializer.vec2FromNBT(nbt.getCompound("offset"));
        this.isFirst = nbt.getBoolean("is_first");
    }
}
