package com.shblock.physicscontrol.client.gui.PhysicsSimulator;

import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.shapes.Box2dShape;
import com.jme3.bullet.collision.shapes.GImpactCollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.collision.shapes.infos.IndexedMesh;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Quaternion;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.shblock.physicscontrol.client.InteractivePhysicsSimulator2D;
import com.shblock.physicscontrol.client.gui.GlobalImGuiRenderer;
import com.shblock.physicscontrol.client.gui.ImGuiBase;
import com.shblock.physicscontrol.client.gui.RenderHelper;
import com.shblock.physicscontrol.command.CommandAddRigidBody;
import com.shblock.physicscontrol.command.CommandSingleStep;
import com.shblock.physicscontrol.physics.physics2d.CollisionObjectUserObj2D;
import com.shblock.physicscontrol.physics.util.*;
import imgui.ImGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ColorHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.lwjgl.glfw.GLFW.*;

public class GuiPhysicsSimulator extends ImGuiBase {
    private final ItemStack item;

    private static final float MAX_SCALE = 100000F;
    private static final float MIN_SCALE = 0.1F;
    private static final int ANGLE_STEP = 6;
    private static final double PER_STEP = Math.PI / 6;

    private float globalScale = 100F;
    private float scaleSpeed = 0.05F;
    private Vector2f globalTranslate = new Vector2f(0F, 0F);

    private State state = State.NONE;
    private DrawShapes drawingShape = null;
    private Tools currentTool = Tools.DRAW_SPHERE;
    private final List<Vector2f> drawPoints = new ArrayList<>();

    private int currentGuiId = "gui".hashCode();
    private List<PcoEditGui> pcoEditGuis = new ArrayList<>();

    private double currentMouseX = 0, currentMouseY = 0;

    public GuiPhysicsSimulator(@Nullable ItemStack item) {
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

        ImGuiBuilder.buildToolSelectorUI();

        if (this.state == State.DRAW) {
            buildImGuiDrawing();
        }

        for (int i=0; i<this.pcoEditGuis.size(); i++) {
            if (!this.pcoEditGuis.get(i).buildImGui()) {
                this.pcoEditGuis.remove(i);
                i--;
            }
        }
    }

    public int getNextGuiId() {
        this.currentGuiId++;
        return this.currentGuiId;
    }

    @Override
    public void render(MatrixStack matrixStack, int combinedLight, int combinedOverlay, float particleTick) {
        super.render(matrixStack, combinedLight, combinedOverlay, particleTick);
        getSimulator().frame(particleTick);

        renderSpace(matrixStack, getSimulator().getSpace());

        drawScaleMeasure(matrixStack);
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

        if (this.state == State.DRAW) {
            renderDrawing(matrixStack);
        }

        matrixStack.popPose();
    }

    private void toSquare(Vector2f start, Vector2f end) {
        Vector2f size = end.subtract(start);
        float max = Math.max(Math.abs(size.x), Math.abs(size.y));

        if (size.x != 0F) {
            size.x /= Math.abs(size.x);
        }
        if (size.y != 0F) {
            size.y /= Math.abs(size.y);
        }

        size.multLocal(max);
        end.set(start.add(size));
    }

