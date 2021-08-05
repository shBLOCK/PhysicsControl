package com.shblock.physicscontrol.client.gui;

import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.PhysicsRayTestResult;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Vector3f;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.shblock.physicscontrol.client.InteractivePhysicsSimulator2D;
import com.shblock.physicscontrol.command.CommandAddRigidBody;
import com.shblock.physicscontrol.command.CommandSingleStep;
import com.shblock.physicscontrol.physics.physics2d.CollisionObjectUserObj2D;
import com.shblock.physicscontrol.physics.util.BoundingBoxHelper;
import com.shblock.physicscontrol.physics.util.NBTSerializer;
import com.shblock.physicscontrol.physics.util.Vector2f;
import imgui.ImGui;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

public class GuiPhysicsSimulator extends ImGuiBase {
    private final ItemStack item;

    private float globalScale = 100F;
    private float scaleSpeed = 0.05F;
    private Vector2f globalTranslate = new Vector2f(0F, 0F);

    private State state = State.NONE;
    private DrawShapes drawingShape = null;
    private final List<Vector2f> drawPoints = new ArrayList<>();
    private boolean isFirstMove = false;

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
        new InteractivePhysicsSimulator2D(space);
    }
    private static InteractivePhysicsSimulator2D getSimulator() {
        return InteractivePhysicsSimulator2D.getInstance();
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
        getSimulator().close();
    }

    @Override
    public void tick() {
        super.tick();
        getSimulator().tick();
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
        getSimulator().frame(particleTick);

        matrixStack.pushPose();
        renderSpace(matrixStack, getSimulator().getSpace());
        matrixStack.popPose();
    }

    private void renderSpace(MatrixStack matrixStack, PhysicsSpace space) {
        matrixStack.pushPose();
//        matrixStack.mulPose();
        matrixStack.translate(this.globalTranslate.x, this.globalTranslate.y, 0F);
        matrixStack.scale(this.globalScale, this.globalScale, 1F);
        BoundingBox screenBB = new BoundingBox(toSpacePos(0F, 0F).toVec3(), toSpacePos(width, height).toVec3());
        for (PhysicsCollisionObject body : space.getPcoList()) {
            if (BoundingBoxHelper.isOverlapping2D(body.boundingBox(null), screenBB)) {
                ShapeRenderer2D.drawCollisionObject(matrixStack, body, getSimulator().isSelected(body));
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
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        switch (button) {
            case 0:
                switch (this.state) {
                    case NONE:
                        if (getSimulator().isPointOnAnySelected(toSpacePos(mouseX, mouseY))) {
                            this.state = State.MOVING;
                            this.isFirstMove = true;
                            return true;
                        }
                        return false;
                }
                return false;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        switch (button) {
            case 2:
                this.globalTranslate.x += deltaX;
                this.globalTranslate.y += deltaY;
                return true;
            case 0:
                switch (this.state) {
                    case MOVING:
                        getSimulator().moveSelected(new Vector2f(deltaX, -deltaY).divideLocal(this.globalScale), this.isFirstMove);
                        if (this.isFirstMove) {
                            this.isFirstMove = false;
                        }
                        return true;
                }
                return false;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        switch (button) {
            case 0:
                switch (this.state) {
                    case NONE:
                        List<PhysicsCollisionObject> results = getSimulator().pointTest(toSpacePos(mouseX, mouseY));
                        if (results.isEmpty()) {
                            if (!hasControlDown()) {
                                getSimulator().unselectAll();
                            }
                        } else {
                            PhysicsCollisionObject top = null;
                            CollisionObjectUserObj2D top_usr_obj = null;
                            for (PhysicsCollisionObject obj : results) {
                                CollisionObjectUserObj2D usr_obj = (CollisionObjectUserObj2D) obj.getUserObject();
                                if (top == null) {
                                    top = obj;
                                    top_usr_obj = usr_obj;
                                } else if (usr_obj.getZLevel() > top_usr_obj.getZLevel()) {
                                    top = obj;
                                    top_usr_obj = usr_obj;
                                }
                            }

                            if (hasControlDown()) {
                                if (getSimulator().isSelected(top)) {
                                    getSimulator().unselect(top);
                                } else {
                                    getSimulator().select(top);
                                }
                            } else {
                                getSimulator().unselectAll();
                                getSimulator().select(top);
                            }
                        }
                        return true;
                    case MOVING:
                        this.state = State.NONE;
                        return true;
                }
                return false;
            case 1:
                PhysicsRigidBody body = new PhysicsRigidBody(new SphereCollisionShape(0.5F), 1F);
                body.setPhysicsLocation(toSpacePos(mouseX, mouseY).toVec3());
                body.setLinearVelocity(new Vector3f(10F, 0F, 0F));
                getSimulator().executeCommand(new CommandAddRigidBody(getSimulator().getSpace(), body));
                return true;
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
                switch (this.state) {
                    case NONE:
                        getSimulator().switchSimulationRunning();
                        return true;
                }
                return false;
            case GLFW_KEY_Z:
                switch (this.state) {
                    case NONE:
                        if (hasControlDown()) {
                            getSimulator().undo();
                        }
                        return true;
                }
                return false;
            case GLFW_KEY_Y:
                switch (this.state) {
                    case NONE:
                        if (hasControlDown()) {
                            getSimulator().redo();
                        }
                        return true;
                }
                return false;
            case GLFW_KEY_S:
                switch (this.state) {
                    case NONE:
                        int steps = 1;
                        if (hasShiftDown()) {
                            steps = (int) (1 / getSimulator().getSpace().getAccuracy());
                        }
                        getSimulator().executeCommand(new CommandSingleStep(steps));
                        return true;
                }
                return false;
        }
        return false;
    }
}

enum State {
    NONE, MOVING, DRAW
}

enum DrawShapes {
    SPHERE, BOX, POLYGON
}
