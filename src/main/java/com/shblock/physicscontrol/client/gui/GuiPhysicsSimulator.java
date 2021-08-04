package com.shblock.physicscontrol.client.gui;

import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.RayTestFlag;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Vector3f;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.shblock.physicscontrol.client.InteractivePhysicsSimulator;
import com.shblock.physicscontrol.command.CommandAddRigidBody;
import com.shblock.physicscontrol.physics.util.BoundingBoxHelper;
import com.shblock.physicscontrol.physics.util.NBTSerializer;
import com.shblock.physicscontrol.physics.util.Vector2f;
import imgui.ImGui;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nullable;

import static org.lwjgl.glfw.GLFW.*;

public class GuiPhysicsSimulator extends ImGuiBase {
    private final ItemStack item;
    private InteractivePhysicsSimulator simulator;

    private float globalScale = 100F;
    private float scaleSpeed = 0.05F;
    private Vector2f globalTranslate = new Vector2f(0F, 0F);

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

    public static GuiPhysicsSimulator tryGetInstance() {
        if (Minecraft.getInstance().screen instanceof GuiPhysicsSimulator) {
            return (GuiPhysicsSimulator) Minecraft.getInstance().screen;
        }
        return null;
    }

    @Override
    public void onClose() {
        super.onClose();
        this.simulator.close();
    }

    @Override
    public void tick() {
        super.tick();
        this.simulator.tick();
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
        this.simulator.frame(particleTick);

        matrixStack.pushPose();
        renderSpace(matrixStack, this.simulator.getSpace());
        matrixStack.popPose();
    }

    private void renderSpace(MatrixStack matrixStack, PhysicsSpace space) {
        matrixStack.pushPose();
//        matrixStack.mulPose();
        matrixStack.translate(this.globalTranslate.x, this.globalTranslate.y, 0F);
        matrixStack.scale(this.globalScale, this.globalScale, 1F);
        BoundingBox screenBB = new BoundingBox(toSpacePos(0F, 0F).toVec3(), toSpacePos(width, height).toVec3());
        for (PhysicsRigidBody body : space.getRigidBodyList()) {
            if (BoundingBoxHelper.isOverlapping2D(body.boundingBox(null), screenBB)) {
                ShapeRenderer2D.drawRigidBody(matrixStack, body, false);
            }
        }
        matrixStack.popPose();
    }

    private Vector2f toSpacePos(float x, float y) {
        return new Vector2f(
                (x - this.globalTranslate.x) / this.globalScale,
                -((y - this.globalTranslate.y) / this.globalScale)
        );
    }

    private Vector2f toSpacePos(double x, double y) {
        return new Vector2f(
                (x - this.globalTranslate.x) / this.globalScale,
                -((y - this.globalTranslate.y) / this.globalScale)
        );
    }

    private Vector2f toScreenPos(float x, float y) {
        return new Vector2f(
                x * this.globalScale + this.globalTranslate.x,
                -(y * this.globalScale) + this.globalTranslate.y
        );
    }

    private Vector2f toScreenPos(double x, double y) {
        return new Vector2f(
                x * this.globalScale + this.globalTranslate.x,
                -(y * this.globalScale) + this.globalTranslate.y
        );
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (button == 2) {
            this.globalTranslate.x += deltaX;
            this.globalTranslate.y += deltaY;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            PhysicsRigidBody body = new PhysicsRigidBody(new SphereCollisionShape(0.5F), 1F);
            body.setPhysicsLocation(toSpacePos(mouseX, mouseY).toVec3());
            body.setLinearVelocity(new Vector3f(10F, 0F, 0F));
            this.simulator.executeCommand(new CommandAddRigidBody(body));
            return true;
        } else if (button == 1) {
            System.out.println(this.simulator.getSpace().rayTestRaw(
                    toSpacePos(mouseX, mouseY).toVec3().add(new Vector3f(0F, 0F, 10000)),
                    toSpacePos(mouseX, mouseY).toVec3().subtract(new Vector3f(0F, 0F, 10000))
            ));
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        Vector2f old_pos = toSpacePos(mouseX, mouseY);

        float old_scale = this.globalScale;
        this.globalScale *= (float) (1 + this.scaleSpeed * delta);
        if (this.globalScale <= 0F) {
            this.globalScale = old_scale;
        }

        Vector2f new_pos = toSpacePos(mouseX, mouseY);
        Vector2f move = new_pos.subtract(old_pos);
        move.y = -move.y;
        move.multLocal(this.globalScale);
        this.globalTranslate.addLocal(move);

        return true;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        switch (keyCode) {
            case GLFW_KEY_SPACE:
                this.simulator.switchSimulationRunning();
                return true;
            case GLFW_KEY_Z:
                if (hasControlDown()) {
                    this.simulator.undo();
                }
                return true;
            case GLFW_KEY_Y:
                if (hasControlDown()) {
                    this.simulator.redo();
                }
                return true;
            case GLFW_KEY_S:
                PhysicsRigidBody body = new PhysicsRigidBody(new SphereCollisionShape(0.5F), 1F);
                body.setPhysicsLocation(new Vector3f(0F, 0F, 0F));
                body.setLinearVelocity(new Vector3f(10F, 0F, 0F));
                this.simulator.executeCommand(new CommandAddRigidBody(body));
                return true;
        }
        return false;
    }
}
