package com.shblock.physicscontrol.client.gui.PhysicsSimulator;

import codechicken.lib.render.shader.ShaderProgram;
import codechicken.lib.render.shader.ShaderProgramBuilder;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.shblock.physicscontrol.PhysicsControl;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL43;

import static codechicken.lib.render.shader.ShaderObject.StandardShaderType.FRAGMENT;
import static codechicken.lib.render.shader.ShaderObject.StandardShaderType.VERTEX;

public class FluidRender2D {
    public static ShaderProgram fluidShader = ShaderProgramBuilder.builder()
            .addShader("vert", shader -> shader
                    .type(VERTEX)
                    .source(new ResourceLocation(PhysicsControl.MODID, "shaders/fluid.vert")))
            .addShader("frag", shader -> shader
                    .type(FRAGMENT)
                    .source(new ResourceLocation(PhysicsControl.MODID, "shaders/fluid.frag")))
            .build();

    static {
        ((IReloadableResourceManager) Minecraft.getInstance().getResourceManager()).registerReloadListener(fluidShader);
    }

    public void renderFluid(MatrixStack matrixStack, float partialTicks) {
        matrixStack.pushPose();

        fluidShader.use();



        fluidShader.release();

        matrixStack.popPose();
    }
}