    private void renderDrawing(MatrixStack matrixStack) {
        matrixStack.pushPose();

        Vector2f start, end;
        Matrix4f matrix;
        switch (this.drawingShape) {
            case SPHERE:
                start = this.drawPoints.get(0).clone();
                end = this.drawPoints.get(1).clone();
                matrixStack.translate(start.x, -start.y, 0F);
                double angle = MyVector2f.angle(end.subtract(start), new Vector2f(0F, 1F));
                if (hasShiftDown()) {
                    double step = angle / PER_STEP;
                    int steps = (int) Math.round(step);
                    angle = PER_STEP * steps;
                }
                if (end.subtract(start).x < 0F) {
                    angle = -angle;
                }
                matrixStack.mulPose(
                        new net.minecraft.util.math.vector.Quaternion(0F, 0F, (float) angle, false)
                );
                matrix = matrixStack.last().pose();
                float radius = end.subtract(start).length();
                RenderHelper.drawCircleDirection(matrix, radius, 1F, 1F, 1F, 1F);
                RenderHelper.drawCircleFrame(matrix, radius, ShapeRenderer2D.SELECTED_FRAME_WIDTH, 1F, 1F, 1F, 1F);
                break;
            case BOX:
                start = this.drawPoints.get(0).clone();
                end = this.drawPoints.get(1).clone();
                if (hasShiftDown()) {
                    toSquare(start, end);
                }
                matrix = matrixStack.last().pose();
                RenderHelper.drawBoxFrame(matrix, start.x, -start.y, end.x, -end.y, ShapeRenderer2D.SELECTED_FRAME_WIDTH, 1F, 1F, 1F, 1F);
                break;
            case POLYGON:
                matrix = matrixStack.last().pose();
                List<Vector2f> vertexes = this.drawPoints.stream().map(vec -> new Vector2f(vec.x, -vec.y)).collect(Collectors.toList());
                RenderHelper.drawPolygon(matrix, vertexes, 1F, 1F, 1F, 0.3F);
                RenderHelper.drawPolygonFrame(matrix, vertexes, ShapeRenderer2D.SELECTED_FRAME_WIDTH, 1F, 1F, 1F, 1F);
                break;
        }

        matrixStack.popPose();
    }

    private void buildImGuiDrawing() {
        switch (this.drawingShape) {
            case SPHERE:
                Vector2f start = this.drawPoints.get(0).clone();
                Vector2f end = this.drawPoints.get(1).clone();
                float radius = start.subtract(end).length();
                ImGui.beginTooltip();
                ImGui.text(I18n.get("physicscontrol.gui.sim.tooltip.radius", radius));

                double angle = MyVector2f.angle(end.subtract(start), new Vector2f(0F, 1F));
                if (hasShiftDown()) {
                    double step = angle / PER_STEP;
                    int steps = (int) Math.round(step);
                    angle = PER_STEP * steps;
                }
                if (end.subtract(start).x < 0F) {
                    angle = -angle;
                }
                ImGui.text(I18n.get("physicscontrol.gui.sim.tooltip.angle", Math.toDegrees(angle)));

                ImGui.endTooltip();
                return;
            case BOX:
                start = this.drawPoints.get(0).clone();
                end = this.drawPoints.get(1).clone();
                if (hasShiftDown()) {
                    toSquare(start, end);
                }

                Vector2f offset = end.subtract(start);

                ImGui.beginTooltip();
                ImGui.text(I18n.get("physicscontrol.gui.sim.tooltip.width", Math.abs(offset.x)));
                ImGui.text(I18n.get("physicscontrol.gui.sim.tooltip.height", Math.abs(offset.y)));
                ImGui.endTooltip();
                return;
            case POLYGON:
        }
    }

