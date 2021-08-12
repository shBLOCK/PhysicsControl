package com.shblock.physicscontrol.command;

import com.shblock.physicscontrol.client.InteractivePhysicsSimulator2D;
import com.shblock.physicscontrol.physics.physics.BodyUserObj;
import com.shblock.physicscontrol.physics.util.BodyHelper;
import com.shblock.physicscontrol.physics.util.NBTSerializer;
import com.shblock.physicscontrol.physics.util.ShapeHelper;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyType;

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
        register(SetDensity.class);
        register(SetMass.class);
        register(SetFriction.class);
        register(SetRestitution.class);
        register(SetPos.class);
        register(SetRotation.class);
        register(SetLinearVelocity.class);
        register(SetAngularVelocity.class);
        register(StopMovement.class);
        register(SetCollisionGroup.class);
        register(SetStatic.class);
    }

    public abstract static class EditOperationBase implements INBTSerializable<CompoundNBT> {
        public abstract void execute(Body body, BodyUserObj obj);

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
        public void execute(Body body, BodyUserObj obj) {
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

        public SetColor(float r, float g, float b, float a) {
            this.r = (int) (r * 256);
            this.g = (int) (g * 256);
            this.b = (int) (b * 256);
            this.a = (int) (a * 256);
            if (r > 255) {
                r = 255;
            }
            if (g > 255) {
                g = 255;
            }
            if (b > 255) {
                b = 255;
            }
            if (a > 255) {
                a = 255;
            }
        }

        @Override
        public void execute(Body body, BodyUserObj obj) {
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

    public static class SetDensity extends EditOperationBase {
        private float density;

        public SetDensity() {}

        public SetDensity(float density) {
            this.density = density;
        }

        @Override
        public void execute(Body body, BodyUserObj obj) {
            BodyHelper.forEachFixture(
                    body,
                    fixture -> fixture.setDensity(this.density)
            );
            body.resetMassData();
            body.setAwake(true);
            body.setActive(true);
        }

        @Override
        public boolean mergeWith(EditOperationBase operation) {
            if (operation instanceof SetDensity) {
                this.density = ((SetDensity) operation).density;
                return true;
            }
            return false;
        }

        @Override
        public CompoundNBT serializeNBT() {
            CompoundNBT nbt = new CompoundNBT();
            nbt.putFloat("density", this.density);
            return nbt;
        }

        @Override
        public void deserializeNBT(CompoundNBT nbt) {
            this.density = nbt.getFloat("density");
        }

        @Override
        public String getName() {
            return "set_density";
        }
    }

    public static class SetMass extends EditOperationBase {
        private float mass;

        public SetMass() {}

        public SetMass(float mass) {
            this.mass = mass;
        }

        @Override
        public void execute(Body body, BodyUserObj obj) {
            float[] area = {0F};
            BodyHelper.forEachFixture(
                    body,
                    fixture -> area[0] += ShapeHelper.getSurfaceArea2D(fixture.getShape())
            );
            float density = this.mass / area[0];
            BodyHelper.forEachFixture(
                    body,
                    fixture -> fixture.setDensity(density)
            );
            body.resetMassData();
            body.setAwake(true);
            body.setActive(true);
        }

        @Override
        public boolean mergeWith(EditOperationBase operation) {
            if (operation instanceof SetMass) {
                SetMass op = (SetMass) operation;
                this.mass = op.mass;
                return true;
            }
            return false;
        }

        @Override
        public CompoundNBT serializeNBT() {
            CompoundNBT nbt = new CompoundNBT();
            nbt.putFloat("mass", this.mass);
            return nbt;
        }

        @Override
        public void deserializeNBT(CompoundNBT nbt) {
            this.mass = nbt.getFloat("mass");
        }

        @Override
        public String getName() {
            return "set_mass";
        }
    }

    public static class SetFriction extends EditOperationBase {
        private float friction;

        public SetFriction() {}

        public SetFriction(float friction) {
            this.friction = friction;
        }

        @Override
        public void execute(Body body, BodyUserObj obj) {
            body.getFixtureList().setFriction(this.friction);
        }

        @Override
        public boolean mergeWith(EditOperationBase operation) {
            if (operation instanceof SetFriction) {
                SetFriction op = (SetFriction) operation;
                this.friction = op.friction;
                return true;
            }
            return false;
        }

        @Override
        public CompoundNBT serializeNBT() {
            CompoundNBT nbt = new CompoundNBT();
            nbt.putFloat("friction", this.friction);
            return nbt;
        }

        @Override
        public void deserializeNBT(CompoundNBT nbt) {
            this.friction = nbt.getFloat("friction");
        }

        @Override
        public String getName() {
            return "set_friction";
        }
    }

    public static class SetRestitution extends EditOperationBase {
        private float restitution;

        public SetRestitution() {}

        public SetRestitution(float restitution) {
            this.restitution = restitution;
        }

        @Override
        public void execute(Body body, BodyUserObj obj) {
            body.getFixtureList().setRestitution(this.restitution);
        }

        @Override
        public boolean mergeWith(EditOperationBase operation) {
            if (operation instanceof SetRestitution) {
                SetRestitution op = (SetRestitution) operation;
                this.restitution = op.restitution;
                return true;
            }
            return false;
        }

        @Override
        public CompoundNBT serializeNBT() {
            CompoundNBT nbt = new CompoundNBT();
            nbt.putFloat("restitution", this.restitution);
            return nbt;
        }

        @Override
        public void deserializeNBT(CompoundNBT nbt) {
            this.restitution = nbt.getFloat("restitution");
        }

        @Override
        public String getName() {
            return "set_restitution";
        }
    }

//    public static class SetMaterial extends EditOperationBase { // Set the property to a MC material
//        private SetColor operationColor;
////        private SetMass operationMass;
//        private SetFriction operationFriction;
//        private SetRestitution operationRestitution;
//
//        public SetMaterial() {}
//
//        public SetMaterial() {
//
//        }
//
//        @Override
//        public void execute(Body body, CollisionObjectUserObj2D obj) {
//
//        }
//
//        @Override
//        public boolean mergeWith(EditOperationBase operation) {
//            return false;
//        }
//
//        @Override
//        public CompoundNBT serializeNBT() {
//            return null;
//        }
//
//        @Override
//        public void deserializeNBT(CompoundNBT nbt) {
//
//        }
//
//        @Override
//        public String getName() {
//            return null;
//        }
//    }

    public static class SetPos extends EditOperationBase {
        private Vec2 pos;

        public SetPos() {}

        public SetPos(Vec2 velocity) { // axis=0 : X, axis=1 : Y
            this.pos = velocity;
        }

        @Override
        public void execute(Body body, BodyUserObj obj) {
            InteractivePhysicsSimulator2D.getInstance().setBodyPosLocal(body, this.pos);
        }

        @Override
        public boolean mergeWith(EditOperationBase operation) {
            if (operation instanceof SetPos) {
                this.pos = ((SetPos) operation).pos;
                return true;
            }
            return false;
        }

        @Override
        public CompoundNBT serializeNBT() {
            CompoundNBT nbt = new CompoundNBT();
            nbt.put("pos", NBTSerializer.toNBT(this.pos));
            return nbt;
        }

        @Override
        public void deserializeNBT(CompoundNBT nbt) {
            this.pos = NBTSerializer.vec2FromNBT(nbt.getCompound("pos"));
        }

        @Override
        public String getName() {
            return "set_pos";
        }
    }

    public static class SetRotation extends EditOperationBase {
        private float rotation;

        public SetRotation() {}

        public SetRotation(float rotation) {
            this.rotation = rotation;
        }

        @Override
        public void execute(Body body, BodyUserObj obj) {
            body.setTransform(body.getPosition(), this.rotation);
        }

        @Override
        public boolean mergeWith(EditOperationBase operation) {
            if (operation instanceof SetRotation) {
                this.rotation = ((SetRotation) operation).rotation;
                return true;
            }
            return false;
        }

        @Override
        public CompoundNBT serializeNBT() {
            CompoundNBT nbt = new CompoundNBT();
            nbt.putFloat("rotation", this.rotation);
            return nbt;
        }

        @Override
        public void deserializeNBT(CompoundNBT nbt) {
            this.rotation = nbt.getFloat("rotation");
        }

        @Override
        public String getName() {
            return "set_rotation";
        }
    }

    public static class SetLinearVelocity extends EditOperationBase {
        private Vec2 velocity;

        public SetLinearVelocity() {}

        public SetLinearVelocity(Vec2 velocity) { // axis=0 : X, axis=1 : Y
            this.velocity = velocity;
        }

        @Override
        public void execute(Body body, BodyUserObj obj) {
            body.setLinearVelocity(velocity);
        }

        @Override
        public boolean mergeWith(EditOperationBase operation) {
            if (operation instanceof SetLinearVelocity) {
                this.velocity = ((SetLinearVelocity) operation).velocity;
                return true;
            }
            return false;
        }

        @Override
        public CompoundNBT serializeNBT() {
            CompoundNBT nbt = new CompoundNBT();
            nbt.put("velocity", NBTSerializer.toNBT(this.velocity));
            return nbt;
        }

        @Override
        public void deserializeNBT(CompoundNBT nbt) {
            this.velocity = NBTSerializer.vec2FromNBT(nbt.getCompound("velocity"));
        }

        @Override
        public String getName() {
            return "set_linear_velocity";
        }
    }

    public static class SetAngularVelocity extends EditOperationBase {
        private float velocity;

        public SetAngularVelocity() {}

        public SetAngularVelocity(float velocity) { // axis=0 : X, axis=1 : Y
            this.velocity = velocity;
        }

        @Override
        public void execute(Body body, BodyUserObj obj) {
            body.setAngularVelocity(this.velocity);
        }

        @Override
        public boolean mergeWith(EditOperationBase operation) {
            if (operation instanceof SetAngularVelocity) {
                this.velocity = ((SetAngularVelocity) operation).velocity;
                return true;
            }
            return false;
        }

        @Override
        public CompoundNBT serializeNBT() {
            CompoundNBT nbt = new CompoundNBT();
            nbt.putFloat("velocity", this.velocity);
            return nbt;
        }

        @Override
        public void deserializeNBT(CompoundNBT nbt) {
            this.velocity = nbt.getFloat("velocity");
        }

        @Override
        public String getName() {
            return "set_angular_velocity";
        }
    }

    public static class StopMovement extends EditOperationBase {
        public StopMovement() {}

        @Override
        public void execute(Body body, BodyUserObj obj) {
            body.setLinearVelocity(new Vec2(0F, 0F));
            body.setAngularVelocity(0F);
        }

        @Override
        public boolean mergeWith(EditOperationBase operation) {
            return operation instanceof StopMovement;
        }

        @Override
        public CompoundNBT serializeNBT() {
            return new CompoundNBT();
        }

        @Override
        public void deserializeNBT(CompoundNBT nbt) {
        }

        @Override
        public String getName() {
            return "stop_movement";
        }
    }

    public static class SetCollisionGroup extends EditOperationBase {
        private int groups;

        public SetCollisionGroup() {}

        public SetCollisionGroup(int groups) {
            this.groups = groups;
        }

        @Override
        public void execute(Body body, BodyUserObj obj) {
            body.getFixtureList().getFilterData().maskBits = this.groups;
            body.getFixtureList().setFilterData(
                    body.getFixtureList().getFilterData()
            );
        }

        @Override
        public boolean mergeWith(EditOperationBase operation) {
            if (operation instanceof SetCollisionGroup) {
                this.groups = ((SetCollisionGroup) operation).groups;
                return true;
            }
            return false;
        }

        @Override
        public CompoundNBT serializeNBT() {
            CompoundNBT nbt = new CompoundNBT();
            nbt.putInt("groups", this.groups);
            return nbt;
        }

        @Override
        public void deserializeNBT(CompoundNBT nbt) {
            this.groups = nbt.getInt("groups");
        }

        @Override
        public String getName() {
            return "set_collision_group";
        }
    }

    public static class SetStatic extends EditOperationBase {
        private boolean isStatic;

        public SetStatic() {}

        public SetStatic(boolean isStatic) {
            this.isStatic = isStatic;
        }

        @Override
        public void execute(Body body, BodyUserObj obj) {
            body.setType(BodyType.STATIC);
        }

        @Override
        public boolean mergeWith(EditOperationBase operation) {
            if (operation instanceof SetStatic) {
                this.isStatic = ((SetStatic) operation).isStatic;
                return true;
            }
            return false;
        }

        @Override
        public CompoundNBT serializeNBT() {
            CompoundNBT nbt = new CompoundNBT();
            nbt.putBoolean("is_static", this.isStatic);
            return nbt;
        }

        @Override
        public void deserializeNBT(CompoundNBT nbt) {
            this.isStatic = nbt.getBoolean("is_static");
        }

        @Override
        public String getName() {
            return "set_static";
        }
    }

    // We can just use CommandChangeZLevel, don't have to make another one
//    public static class ChangeZLevel extends EditOperationBase {
//        private int change;
//
//        public ChangeZLevel() {}
//
//        public ChangeZLevel(int change) {
//            this.change = change;
//        }
//
//        @Override
//        public void execute(Body body, CollisionObjectUserObj2D obj) {
//            InteractivePhysicsSimulator2D.getInstance().changeZLevel(body, this.change);
//        }
//
//        @Override
//        public boolean mergeWith(EditOperationBase operation) {
//            return false;
//        }
//
//        @Override
//        public CompoundNBT serializeNBT() {
//            return null;
//        }
//
//        @Override
//        public void deserializeNBT(CompoundNBT nbt) {
//
//        }
//
//        @Override
//        public String getName() {
//            return null;
//        }
//    }
}
