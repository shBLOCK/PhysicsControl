package com.shblock.physicscontrol.client.gui.PhysicsSimulator;

import com.shblock.physicscontrol.physics.util.NBTSerializer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;
import org.jbox2d.particle.ParticleType;

public class SimulatorConfig implements INBTSerializable<CompoundNBT> {
    // Drag Tool
    public float dragToolMaxForce;
    public float dragToolDampingRatio;
    public boolean dragToolDisableRotation;
    public boolean dragToolDragCenter;
    public float dragToolFrequency;

    // Give Force
    public float giveForceStrength;
    public boolean giveForceIsStatic;
    public float giveForceStaticForce;
    public boolean giveForceOnCenter;

    // Particle Tools
    public float particleToolSize;
    public boolean particleToolSetColor;
    public static final float[] defaultParticleToolColor = new float[]{52F / 255F, 60F / 255F, 1F, 1F};
    public float[] particleToolColor;
    public boolean particleToolSetFlags;
    public static final int defaultParticleToolFlags = ParticleType.b2_colorMixingParticle;
    public int particleToolFlags;

    // Particle Render
    public float particleRenderSmoothLowerBound;
    public float particleRenderSmoothUpperBound;
    public float particleRenderBorderLowerBound;
    public float particleRenderBorderUpperBound;

    public SimulatorConfig() {
        resetDragTool();
        resetGiveForce();
        resetParticleTool();
        resetParticleRender();
    }

    public void resetDragTool() {
        dragToolMaxForce = 1E6F;
        dragToolDampingRatio = 0.7F;
        dragToolDisableRotation = false;
        dragToolDragCenter = false;
        dragToolFrequency = 10F;
    }

    public void resetGiveForce() {
        giveForceStrength = 1F;
        giveForceIsStatic = false;
        giveForceStaticForce = 10F;
        giveForceOnCenter = false;
    }

    public void resetParticleTool() {
        particleToolSize = 1F;
        particleToolSetColor = false;
        particleToolColor = defaultParticleToolColor.clone();
        particleToolSetFlags = false;
        particleToolFlags = defaultParticleToolFlags;
    }

    public void resetParticleRender() {
        particleRenderSmoothLowerBound = 0.3F;
        particleRenderSmoothUpperBound = 4.0F;
        particleRenderBorderLowerBound = 0.75F;
        particleRenderBorderUpperBound = 1.0F;
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putFloat("dragToolMaxForce", this.dragToolMaxForce);
        nbt.putFloat("dragToolDampingRatio", this.dragToolDampingRatio);
        nbt.putBoolean("dragToolDisableRotation", this.dragToolDisableRotation);
        nbt.putBoolean("dragToolDragCenter", this.dragToolDragCenter);
        nbt.putFloat("dragToolFrequency", this.dragToolFrequency);

        nbt.putFloat("giveForceStrength", this.giveForceStrength);
        nbt.putBoolean("giveForceIsStatic", this.giveForceIsStatic);
        nbt.putFloat("giveForceStaticForce", this.giveForceStaticForce);
        nbt.putBoolean("giveForceOnCenter", this.giveForceOnCenter);

        nbt.putFloat("particleToolSize", this.particleToolSize);
        nbt.putBoolean("particleToolSetColor", this.particleToolSetColor);
        nbt.put("particleToolColor", NBTSerializer.toNBT(this.particleToolColor));
        nbt.putBoolean("particleToolSetFlags", this.particleToolSetFlags);
        nbt.putInt("particleToolFlags", this.particleToolFlags);

        nbt.putFloat("particleRenderSmoothLowerBound", this.particleRenderSmoothLowerBound);
        nbt.putFloat("particleRenderSmoothUpperBound", this.particleRenderSmoothUpperBound);
        nbt.putFloat("particleRenderBorderLowerBound", this.particleRenderBorderLowerBound);
        nbt.putFloat("particleRenderBorderUpperBound", this.particleRenderBorderUpperBound);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        this.dragToolMaxForce = nbt.getFloat("dragToolMaxForce");
        this.dragToolDampingRatio = nbt.getFloat("dragToolDampingRatio");
        this.dragToolDisableRotation = nbt.getBoolean("dragToolDisableRotation");
        this.dragToolDragCenter = nbt.getBoolean("dragToolDragCenter");
        this.dragToolFrequency = nbt.getFloat("dragToolFrequency");

        this.giveForceStrength = nbt.getFloat("giveForceStrength");
        this.giveForceIsStatic = nbt.getBoolean("giveForceIsStatic");
        this.giveForceStaticForce = nbt.getFloat("giveForceStaticForce");
        this.giveForceOnCenter = nbt.getBoolean("giveForceOnCenter");

        this.particleToolSize = nbt.getFloat("particleToolSize");
        this.particleToolSetColor = nbt.getBoolean("particleToolSetColor");
        this.particleToolColor = NBTSerializer.floatArrayFromNBT(nbt.get("particleToolColor"));
        this.particleToolSetFlags = nbt.getBoolean("particleToolSetFlags");
        this.particleToolFlags = nbt.getInt("particleToolFlags");

        this.particleRenderSmoothLowerBound = nbt.getFloat("particleRenderSmoothLowerBound");
        this.particleRenderSmoothUpperBound = nbt.getFloat("particleRenderSmoothUpperBound");
        this.particleRenderBorderLowerBound = nbt.getFloat("particleRenderBorderLowerBound");
        this.particleRenderBorderUpperBound = nbt.getFloat("particleRenderBorderUpperBound");
    }
}
