package com.shblock.physicscontrol.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.shblock.physicscontrol.client.gui.PhysicsSimulator.GuiPhysicsSimulator;
import com.shblock.physicscontrol.physics.util.MyVec2;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.vector.Matrix4f;
import org.jbox2d.common.Vec2;
import org.lwjgl.opengl.GL11;

import java.util.List;

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
    }

    public static void drawSector(Matrix4f matrix, float radius, float size, float r, float g, float b, float a) {
        RenderSystem.disableTexture();
        RenderSystem.disableCull();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder builder = tessellator.getBuilder();
        builder.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION_COLOR);
        builder.vertex(matrix, 0F, 0F, 0F).color(r, g, b, a).endVertex();
        if (size > 0) {
            for (int i = 0; i < CIRCLE_SIDES * size; i++) {
                builder.vertex(matrix,
                        (float) Math.sin(Math.PI * ((float) i / CIRCLE_SIDES) * 2F) * radius,
                        (float) Math.cos(Math.PI * ((float) i / CIRCLE_SIDES) * 2F) * radius,
                        0F)
                        .color(r, g, b, a)
                        .endVertex();
            }
        } else {
            for (int i = CIRCLE_SIDES; i > CIRCLE_SIDES * (1 + size); i--) {
                builder.vertex(matrix,
                        (float) Math.sin(Math.PI * ((float) i / CIRCLE_SIDES) * 2F) * radius,
                        (float) Math.cos(Math.PI * ((float) i / CIRCLE_SIDES) * 2F) * radius,
                        0F)
                        .color(r, g, b, a)
                        .endVertex();
            }
        }
        builder.vertex(matrix,
                (float) Math.sin(Math.PI * size * 2F) * radius,
                (float) Math.cos(Math.PI * size * 2F) * radius,
                0F)
                .color(r, g, b, a)
                .endVertex();
        tessellator.end();

        RenderSystem.enableTexture();
        RenderSystem.enableCull();
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

    public static void drawBoxFrame(Matrix4f matrix, float x1, float y1, float x2, float y2, float lineWidth, float r, float g, float b, float a) {
        RenderSystem.disableTexture();
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        RenderSystem.lineWidth(lineWidth);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder builder = tessellator.getBuilder();
        builder.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
        builder.vertex(matrix, x1, y1, 0F).color(r, g, b, a).endVertex();
        builder.vertex(matrix, x2, y1, 0F).color(r, g, b, a).endVertex();
        builder.vertex(matrix, x2, y2, 0F).color(r, g, b, a).endVertex();
        builder.vertex(matrix, x1, y2, 0F).color(r, g, b, a).endVertex();
        tessellator.end();

        RenderSystem.enableTexture();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        RenderSystem.lineWidth(1F);
    }

    public static void drawBoxFrameWH(Matrix4f matrix, float x, float y, float w, float h, float lineWidth, float r, float g, float b, float a) {
        drawBoxFrame(matrix, x, y, x + w, y + h, lineWidth, r, g, b, a);
    }

    public static void drawBox(Matrix4f matrix, float x1, float y1, float x2, float y2, float r, float g, float b, float a) {
        RenderSystem.disableTexture();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder builder = tessellator.getBuilder();
        builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        builder.vertex(matrix, x1, y1, 0F).color(r, g, b, a).endVertex();
        builder.vertex(matrix, x1, y2, 0F).color(r, g, b, a).endVertex();
        builder.vertex(matrix, x2, y2, 0F).color(r, g, b, a).endVertex();
        builder.vertex(matrix, x2, y1, 0F).color(r, g, b, a).endVertex();
        tessellator.end();

        RenderSystem.enableTexture();
    }

    public static void drawBoxWH(Matrix4f matrix, float x, float y, float w, float h, float r, float g, float b, float a) {
        drawBox(matrix, x, y, x + w, y + h, r, g, b, a);
    }

    public static void drawPolygon(Matrix4f matrix, List<Vec2> vertexes, float r, float g, float b, float a) {
        RenderSystem.disableTexture();
        RenderSystem.disableCull();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder builder = tessellator.getBuilder();
        builder.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION_COLOR);
        for (Vec2 vec : vertexes) {
            builder.vertex(matrix, vec.x, vec.y, 0F).color(r, g, b, a).endVertex();
        }
        tessellator.end();

        RenderSystem.enableTexture();
        RenderSystem.enableCull();
    }

    public static void drawPolygonFrame(Matrix4f matrix, List<Vec2> vertexes, float lineWidth, float r, float g, float b, float a) {
        RenderSystem.disableTexture();
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        RenderSystem.lineWidth(lineWidth);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder builder = tessellator.getBuilder();
        builder.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
        for (Vec2 vec : vertexes) {
            builder.vertex(matrix, vec.x, vec.y, 0F).color(r, g, b, a).endVertex();
        }
        tessellator.end();

        RenderSystem.enableTexture();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        RenderSystem.lineWidth(1F);
    }

    public static void drawLine(Matrix4f matrix, Vec2 start, Vec2 end, float lineWidth, float r, float g, float b, float a) {
        RenderSystem.disableTexture();
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        RenderSystem.lineWidth(lineWidth);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder builder = tessellator.getBuilder();
        builder.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        builder.vertex(matrix, start.x, start.y, 0F).color(r, g, b, a).endVertex();
        builder.vertex(matrix, end.x, end.y, 0F).color(r, g, b, a).endVertex();
        tessellator.end();

        RenderSystem.enableTexture();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        RenderSystem.lineWidth(1F);
    }

    public static void drawArrow(Matrix4f matrix, Vec2 start, Vec2 end, float r, float g, float b, float a) {
        RenderSystem.disableTexture();
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        float length = end.sub(start).length();
        RenderSystem.lineWidth(length * 0.15F);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder builder = tessellator.getBuilder();
        builder.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        builder.vertex(matrix, start.x, start.y, 0F).color(r, g, b, a).endVertex();
        builder.vertex(matrix, end.x, end.y, 0F).color(r, g, b, a).endVertex();
        tessellator.end();

        float angle = (float) MyVec2.angle(end.sub(start), new Vec2(0F, 1F));
        if (end.sub(start).x < 0F) {
            angle = -angle;
        }
        float deltaAngle = (float) (Math.PI * 0.15F);
        float size = length * 0.05F;
        RenderSystem.disableCull();
        builder.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);
        builder.vertex(matrix, end.x, end.y, 0F).color(r, g, b, a).endVertex();
        builder.vertex(matrix, end.x - (float) (Math.sin(angle + deltaAngle) * size), end.y - (float) (Math.cos(angle + deltaAngle) * size), 0F).color(r, g, b, a).endVertex();
        builder.vertex(matrix, end.x - (float) (Math.sin(angle - deltaAngle) * size), end.y - (float) (Math.cos(angle - deltaAngle) * size), 0F).color(r, g, b, a).endVertex();
        tessellator.end();
        RenderSystem.enableCull();

        RenderSystem.enableTexture();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        RenderSystem.lineWidth(1F);
    }
}
