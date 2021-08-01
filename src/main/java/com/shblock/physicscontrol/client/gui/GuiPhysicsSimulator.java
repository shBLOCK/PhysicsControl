package com.shblock.physicscontrol.client.gui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import imgui.ImGui;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class GuiPhysicsSimulator extends ImGuiBase {
    protected GuiPhysicsSimulator(ITextComponent p_i51108_1_) {
        super(p_i51108_1_);
    }

    @Override
    public void buildImGui() {
        ImGui.showDemoWindow();
    }

    @Override
    public void render(MatrixStack matrixStack, int combinedLight, int combinedOverlay, float particleTick) {
        super.render(matrixStack, combinedLight, combinedOverlay, particleTick);
        matrixStack.pushPose();
        renderComponentTooltip(matrixStack, Lists.newArrayList(new StringTextComponent("etxtextextetxettxtetextetxtetxetxtexe"), new StringTextComponent("dgyafsuafgsyfdgsydfgsdygfs"), new StringTextComponent("1234567899764")), combinedLight, combinedOverlay);
        matrixStack.popPose();
    }
}
