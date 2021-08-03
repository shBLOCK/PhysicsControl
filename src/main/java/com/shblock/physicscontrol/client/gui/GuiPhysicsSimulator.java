package com.shblock.physicscontrol.client.gui;

import com.google.common.collect.Lists;
import com.jme3.bullet.PhysicsSpace;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.shblock.physicscontrol.client.InteractivePhysicsSimulator;
import com.shblock.physicscontrol.physics.physics2d.CustomSpace2D;
import com.shblock.physicscontrol.physics.util.NBTSerializer;
import imgui.ImGui;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nullable;

public class GuiPhysicsSimulator extends ImGuiBase {
    private final ItemStack item;
    private InteractivePhysicsSimulator simulator;

    protected GuiPhysicsSimulator(@Nullable ItemStack item) {
        super(new StringTextComponent("Physics Simulator"));
        this.item = item;
        CompoundNBT nbt = null;
        if (this.item != null) {
            nbt = item.getTagElement("space");
        }
        PhysicsSpace space;
        if (nbt != null) {
            space = NBTSerializer.physicsSpaceFromNBT(nbt);
        } else {
            space = new PhysicsSpace(PhysicsSpace.BroadphaseType.DBVT); //TODO: make a config option of this
        }
        this.simulator = new InteractivePhysicsSimulator(space);
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void buildImGui() {
        ImGui.showDemoWindow();

        ImGui.beginMainMenuBar();
        ImGui.text("test");
        ImGui.endMainMenuBar();
    }

    @Override
    public void render(MatrixStack matrixStack, int combinedLight, int combinedOverlay, float particleTick) {
        super.render(matrixStack, combinedLight, combinedOverlay, particleTick);
        matrixStack.pushPose();
        renderComponentTooltip(matrixStack, Lists.newArrayList(new StringTextComponent("etxtextextetxettxtetextetxtetxetxtexe"), new StringTextComponent("dgyafsuafgsyfdgsydfgsdygfs"), new StringTextComponent("1234567899764")), combinedLight, combinedOverlay);
        matrixStack.popPose();
    }
}
