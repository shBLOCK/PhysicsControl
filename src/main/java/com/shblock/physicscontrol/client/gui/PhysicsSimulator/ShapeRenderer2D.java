package com.shblock.physicscontrol.client.gui.PhysicsSimulator;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.shblock.physicscontrol.client.gui.RenderHelper;
import com.shblock.physicscontrol.physics.user_obj.BodyUserObj;
import com.shblock.physicscontrol.physics.util.QuaternionUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.Fixture;
import org.lwjgl.opengl.GL11;

public class ShapeRenderer2D {
    private static final float Z_LEVEL_STEP = 0.001F;
    public static float FRAME_WIDTH = 1F;
    public static float SELECTED_FRAME_WIDTH = 5F;

    public static void drawBody(MatrixStack matrixStack, Body body, boolean isSelected) {
        matrixStack.pushPose();

        RenderSystem.enableDepthTest();

        Vec2 pos = new Vec2(body.getPosition());
        Quaternion rotation = QuaternionUtil.setZRadians(-body.getAngle());
//        Vec2 scale = new Vec2(body.getScale(null));

        BodyUserObj userObj = (BodyUserObj) body.getUserData();
        if (userObj == null) {
            return;
        }
        Shape shape = body.getFixtureList().m_shape;

        matrixStack.translate(pos.x, -pos.y, userObj.getZLevel() * Z_LEVEL_STEP - 1000F);
//        matrixStack.scale(scale.x, scale.y, 1F);
        matrixStack.mulPose(rotation);

        Matrix4f matrix = matrixStack.last().pose();

        float r = userObj.getFloatR();
        float g = userObj.getFloatG();
        float b = userObj.getFloatB();
        float a = userObj.getFloatAlpha();
        float dr = r * RenderHelper.COLOR_DECREASE;
        float dg = g * RenderHelper.COLOR_DECREASE;
        float db = b * RenderHelper.COLOR_DECREASE;
        ResourceLocation texture = userObj.getTexture();
        if (shape instanceof CircleShape) {
            float radius = shape.getRadius();
            RenderHelper.drawCircle(matrix, radius, r, g, b, a, texture);

            if (texture == null) {
                RenderHelper.drawCircleDirection(matrix, radius, dr, dg, db, a);
            }

            if (isSelected) {
                RenderHelper.drawCircleFrame(matrix, radius, SELECTED_FRAME_WIDTH, 1F, 1F, 1F, 1F);
            } else {
                RenderHelper.drawCircleFrame(matrix, radius, FRAME_WIDTH, dr, dg, db, a);
            }
        } else if (shape instanceof PolygonShape) {
            PolygonShape poly = (PolygonShape) shape;

            int count;
            Vec2[] vertexes;
            if (userObj.getPolygonVertexCache() != null) {
                vertexes = userObj.getPolygonVertexCache();
                count = vertexes.length;
            } else {
                vertexes = poly.getVertices();
                count = poly.getVertexCount();
            }

            VertexFormat vertexFormat = DefaultVertexFormats.POSITION_COLOR;
            if (texture != null) {
                vertexFormat = DefaultVertexFormats.POSITION_COLOR_TEX;
                Minecraft.getInstance().getTextureManager().bind(texture);
            }

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder builder = tessellator.getBuilder();

            if (texture == null) {
                RenderSystem.disableTexture();
            } else {
                RenderSystem.enableTexture();
            }
            RenderSystem.disableCull();

            if (userObj.getPolygonVertexCache() != null) { // this means this shape is a polygon
                builder.begin(GL11.GL_TRIANGLES, vertexFormat);
                Fixture fixture = body.getFixtureList();
                while (fixture != null) {
                    Shape s = fixture.getShape();
                    if (s instanceof PolygonShape) {
                        PolygonShape polygon = (PolygonShape) s;
                        for (int v=0; v<polygon.getVertexCount(); v++) {
                            Vec2 vertex = polygon.m_vertices[v];
                            builder.vertex(matrix, vertex.x, -vertex.y, 0F)
                                    .color(r, g, b, a)
                                    .uv(vertex.x, -vertex.y)
                                    .endVertex();
                        }
                    }
                    fixture = fixture.m_next;
                }
            } else { // this means this shape is a box
                builder.begin(GL11.GL_POLYGON, vertexFormat);
                for (int i = 0; i < count; i++) {
                    Vec2 vertex = vertexes[i];
                    builder.vertex(matrix, vertex.x, -vertex.y, 0F)
                            .color(r, g, b, a)
                            .uv(vertex.x, -vertex.y)
                            .endVertex();
                }
            }
            tessellator.end();

                GL11.glEnable(GL11.GL_LINE_SMOOTH);
                RenderSystem.lineWidth(isSelected ? SELECTED_FRAME_WIDTH : FRAME_WIDTH);
                RenderSystem.disableTexture();

                builder.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
                for (int i=0; i<count; i++) {
                    Vec2 vertex = vertexes[i];
                    if (isSelected) {
                        builder.vertex(matrix, vertex.x, -vertex.y, 0F).color(1F, 1F, 1F, 1F).endVertex();
                    } else {
                        builder.vertex(matrix, vertex.x, -vertex.y, 0F).color(dr, dg, db, a).endVertex();
                    }
                }
                tessellator.end();

                GL11.glDisable(GL11.GL_LINE_SMOOTH);
                RenderSystem.lineWidth(1F);
                RenderSystem.enableTexture();

            if (texture == null) {
                RenderSystem.enableTexture();
            }
            RenderSystem.enableCull();
        }

        RenderSystem.disableDepthTest();

        matrixStack.popPose();
    }
}
