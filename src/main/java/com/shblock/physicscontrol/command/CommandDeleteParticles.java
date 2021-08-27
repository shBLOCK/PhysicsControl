package com.shblock.physicscontrol.command;

import com.shblock.physicscontrol.client.InteractivePhysicsSimulator2D;
import net.minecraft.nbt.CompoundNBT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class CommandDeleteParticles extends PhysicsCommandBase {
    private Set<Integer> indexes;

    public CommandDeleteParticles() {}

    public CommandDeleteParticles(Set<Integer> indexes) {
        super(null);
        this.indexes = indexes;
    }

    public CommandDeleteParticles(int[] indexes) {
        this(new HashSet<>());
        for (int index : indexes) {
            this.indexes.add(index);
        }
    }

    @Override
    public void execute() {
        InteractivePhysicsSimulator2D simulator = InteractivePhysicsSimulator2D.getInstance();

        for (int index : this.indexes) {
            simulator.deleteParticleLocal(index);
        }
    }

    @Override
    public boolean mergeWith(AbstractCommand command) {
        if (command instanceof CommandDeleteParticles) {
            CommandDeleteParticles cmd = (CommandDeleteParticles) command;
            this.indexes.addAll(cmd.indexes);
            return true;
        }
        return false;
    }

    @Override
    public String getName() {
        return "delete_particles";
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = super.serializeNBT();
        nbt.putIntArray("indexes", new ArrayList<>(this.indexes));
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        super.deserializeNBT(nbt);
        this.indexes = Arrays.stream(nbt.getIntArray("indexes")).boxed().collect(Collectors.toSet());
    }
}
