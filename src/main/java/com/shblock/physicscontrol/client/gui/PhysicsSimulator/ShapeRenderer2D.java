package com.shblock.physicscontrol.client.gui.PhysicsSimulator;

import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.shapes.Box2dShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.GImpactCollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.collision.shapes.infos.CompoundMesh;
import com.jme3.bullet.collision.shapes.infos.IndexedMesh;
import com.jme3.math.Vector3f;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.shblock.physicscontrol.client.gui.RenderHelper;
import com.shblock.physicscontrol.physics.physics2d.CollisionObjectUserObj2D;
import com.shblock.physicscontrol.physics.util.MeshHelper;
import com.shblock.physicscontrol.physics.util.Vector2f;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public class ShapeRenderer2D {
    private static final float Z_LEVEL_STEP = 0.001F;
    public static float FRAME_WIDTH = 1F;
    public static float SELECTED_FRAME_WIDTH = 5F;

    public static void drawCollisionObject(MatrixStack matrixStack, PhysicsCollisionObject body, boolean isSelected) {
        matrixStack.pushPose();

        RenderSystem.enableDepthTest();

        Vector2f pos = new Vector2f(body.getPhysicsLocation(null));
        com.jme3.math.Quaternion q = body.getPhysicsRotation(null);
        Quaternion rotation = new Quaternion(q.getX(), q.getY(), q.getZ(), q.getW());
        Vector2f scale = new Vector2f(body.getScale(null));

        CollisionShape shape = body.getCollisionShape();
        CollisionObjectUserObj2D userObj = (CollisionObjectUserObj2D) body.getUserObject();

        matrixStack.translate(pos.x, -pos.y, userObj.getZLevel() * Z_LEVEL_STEP - 1000F);
        matrixStack.scale(scale.x, scale.y, 1F);
        matrixStack.mulPose(rotation);

        Matrix4f matrix = matrixStack.last().pose();

        float r = userObj.getFloatR();
        float g = userObj.getFloatG();
        float b = userObj.getFloatB();
        float a = userObj.getFloatAlpha();
        float dr = r * RenderHelper.COLOR_DECREASE;
        float dg = g * RenderHelper.COLOR_DECREASE;
        float db = b * RenderHelper.COLOR_DECREASE;
        if (shape instanceof SphereCollisionShape) {
            float radius = ((SphereCollisionShape) shape).getRadius();
            RenderHelper.drawCircle(matrix, radius, r, g, b, a);
            RenderHelper.drawCircleDirection(matrix, radius, dr, dg, db, a);
            if (isSelected) {
                RenderHelper.drawCircleFrame(matrix, radius, SELECTED_FRAME_WIDTH, 1F, 1F, 1F, 1F);
            } else {
                RenderHelper.drawCircleFrame(matrix, radius, FRAME_WIDTH, dr, dg, db, a);
            }
        } else if (shape instanceof Box2dShape) {
            Box2dShape box = (Box2dShape) shape;
            Vector3f size = box.getHalfExtents(null);
            RenderHelper.drawBox(matrix, -size.x, -size.y, size.x, size.y, r, g, b, a);
            if (isSelected) {
                RenderHelper.drawBoxFrame(matrix, -size.x, -size.y, size.x, size.y, SELECTED_FRAME_WIDTH, 1F, 1F, 1F, 1F);
            } else {
                RenderHelper.drawBoxFrame(matrix, -size.x, -size.y, size.x, size.y, FRAME_WIDTH, dr, dg, db, a);
            }
        } else if (shape instanceof GImpactCollisionShape) {
            CompoundMesh compoundMesh = MeshHelper.getCompoundMesh((GImpactCollisionShape) shape);
            List<Vector2f> renderData = new ArrayList<>();
            List<Vector2f> vertexes = new ArrayList<>();
            for (IndexedMesh mesh : MeshHelper.getSubMeshes(compoundMesh)) {
                IntBuffer indexes = mesh.copyIndices();
                indexes.flip();
                FloatBuffer vertexBuffer = mesh.copyVertexPositions();
                vertexBuffer.flip();
                while (vertexBuffer.hasRemaining()) {
                    vertexes.add(new Vector2f(vertexBuffer.get(), vertexBuffer.get()));
                    vertexBuffer.get(); // ignore the Z value
                }
                while (indexes.hasRemaining()) {
                    renderData.add(vertexes.get(indexes.get()));
                    renderData.add(vertexes.get(indexes.get()));
                    renderData.add(vertexes.get(indexes.get()));
                }
            }

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder builder = tessellator.getBuilder();

            RenderSystem.disableTexture();
            RenderSystem.disableCull();
            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            RenderSystem.lineWidth(isSelected ? SELECTED_FRAME_WIDTH : FRAME_WIDTH);

            builder.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);
            for (Vector2f vertex : renderData) {
                builder.vertex(matrix, vertex.x, -vertex.y, 0F).color(r, g, b, a).endVertex();
            }
            tessellator.end();

            builder.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
            for (Vector2f vertex : vertexes) {
                if (isSelected) {
                    builder.vertex(matrix, vertex.x, -vertex.y, 0F).color(1F, 1F, 1F, 1F).endVertex();
                } else {
                    builder.vertex(matrix, vertex.x, -vertex.y, 0F).color(dr, dg, db, a).endVertex();
                }
            }
            tessellator.end();

            RenderSystem.enableTexture();
            RenderSystem.enableCull();
            GL11.glDisable(GL11.GL_LINE_SMOOTH);
            RenderSystem.lineWidth(1F);

            // for debug
            builder.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
            for (int i=0; i<renderData.size() / 3; i++) {
                builder.vertex(matrix, renderData.get(i * 3).x, -renderData.get(i * 3).y, 0F).color(dr, dg, db, a).endVertex();
                builder.vertex(matrix, renderData.get(i * 3 + 1).x, -renderData.get(i * 3 + 1).y, 0F).color(dr, dg, db, a).endVertex();
                builder.vertex(matrix, renderData.get(i * 3 + 1).x, -renderData.get(i * 3 + 1).y, 0F).color(dr, dg, db, a).endVertex();
                builder.vertex(matrix, renderData.get(i * 3 + 2).x, -renderData.get(i * 3 + 2).y, 0F).color(dr, dg, db, a).endVertex();
                builder.vertex(matrix, renderData.get(i * 3 + 2).x, -renderData.get(i * 3 + 2).y, 0F).color(dr, dg, db, a).endVertex();
                builder.vertex(matrix, renderData.get(i * 3).x, -renderData.get(i * 3).y, 0F).color(dr, dg, db, a).endVertex();
            }
            tessellator.end();
        }

        RenderSystem.disableDepthTest();

        matrixStack.popPose();
    }
}
