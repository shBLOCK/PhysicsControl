package com.shblock.physicscontrol.command;

import com.shblock.physicscontrol.client.I18nHelper;
import com.shblock.physicscontrol.client.InteractivePhysicsSimulator2D;
import com.shblock.physicscontrol.physics.physics.BodyUserObj;
import com.shblock.physicscontrol.physics.util.NBTSerializer;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;

import java.util.ArrayList;
import java.util.List;

public class CommandAddRigidBody extends PhysicsCommandBase {
    private CompoundNBT body;
    private ListNBT fixtures;

    public CommandAddRigidBody() {}

    public CommandAddRigidBody(BodyDef body, Shape shape) {
        super(null);
        if (body.getUserData() == null) {
            body.setUserData(InteractivePhysicsSimulator2D.getInstance().getNextUserObj(I18n.get(I18nHelper.getCollisionShapeName(shape))));
        }
        this.body = NBTSerializer.toNBT(body);
        FixtureDef fixture = new FixtureDef();
        fixture.setDensity(2F);
        fixture.shape = shape;
        fixture.filter.groupIndex = 0;
        fixture.filter.maskBits = 0x0001;
        this.fixtures = new ListNBT();
        this.fixtures.add(NBTSerializer.toNBT(fixture));
    }

    public CommandAddRigidBody(BodyDef body, Shape[] shapes) {
        super(null);
        if (body.getUserData() == null) {
            body.setUserData(InteractivePhysicsSimulator2D.getInstance().getNextUserObj(I18n.get(I18nHelper.getCollisionShapeName(shapes[0]))));
        }
        this.fixtures = new ListNBT();
        this.body = NBTSerializer.toNBT(body);
        FixtureDef fixture;
        for (int i=0; i<shapes.length; i++) {
            fixture = new FixtureDef();
            fixture.setDensity(2F);
            fixture.shape = shapes[i];
            fixture.filter.groupIndex = 0;
            fixture.filter.maskBits = 0x0001;
            this.fixtures.add(NBTSerializer.toNBT(fixture));
        }
    }

    @Override
    public void execute() {
        List<FixtureDef> list = new ArrayList<>();
        for (int i=0; i<this.fixtures.size(); i++) {
            list.add(NBTSerializer.fixtureFromNBT(this.fixtures.getCompound(i)));
        }
        InteractivePhysicsSimulator2D.getInstance().addBodyLocal(NBTSerializer.bodyFromNBT(this.body), list.toArray(new FixtureDef[0]));
    }

    @Override
    public String getName() {
        return "add_rigid_body";
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = super.serializeNBT();
        nbt.put("body", this.body);
        nbt.put("fixtures", this.fixtures);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        super.deserializeNBT(nbt);
        this.body = nbt.getCompound("body");
        this.fixtures = nbt.getList("fixtures", Constants.NBT.TAG_COMPOUND);
    }
}
