package com.shblock.physicscontrol.command;

import com.shblock.physicscontrol.client.InteractivePhysicsSimulator2D;
import com.shblock.physicscontrol.physics.util.NBTSerializer;
import com.shblock.physicscontrol.physics.util.ParticleHelper;
import net.minecraft.nbt.CompoundNBT;
import org.apache.commons.lang3.ArrayUtils;
import org.jbox2d.common.Vec2;
import org.jbox2d.particle.ParticleDef;

public class CommandCreateParticles extends PhysicsCommandBase {
    private ParticleDef def;
    private Vec2[] positions;

    public CommandCreateParticles() {}

    public CommandCreateParticles(ParticleDef def, Vec2[] positions) {
        super(null);
        this.def = def;
        this.positions = positions;
    }

    @Override
    public void execute() {
        InteractivePhysicsSimulator2D simulator = InteractivePhysicsSimulator2D.getInstance();

        for (Vec2 pos : positions) {
            this.def.position.set(pos);
            simulator.addParticleLocal(def);
        }
    }

    @Override
    public boolean mergeWith(AbstractCommand command) {
        if (command instanceof CommandCreateParticles) {
            CommandCreateParticles cmd = (CommandCreateParticles) command;
            if (ParticleHelper.particleDefEquals(this.def, cmd.def)) {
                this.positions = ArrayUtils.addAll(this.positions, cmd.positions);
                return true;
            }
        }
        return false;
    }

    @Override
    public String getName() {
        return "create_particles";
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = super.serializeNBT();
        nbt.put("def", NBTSerializer.toNBT(this.def));
        nbt.put("positions", NBTSerializer.toNBT(this.positions));
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        super.deserializeNBT(nbt);
        this.def = NBTSerializer.particleDefFromNBT(nbt.getCompound("def"));
        this.positions = NBTSerializer.vec2listFromNBT(nbt.get("positions"));
    }
}
