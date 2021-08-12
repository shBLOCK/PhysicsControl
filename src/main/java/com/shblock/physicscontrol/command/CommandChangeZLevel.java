package com.shblock.physicscontrol.command;

import com.shblock.physicscontrol.client.InteractivePhysicsSimulator2D;
import com.shblock.physicscontrol.physics.physics.BodyUserObj;
import net.minecraft.nbt.CompoundNBT;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CommandChangeZLevel extends PhysicsCommandBase { //TODO: don't let this command save the whole physics space?
    private int change;
    private List<Integer> objects;

    public CommandChangeZLevel() {}

    public CommandChangeZLevel(int change, List<Body> objects) {
        super(null);
        this.change = change;
        this.objects = objects.stream().map(pco -> ((BodyUserObj) pco.getUserData()).getId()).collect(Collectors.toList());
    }

    @Override
    public void execute() {
        List<BodyUserObj> obj_list = new ArrayList<>();
        InteractivePhysicsSimulator2D.getInstance().forEachBody(
            body -> {
                if (this.objects.contains(((BodyUserObj) body.getUserData()).getId())) {
                    obj_list.add((BodyUserObj) body.getUserData());
                }
            }
        );

        World space = InteractivePhysicsSimulator2D.getInstance().getSpace();

        for (int i=0; i<Math.abs(this.change); i++) {
            change(obj_list, space, this.change > 0 ? 1 : -1);
        }
    }

    private void change(List<BodyUserObj> obj_list, World space, int change) {
        obj_list.sort((a, b) -> {
            return Integer.compare(a.getZLevel(), b.getZLevel()) * (-change); // if change=-1, sort the z-level in inverted order
        });
        for (BodyUserObj obj : obj_list) {
            if (change == 1 && obj.getZLevel() >= space.getBodyCount() - 1) {
                return;
            } else if (change == -1 && obj.getZLevel() <= 0) {
                return;
            }
        }
        for (BodyUserObj obj : obj_list) {
            if (change == 1) {
                obj.moveZLevelUp(space);
            } else {
                obj.moveZLevelDown(space);
            }
        }
    }

    @Override
    public boolean mergeWith(AbstractCommand command) {
        if (command instanceof CommandChangeZLevel) {
            if (((CommandChangeZLevel) command).objects.equals(this.objects)) {
                this.change += ((CommandChangeZLevel) command).change;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean shouldRemove() {
        return this.change == 0;
    }

    @Override
    public String getName() {
        return "change_z_level";
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
