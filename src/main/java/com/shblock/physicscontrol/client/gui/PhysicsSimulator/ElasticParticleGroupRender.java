package com.shblock.physicscontrol.client.gui.PhysicsSimulator;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.shblock.physicscontrol.physics.user_obj.ElasticGroupUserObj;
import com.shblock.physicscontrol.physics.user_obj.ElasticParticleUserObj;
import com.shblock.physicscontrol.physics.util.ParticleHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;
import org.jbox2d.particle.ParticleColor;
import org.jbox2d.particle.ParticleGroup;
import org.lwjgl.opengl.GL11;

public class ElasticParticleGroupRender {
    public static void render(MatrixStack matrixStack, ParticleGroup group, World world) {
        Vec2[] posBuf = world.getParticlePositionBuffer();
        Object[] objBuf = world.getParticleUserDataBuffer();
        int[] flagsBuf = world.getParticleFlagsBuffer();
        ParticleColor[] colorBuf = world.getParticleColorBuffer();
        ElasticGroupUserObj obj = (ElasticGroupUserObj) group.getUserData();
        Vec2[] uvArray = obj.getUvArray();
        int[] mesh = obj.getMesh();

        Vec2[] vertexes = new Vec2[uvArray.length];
        float[][] colors = new float[uvArray.length][4];
        int first = group.getBufferIndex();
        int last = first + group.getParticleCount();
        for (int i=first; i<last; i++) {
            if (!ParticleHelper.isValidParticle(flagsBuf, i)) {
                continue;
            }
            int index = ((ElasticParticleUserObj) objBuf[i]).uvIndex;
            vertexes[index] = posBuf[i].clone();
            colors[index] = ParticleHelper.particleColorToFloat4(colorBuf[i]);
        }

        matrixStack.pushPose();
        Matrix4f matrix = matrixStack.last().pose();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder builder = tessellator.getBuilder();

        ResourceLocation texture = obj.getTexture();
        boolean haveTex = false;
        if (texture != null) {
            haveTex = true;
            RenderSystem.enableTexture();
            Minecraft.getInstance().textureManager.bind(texture);
            builder.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR_TEX);
        } else {
            RenderSystem.disableTexture();
            builder.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);
        }

        for (int i=mesh.length - 1; i>=0; i--) {
            int index = mesh[i];
            Vec2 vertex = vertexes[index];
            if (vertex != null) {
                builder.vertex(matrix, vertex.x, -vertex.y, 0F);
                float[] color = colors[index];
                builder.color(color[0], color[1], color[2], color[3]);
                if (haveTex) {
                    Vec2 uv = uvArray[index];
                    builder.uv(uv.x, uv.y);
                }
                builder.endVertex();
            }
        }

        tessellator.end();

        if (haveTex) {
            RenderSystem.disableTexture();
        } else {
            RenderSystem.enableTexture();
        }

//        RenderSystem.disableTexture();
////        GL11.glPointSize(5);
//        builder.begin(GL11.GL_POINTS, DefaultVertexFormats.POSITION_COLOR);
//        for (int i=first; i<last; i++) {
//            Vec2 pos = posBuf[i];
//            builder.vertex(matrix, pos.x, -pos.y, 0F).color(0F, 1F, 0F, 1F).endVertex();
//        }
//        tessellator.end();
//        RenderSystem.enableTexture();

        matrixStack.popPose();
    }
}
