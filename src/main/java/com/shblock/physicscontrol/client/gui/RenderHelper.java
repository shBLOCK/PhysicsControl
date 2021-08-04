package com.shblock.physicscontrol.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.vector.Matrix4f;
import org.lwjgl.opengl.GL11;

public class RenderHelper {
    public static int CIRCLE_SIDES = 64;
    public static float CIRCLE_DIRECTION_SIZE = 0.04F;
    public static float COLOR_DECREASE = 0.5F;

    public static void drawCircle(Matrix4f matrix, float radius, float r, float g, float b, float a) {
        RenderSystem.disableTexture();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder builder = tessellator.getBuilder();
        builder.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION_COLOR);
        for (int i=0; i<CIRCLE_SIDES; i++) {
            builder.vertex(matrix,
                    (float) Math.sin(Math.PI * ((float) i / CIRCLE_SIDES) * 2F) * radius,
                    (float) Math.cos(Math.PI * ((float) i / CIRCLE_SIDES) * 2F) * radius,
                    0F)
            .color(r, g, b, a)
            .endVertex();
        }
        tessellator.end();

        RenderSystem.enableTexture();

//        RenderSystem.disableTexture();
//        GL11.glEnable(GL11.GL_POINT_SMOOTH);
//        GL11.glPointSize(radius * scale * 4);
//
//        Tessellator tessellator = Tessellator.getInstance();
//        BufferBuilder builder = tessellator.getBuilder();
//        builder.begin(GL11.GL_POINTS, DefaultVertexFormats.POSITION_COLOR);
//        builder.vertex(matrix, 0F, 0F, 0F).color(r, g, b, a).endVertex();
//        tessellator.end();
//
//        RenderSystem.enableTexture();
//        GL11.glDisable(GL11.GL_POINT_SMOOTH);
//        GL11.glPointSize(1F);
    }

    public static void drawCircle(Matrix4f matrix, float radius, int r, int g, int b, int a) {
        drawCircle(matrix, radius, r / 256F, g / 256F, b / 256F, a / 256F);
    }

    public static void drawCircleDirection(Matrix4f matrix, float radius, float r, float g, float b, float a) {
        RenderSystem.disableTexture();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder builder = tessellator.getBuilder();
        builder.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION_COLOR);
        builder.vertex(matrix, 0F, 0F, 0F).color(r, g, b, a).endVertex();
        float firstX = 0, firstY = 0, lastX = 0, lastY = 0;
        boolean isFirst = true;
        for (int i=0; i<CIRCLE_SIDES; i++) {
            if (Math.abs(CIRCLE_SIDES * 0.5F - i) / CIRCLE_SIDES < CIRCLE_DIRECTION_SIZE) {
                lastX = (float) Math.sin(Math.PI * ((float) i / CIRCLE_SIDES) * 2F) * radius;
                lastY = (float) Math.cos(Math.PI * ((float) i / CIRCLE_SIDES) * 2F) * radius;
                if (isFirst) {
                    firstX = lastX;
                    firstY = lastY;
                    isFirst = false;
                }
                builder.vertex(matrix, lastX, lastY, 0F)
                        .color(r, g, b, a)
                        .endVertex();
            }
        }

        tessellator.end();

        GL11.glEnable(GL11.GL_LINE_SMOOTH);

        builder.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        builder.vertex(matrix, firstX, firstY, 0F).color(r, g, b, a).endVertex();
        builder.vertex(matrix, 0F, 0F, 0F).color(r, g, b, a).endVertex();
        builder.vertex(matrix, lastX, lastY, 0F).color(r, g, b, a).endVertex();
        tessellator.end();

        GL11.glDisable(GL11.GL_LINE_SMOOTH);

        RenderSystem.enableTexture();
    }

    public static void drawCircleDirection(Matrix4f matrix, float radius, int r, int g, int b, int a) {
        drawCircleDirection(matrix, radius, r / 256F, g / 256F, b / 256F, a / 256F);
    }

    public static void drawCircleFrame(Matrix4f matrix, float radius, float lineWidth, float r, float g, float b, float a) {
        RenderSystem.disableTexture();
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        RenderSystem.lineWidth(lineWidth);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder builder = tessellator.getBuilder();
        builder.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
        for (int i=0; i<CIRCLE_SIDES; i++) {
            builder.vertex(matrix,
                    (float) Math.sin(Math.PI * ((float) i / CIRCLE_SIDES) * 2F) * radius,
                    (float) Math.cos(Math.PI * ((float) i / CIRCLE_SIDES) * 2F) * radius,
                    0F)
                    .color(r, g, b, a)
                    .endVertex();
        }
        tessellator.end();

        RenderSystem.enableTexture();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        RenderSystem.lineWidth(1F);
    }

    public static void drawCircleFrame(Matrix4f matrix, float radius, float lineWidth, int r, int g, int b, int a) {
        drawCircleFrame(matrix, radius, lineWidth, r / 256F, g / 256F, b / 256F, a / 256F);
    }
}
