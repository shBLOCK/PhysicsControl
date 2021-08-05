package com.shblock.physicscontrol.client.gui;

import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.shblock.physicscontrol.physics.physics2d.CollisionObjectUserObj2D;
import com.shblock.physicscontrol.physics.util.Vector2f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;

import java.util.Random;

public class ShapeRenderer2D {
    public static void drawRigidBody(MatrixStack matrixStack, PhysicsRigidBody body, boolean isSelected) {
        matrixStack.pushPose();

        Vector2f pos = new Vector2f(body.getPhysicsLocation(null));
        com.jme3.math.Quaternion q = body.getPhysicsRotation(null);
        Quaternion rotation = new Quaternion(q.getX(), q.getY(), q.getZ(), q.getW());
        Vector2f scale = new Vector2f(body.getScale(null));

        matrixStack.translate(pos.x, -pos.y, 0F);
        matrixStack.scale(scale.x, scale.y, 1F);
        matrixStack.mulPose(rotation);

        Matrix4f matrix = matrixStack.last().pose();

        CollisionShape shape = body.getCollisionShape();
        CollisionObjectUserObj2D userObj = (CollisionObjectUserObj2D) body.getUserObject();
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
                RenderHelper.drawCircleFrame(matrix, radius, 3F, 1F, 1F, 1F, 1F);
            } else {
                RenderHelper.drawCircleFrame(matrix, radius, 1F, dr, dg, db, a);
            }
        }

        matrixStack.popPose();
    }
}
