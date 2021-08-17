package com.shblock.physicscontrol.command;

import com.shblock.physicscontrol.client.InteractivePhysicsSimulator2D;
import com.shblock.physicscontrol.physics.util.NBTSerializer;
import net.minecraft.nbt.CompoundNBT;
import org.jbox2d.common.Vec2;

public class CommandPasteBodies extends PhysicsCommandBase {
    private CompoundNBT data = new CompoundNBT();
    private Vec2 mousePos = null;

    public CommandPasteBodies() {}

    public CommandPasteBodies(CompoundNBT data, Vec2 mousePos) {
        super(null);
        this.data = data;
        this.mousePos = mousePos;
    }

    @Override
    public void execute() {
        InteractivePhysicsSimulator2D.getInstance().pasteBodiesLocal(data, mousePos);
    }

    @Override
    public String getName() {
        return "paste_bodies";
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = super.serializeNBT();
        nbt.put("data", this.data);
        nbt.put("mouse_pos", NBTSerializer.toNBT(this.mousePos));
        return super.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        super.deserializeNBT(nbt);
        this.data = nbt.getCompound("data");
        this.mousePos = NBTSerializer.vec2FromNBT(nbt.getCompound("mouse_pos"));
    }
}
