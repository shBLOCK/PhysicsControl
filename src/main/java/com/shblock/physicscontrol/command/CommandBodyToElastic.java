package com.shblock.physicscontrol.command;

import com.shblock.physicscontrol.client.InteractivePhysicsSimulator2D;
import com.shblock.physicscontrol.physics.user_obj.BodyUserObj;
import com.shblock.physicscontrol.physics.user_obj.ElasticGroupUserObj;
import com.shblock.physicscontrol.physics.user_obj.ElasticParticleUserObj;
import com.shblock.physicscontrol.physics.util.BodyHelper;
import com.shblock.physicscontrol.physics.util.ParticleHelper;
import net.minecraft.nbt.CompoundNBT;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.World;
import org.jbox2d.particle.ParticleColor;
import org.jbox2d.particle.ParticleGroup;
import org.jbox2d.particle.ParticleGroupDef;
import org.jbox2d.particle.ParticleType;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicReference;

public class CommandBodyToElastic extends PhysicsCommandBase {
    private int bodyId;
    private int particleFlags;
    private ParticleGroupDef groupDef;

    public CommandBodyToElastic() {}

    public CommandBodyToElastic(Body body, int particleFlags, @Nullable ParticleGroupDef groupDef) {
        super(null);
        this.bodyId = ((BodyUserObj) body.getUserData()).getId();
        this.particleFlags = particleFlags;
        this.groupDef = groupDef == null ? new ParticleGroupDef() : groupDef;
    }

    public CommandBodyToElastic(Body body, int particleFlags) {
        this(body, particleFlags, null);
    }

    public CommandBodyToElastic(Body body) {
        this(body, ParticleType.b2_springParticle | ParticleType.b2_elasticParticle);
    }

    @Override
    public void execute() {
        InteractivePhysicsSimulator2D simulator = InteractivePhysicsSimulator2D.getInstance();
        World world = simulator.getSpace();
        Body body = simulator.getBodyFromId(bodyId);
        BodyUserObj obj = (BodyUserObj) body.getUserData();

        groupDef.flags = this.particleFlags;
        groupDef.position.set(body.getPosition());
        groupDef.angle = body.getAngle();
        groupDef.flags = particleFlags;
        groupDef.color = ParticleHelper.particleColorFromFloat4(obj.r, obj.g, obj.b, obj.alpha);
        groupDef.destroyAutomatically = true;

        AtomicReference<ParticleGroup> groupReference = new AtomicReference<>();
        BodyHelper.forEachFixture(body, fixture -> {
            groupDef.shape = fixture.getShape();

            ParticleGroup newGroup = simulator.addParticleGroupLocal(groupDef);
            if (groupReference.get() == null) {
                groupReference.set(newGroup);
            } else {
                simulator.joinParticleGroupLocal(groupReference.get(), newGroup);
            }
        });
        ParticleGroup group = groupReference.get();

        int first = group.getBufferIndex();
        int last = first + group.getParticleCount();
        Vec2[] posBuf = world.getParticlePositionBuffer();
        ParticleColor[] colorBuf = world.getParticleColorBuffer();
        Object[] userObjBuf = world.getParticleUserDataBuffer();
        for (int index = first; index < last; index++) {
            colorBuf[index].set(ParticleHelper.particleColorFromFloat4(obj.getColor4f()));
            Vec2 localPos = body.getLocalPoint(posBuf[index]);
            userObjBuf[index] = new ElasticParticleUserObj(localPos.x, localPos.y);
        }

        group.setUserData(ElasticGroupUserObj.create(simulator.nextGroupId(), obj.getName(), world, group, obj.getMaterial()));

        simulator.deleteBodyLocal(body);
    }

    @Override
    public String getName() {
        return "body_to_elastic";
    }

    @Override
    public CompoundNBT serializeNBT() {
        return super.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        super.deserializeNBT(nbt);
    }
}
