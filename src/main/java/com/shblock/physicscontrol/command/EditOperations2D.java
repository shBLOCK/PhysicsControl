package com.shblock.physicscontrol.command;

import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.shblock.physicscontrol.physics.physics2d.CollisionObjectUserObj2D;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.HashMap;
import java.util.Map;

public class EditOperations2D {
    private static final Map<String, Class<? extends EditOperationBase>> registry_map = new HashMap<>();

    public static void register(String name, Class<? extends EditOperationBase> clz) {
        registry_map.put(name, clz);
    }

    public static void register(Class<? extends EditOperationBase> clz) {
        try {
            register(clz.newInstance().getName(), clz);
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            assert false;
        }
    }

    private static EditOperationBase fromName(String name) {
        try {
            return registry_map.get(name).newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static CompoundNBT toNBT(EditOperationBase operation) {
        CompoundNBT nbt = operation.serializeNBT();
        nbt.putString("type", operation.getName());
        return nbt;
    }

    public static EditOperationBase fromNBT(CompoundNBT nbt) {
        EditOperationBase operation = fromName(nbt.getString("type"));
        operation.deserializeNBT(nbt);
        return operation;
    }

    public static void init() {
        register(SetName.class);
        register(SetColor.class);
    }

    public abstract static class EditOperationBase implements INBTSerializable<CompoundNBT> {
        public abstract void execute(PhysicsCollisionObject pco, CollisionObjectUserObj2D obj);

        public abstract boolean mergeWith(EditOperationBase operation);

        @Override
        public abstract CompoundNBT serializeNBT();

        @Override
        public abstract void deserializeNBT(CompoundNBT nbt);

        public abstract String getName();
    }

    public static class SetName extends EditOperationBase {
        private String name;

        public SetName() {}

        public SetName(String name) {
            this.name = name;
        }

        @Override
        public void execute(PhysicsCollisionObject pco, CollisionObjectUserObj2D obj) {
            obj.setName(name);
        }

        @Override
        public boolean mergeWith(EditOperationBase operation) {
            return false;
        }

        @Override
        public CompoundNBT serializeNBT() {
            CompoundNBT nbt = new CompoundNBT();
            nbt.putString("name", this.name);
            return nbt;
        }

        @Override
        public void deserializeNBT(CompoundNBT nbt) {
            this.name = nbt.getString("name");
        }

        @Override
        public String getName() {
            return "set_name";
        }
    }

    public static class SetColor extends EditOperationBase {
        private int r, g, b, a;

        public SetColor() {}

        public SetColor(int r, int g, int b, int a) {
            this.r = r;
            this.g = g;
            this.b = b;
            this.a = a;
        }

        @Override
        public void execute(PhysicsCollisionObject pco, CollisionObjectUserObj2D obj) {
            obj.r = this.r;
            obj.g = this.g;
            obj.b = this.b;
            obj.alpha = this.a;
        }

        @Override
        public boolean mergeWith(EditOperationBase operation) {
            if (operation instanceof SetColor) {
                SetColor op = (SetColor) operation;
                this.r = op.r;
                this.g = op.g;
                this.b = op.b;
                this.a = op.a;
                return true;
            }
            return false;
        }

        @Override
        public CompoundNBT serializeNBT() {
            CompoundNBT nbt = new CompoundNBT();
            nbt.putInt("r", this.r);
            nbt.putInt("g", this.g);
            nbt.putInt("b", this.b);
            nbt.putInt("a", this.a);
            return nbt;
        }

        @Override
        public void deserializeNBT(CompoundNBT nbt) {
            this.r = nbt.getInt("r");
            this.g = nbt.getInt("g");
            this.b = nbt.getInt("b");
            this.a = nbt.getInt("a");
        }

        @Override
        public String getName() {
            return "set_color";
        }
    }
}
