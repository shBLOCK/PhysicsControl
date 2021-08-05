package com.shblock.physicscontrol.client.gui;

import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.shblock.physicscontrol.physics.physics2d.CollisionObjectUserObj2D;
import com.shblock.physicscontrol.physics.util.Vector2f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;

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

        int r = userObj.r;
        int g = userObj.g;
        int b = userObj.b;
        int a = userObj.alpha;
        int dr = (int) (r * RenderHelper.COLOR_DECREASE);
        int dg = (int) (g * RenderHelper.COLOR_DECREASE);
        int db = (int) (b * RenderHelper.COLOR_DECREASE);
        if (shape instanceof SphereCollisionShape) {
            float radius = ((SphereCollisionShape) shape).getRadius();
            RenderHelper.drawCircle(matrix, radius, r, g, b, a);
            RenderHelper.drawCircleDirection(matrix, radius, dr, dg, db, a);
            if (isSelected) {
                RenderHelper.drawCircleFrame(matrix, radius, SELECTED_FRAME_WIDTH, 1F, 1F, 1F, 1F);
            } else {
                RenderHelper.drawCircleFrame(matrix, radius, FRAME_WIDTH, dr, dg, db, a);
            }
        }

        RenderSystem.disableDepthTest();

        matrixStack.popPose();
    }
}