    private void drawScaleMeasure(MatrixStack matrixStack) {
        matrixStack.pushPose();
        int i = -5;
        double pixel_length, space_length;
        while (true) {
            space_length = Math.pow(10, i);
            pixel_length = space_length * this.globalScale;
            if (pixel_length > 10) {
                break;
            } else {
                i++;
            }
        }

        int color = ColorHelper.PackedColor.color(255, 255, 255, 255);
        int lineH = height - 10;
        int start = (int) (width - 10 - pixel_length);
        int end = width - 10;
        hLine(matrixStack, start, end, lineH, color);
        int sideLineLength = 4;
        vLine(matrixStack, start, lineH - sideLineLength, lineH + sideLineLength, color);
        vLine(matrixStack, end, lineH - sideLineLength, lineH + sideLineLength, color);

        String text;
        if (space_length >= 1) {
            text = String.valueOf((int) space_length);
        } else {
            text = String.valueOf(space_length);
        }
        drawCenteredString(matrixStack, this.font, I18n.get("physicscontrol.gui.sim.scale_measure", text), (int) (width - 10 - pixel_length / 2), height - 21, color);

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

    private Vector2f toSpacePos(Vector2f vec) {
        return this.toSpacePos(vec.x, vec.y);
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

    private Vector2f toScreenPos(Vector2f vec) {
        return this.toScreenPos(vec.x, vec.y);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (GlobalImGuiRenderer.io.getWantCaptureMouse()) {
            return;
        }

        double deltaX = mouseX - this.currentMouseX;
        double deltaY = mouseY - this.currentMouseY;
        this.currentMouseX = mouseX;
        this.currentMouseY = mouseY;

        if (GlobalImGuiRenderer.io.getMouseDown(2)) {
            this.globalTranslate.x += deltaX;
            this.globalTranslate.y += deltaY;
        }
        if (GlobalImGuiRenderer.io.getMouseDown(0)) {
            switch (this.state) {
                case NONE:
                    if (getSimulator().isPointOnAnySelected(toSpacePos(mouseX, mouseY))) {
                        this.state = State.MOVING;
                        getSimulator().moveSelected(new Vector2f(deltaX, -deltaY).divideLocal(this.globalScale), true);
                        return;
                    }
                    return;
                case MOVING:
                    getSimulator().moveSelected(new Vector2f(deltaX, -deltaY).divideLocal(this.globalScale), false);
                    return;
                case DRAW:
                    switch (this.drawingShape) {
                        case SPHERE:
                            this.drawPoints.set(1, toSpacePos(mouseX, mouseY));
                            return;
                        case BOX:
                            this.drawPoints.set(1, toSpacePos(mouseX, mouseY));
                            return;
                        case POLYGON:
                            this.drawPoints.set(this.drawPoints.size() - 1, toSpacePos(mouseX, mouseY));
                            return;
                    }
                    return;
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        switch (button) {
            case 0:
                switch (this.state) {
                    case NONE:
                        if (getSimulator().isPointOnAnySelected(toSpacePos(mouseX, mouseY))) {
                            return false;
                        }
                        switch (this.currentTool) {
                            case DRAW_SPHERE:
                                this.state = State.DRAW;
                                this.drawingShape = DrawShapes.SPHERE;
                                this.drawPoints.clear();
                                this.drawPoints.add(toSpacePos(mouseX, mouseY));
                                this.drawPoints.add(toSpacePos(mouseX, mouseY)); // add the point twice to init both index 0 and 1
                                return true;
                            case DRAW_BOX:
                                this.state = State.DRAW;
                                this.drawingShape = DrawShapes.BOX;
                                this.drawPoints.clear();
                                this.drawPoints.add(toSpacePos(mouseX, mouseY));
                                this.drawPoints.add(toSpacePos(mouseX, mouseY)); // add the point twice to init both index 0 and 1
                                return true;
                            case DRAW_POLYGON:
                                this.state = State.DRAW;
                                this.drawingShape = DrawShapes.POLYGON;
                                this.drawPoints.clear();
                                this.drawPoints.add(toSpacePos(mouseX, mouseY));
                                this.drawPoints.add(toSpacePos(mouseX, mouseY));
                                return true;
                        }
                        return false;
                }
                return false;
            case 1:
                switch (this.state) {
                    case DRAW:
                        switch (this.drawingShape) {
                            case POLYGON:
                                Vector2f new_vertex = toSpacePos(currentMouseX, currentMouseY);
                                if (this.drawPoints.size() >= 2) {
                                    if (new_vertex.equals(this.drawPoints.get(this.drawPoints.size() - 1)) && new_vertex.equals(this.drawPoints.get(this.drawPoints.size() - 2))) {
                                        return false;
                                    }
                                }
                                this.drawPoints.add(new_vertex);
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
//        switch (button) {
//            case 2:
//                this.globalTranslate.x += deltaX;
//                this.globalTranslate.y += deltaY;
//                return true;
//            case 0:
//                switch (this.state) {
//                    case NONE:
//                        if (getSimulator().isPointOnAnySelected(toSpacePos(mouseX, mouseY))) {
//                            this.state = State.MOVING;
//                            getSimulator().moveSelected(new Vector2f(deltaX, -deltaY).divideLocal(this.globalScale), true);
//                            return true;
//                        } else {
//                            return false;
//                        }
//                    case MOVING:
//                        getSimulator().moveSelected(new Vector2f(deltaX, -deltaY).divideLocal(this.globalScale), false);
//                        return true;
//                    case DRAW:
//                        switch (this.drawingShape) {
//                            case SPHERE:
//                                this.drawPoints.set(1, toSpacePos(mouseX, mouseY));
//                                return true;
//                            case BOX:
//                                this.drawPoints.set(1, toSpacePos(mouseX, mouseY));
//                                return true;
//                            case POLYGON:
//                                this.drawPoints.set(this.drawPoints.size() - 1, toSpacePos(mouseX, mouseY));
//                                return true;
//                        }
//                        return false;
//                }
//                return false;
//        }
//        return false;
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        switch (button) {
            case 0:
                switch (this.state) {
                    case NONE:
                        List<PhysicsCollisionObject> results = getSimulator().pointTestSorted(toSpacePos(mouseX, mouseY));
                        if (results.isEmpty()) {
                            if (!hasControlDown()) {
                                getSimulator().unselectAll();
                            }
                        } else {
                            PhysicsCollisionObject top = results.get(0);

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
                    case DRAW:
                        switch (this.drawingShape) {
                            case SPHERE:
                            case BOX:
                                if (this.drawPoints.get(0).equals(this.drawPoints.get(1))) {
                                    this.state = State.NONE;
                                    this.drawingShape = null;
                                    this.drawPoints.clear();
                                    return mouseReleased(mouseX, mouseY, button);
                                }
                                break;
                            case POLYGON:
                                if (this.drawPoints.size() < 3) {
                                    this.state = State.NONE;
                                    this.drawingShape = null;
                                    this.drawPoints.clear();
                                    return mouseReleased(mouseX, mouseY, button);
                                }
                                break;
                        }
                        endShape();
                        return true;
                }
                return false;
            case 1:
                switch (this.state) {
                    case NONE:
                        List<PhysicsCollisionObject> results = getSimulator().pointTestSorted(toSpacePos(mouseX, mouseY));
                        if (results.isEmpty()) {
                            return false;
                        }
                        int pcoId = ((CollisionObjectUserObj2D) results.get(0).getUserObject()).getId();
                        getSimulator().select(results.get(0));
                        for (PcoEditGui gui : this.pcoEditGuis) {
                            if (gui.getPcoId() == pcoId) {
                                gui.reopenMainWindow();
                                return true;
                            }
                        }
                        this.pcoEditGuis.add(new PcoEditGui(pcoId));
                        return true;
                }
                return false;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (hasShiftDown()) {
            if (getSimulator().isPointOnAnySelected(toSpacePos(mouseX, mouseY))) {
                if (delta > 0) {
                    getSimulator().changeSelectedZLevel(1);
                } else {
                    getSimulator().changeSelectedZLevel(-1);
                }
            }
        } else {
            Vector2f old_pos = toSpacePos(mouseX, mouseY);
            float old_scale = this.globalScale;
            this.globalScale *= (float) (1 + this.scaleSpeed * delta);
            if (this.globalScale >= MAX_SCALE || this.globalScale <= MIN_SCALE) {
                this.globalScale = old_scale;
            } else {
                Vector2f new_pos = toSpacePos(mouseX, mouseY);
                Vector2f move = new_pos.subtract(old_pos);
                move.y = -move.y;
                move.multLocal(this.globalScale);
                this.globalTranslate.addLocal(move);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
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
            case GLFW_KEY_A:
                switch (this.state) {
                    case NONE:
                        if (hasControlDown()) {
                            getSimulator().selectAll();
                            return true;
                        }
                        return false;
                }
                return false;
            case GLFW_KEY_DELETE:
                switch (this.state) {
                    case NONE:
                        if (getSimulator().isAnySelected()) {
                            getSimulator().deleteSelected();
                            return true;
                        }
                        return false;
                }
                return false;
            case GLFW_KEY_ESCAPE:
                switch (this.state) {
                    case NONE:
                        onClose();
                    case DRAW:
                        this.state = State.NONE;
                        this.drawingShape = null;
                        this.drawPoints.clear();
                        return true;
                }
                return false;
        }
        return false;
    }

    public void endShape() {
        Vector2f start, end;
        PhysicsRigidBody body = null;
        switch (this.drawingShape) {
            case SPHERE:
                start = this.drawPoints.get(0);
                end = this.drawPoints.get(1);
                double angle = MyVector2f.angle(end.subtract(start), new Vector2f(0F, 1F));
                if (hasShiftDown()) {
                    double step = angle / PER_STEP;
                    int steps = (int) Math.round(step);
                    angle = PER_STEP * steps;
                }
                if (end.subtract(start).x < 0F) {
                    angle = -angle;
                }
                body = new PhysicsRigidBody(
                        new SphereCollisionShape(
                                start.subtract(end).length()
                        )
                );
                body.setPhysicsLocation(start.toVec3());
                body.setPhysicsRotation(new Quaternion().fromAngles(0F, 0F, (float) -angle));
                break;
            case BOX:
                start = this.drawPoints.get(0).clone();
                end = this.drawPoints.get(1).clone();
                if (hasShiftDown()) {
                    toSquare(start, end);
                }

                Vector2f offset = end.subtract(start);
                body = new PhysicsRigidBody(
                        new Box2dShape(
                                Math.abs(offset.x) / 2F,
                                Math.abs(offset.y) / 2F
                        )
                );
                body.setPhysicsLocation(start.add(offset.multLocal(0.5F)).toVec3());
                break;
            case POLYGON:
                if (drawPoints.size() < 3) {
                    break;
                }

                for (int i=0; i<this.drawPoints.size()-1; i++) { // remove repeat points
                    while (i + 1 < this.drawPoints.size() && this.drawPoints.get(i).equals(this.drawPoints.get(i + 1))) {
                        this.drawPoints.remove(i);
                    }
                }

                for (int i=1; i<this.drawPoints.size(); i++) {
                    this.drawPoints.get(i).subtractLocal(this.drawPoints.get(0));
                }
                Vector2f pos = this.drawPoints.get(0).clone();
                this.drawPoints.get(0).set(0F, 0F);

                IndexedMesh mesh = MeshHelper.create2DPolygon(this.drawPoints.toArray(new Vector2f[0]));

                if (mesh == null) {
                    break;
                }

                body = new PhysicsRigidBody(
                        new GImpactCollisionShape(
                                mesh
                        )
                );
                body.setPhysicsLocation(pos.toVec3());
                break;
        }

        if (body != null) {
            if (hasAltDown()) {
                Collection<PhysicsCollisionObject> results = getSimulator().contactTest(body);
                getSimulator().unselectAll();
                for (PhysicsCollisionObject pco : results) {
                    getSimulator().select(pco);
                }
            } else {
                getSimulator().executeCommand(
                        new CommandAddRigidBody(
                                body
                        )
                );
            }
        }

        this.state = State.NONE;
        this.drawingShape = null;
        this.drawPoints.clear();
    }

    public Tools getCurrentTool() {
        return currentTool;
    }

    public void setCurrentTool(Tools tool) {
        this.currentTool = tool;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}

enum State {
    NONE, MOVING, DRAW
}

enum DrawShapes {
    SPHERE, BOX, POLYGON
}

enum Tools {
    DRAW_SPHERE(0, 0, "physicscontrol.gui.sim.name.sphere"),
    DRAW_BOX(1, 0, "physicscontrol.gui.sim.name.box"),
    DRAW_POLYGON(2, 0, "physicscontrol.gui.sim.name.polygon");

    public float u, v;
    public String localizeName;

    Tools(int iconX, int iconY, String localizeName) {
        this.u = iconX * 0.125F;
        this.v = iconY * 0.125F;
        this.localizeName = localizeName;
    }
}
