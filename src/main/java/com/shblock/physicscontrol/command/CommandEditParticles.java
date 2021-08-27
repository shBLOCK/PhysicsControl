package com.shblock.physicscontrol.command;

import com.shblock.physicscontrol.client.InteractivePhysicsSimulator2D;
import com.shblock.physicscontrol.physics.util.NBTSerializer;
import com.shblock.physicscontrol.physics.util.ParticleHelper;
import net.minecraft.nbt.CompoundNBT;
import org.jbox2d.dynamics.World;
import org.jbox2d.particle.ParticleColor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class CommandEditParticles extends PhysicsCommandBase {
    private Set<Integer> indexes;
    private ParticleColor color;
    private int flags;

    public CommandEditParticles() {}

    public CommandEditParticles(Set<Integer> indexes, ParticleColor color, int flags) {
        super(null);
        this.indexes = indexes;
        this.color = color;
        this.flags = flags;
    }

    public CommandEditParticles(Set<Integer> indexes, ParticleColor color) {
        this(indexes, color, -1);
    }

    public CommandEditParticles(Set<Integer> indexes, int flags) {
        this(indexes, null, flags);
    }

    @Override
    public void execute() {
        World world = InteractivePhysicsSimulator2D.getInstance().getSpace();

        if (this.color != null) {
            for (int index : this.indexes) {
                world.getParticleColorBuffer()[index].set(color);
            }
        }

        if (this.flags != -1) {
            for (int index : this.indexes) {
                world.getParticleFlagsBuffer()[index] = this.flags;
            }
        }
    }

    @Override
    public boolean mergeWith(AbstractCommand command) {
        if (command instanceof CommandEditParticles) {
            CommandEditParticles cmd = (CommandEditParticles) command;
            if (ParticleHelper.particleColorEquals(this.color, cmd.color) && this.flags == cmd.flags) {
                this.indexes.addAll(cmd.indexes);
                return true;
            }
        }
        return false;
    }

    @Override
    public String getName() {
        return "edit_particles";
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = super.serializeNBT();
        nbt.putIntArray("indexes", new ArrayList<>(this.indexes));
        nbt.putInt("flags", this.flags);
        if (this.color != null) {
            nbt.put("color", NBTSerializer.toNBT(this.color));
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        super.deserializeNBT(nbt);
        this.indexes = Arrays.stream(nbt.getIntArray("indexes")).boxed().collect(Collectors.toSet());
        this.flags = nbt.getInt("flags");
        if (nbt.contains("color")) {
            this.color = NBTSerializer.particleColorFromNBT(nbt.getCompound("color"));
        } else {
            this.color = null;
        }
    }
}
