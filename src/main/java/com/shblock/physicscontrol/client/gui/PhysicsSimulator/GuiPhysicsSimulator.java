package com.shblock.physicscontrol.client.gui.PhysicsSimulator;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.shblock.physicscontrol.client.I18nHelper;
import com.shblock.physicscontrol.client.InteractivePhysicsSimulator2D;
import com.shblock.physicscontrol.client.gui.GlobalImGuiRenderer;
import com.shblock.physicscontrol.client.gui.ImGuiBase;
import com.shblock.physicscontrol.client.gui.RenderHelper;
import com.shblock.physicscontrol.command.CommandAddRigidBody;
import com.shblock.physicscontrol.command.CommandSingleStep;
import com.shblock.physicscontrol.physics.physics.BodyUserObj;
import com.shblock.physicscontrol.physics.util.*;
import imgui.ImGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ColorHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.StringTextComponent;
import org.jbox2d.collision.AABB;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Settings;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;
import org.jbox2d.dynamics.joints.MouseJoint;
import org.jbox2d.dynamics.joints.MouseJointDef;

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
    private static final double PER_STEP = Math.PI / ANGLE_STEP;

    private float globalScale = 100F;
    private float scaleSpeed = 0.05F;
    private Vec2 globalTranslate = new Vec2(0F, 0F);

    private State state = State.NONE;
    private DrawShapes drawingShape = null;
    private Tools currentTool = Tools.DRAW_CIRCLE;
    private final List<Vec2> drawPoints = new ArrayList<>();

    private MouseJoint draggingJoint = null;

    private ToolConfig toolConfig = new ToolConfig();
    private ToolEditGui toolEditGui;

    private int currentGuiId = "gui".hashCode();
    private List<BodyEditGui> bodyEditGuis = new ArrayList<>();

    private double currentMouseX = 0, currentMouseY = 0;

    public GuiPhysicsSimulator(@Nullable ItemStack item) {
        super(new StringTextComponent("Physics Simulator"));
        this.item = item;
        CompoundNBT nbt = null;
        if (this.item != null) {
            nbt = item.getTagElement("space");
        }
        World space;
        if (nbt != null) {
            space = NBTSerializer.spaceFromNBT(nbt);
        } else {
            space = new World(new Vec2(0F, -9.8F));
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

        ToolEditGui newGui = ImGuiBuilder.buildToolSelectorUI();
        if (this.toolEditGui == null && newGui != null) {
            this.toolEditGui = newGui;
        }

        if (this.state == State.DRAW) {
            buildImGuiDrawing();
        }

        for (int i=0; i<this.bodyEditGuis.size(); i++) {
            if (!this.bodyEditGuis.get(i).buildImGui()) {
                this.bodyEditGuis.remove(i);
                i--;
            }
        }

        if (this.toolEditGui != null) {
            if (!this.toolEditGui.buildImGui(this.toolConfig)) {
                this.toolEditGui = null;
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

    private void renderSpace(MatrixStack matrixStack, World space) {
        matrixStack.pushPose();

//        matrixStack.mulPose();
        matrixStack.translate(this.globalTranslate.x, this.globalTranslate.y, 0F);
        matrixStack.scale(this.globalScale, this.globalScale, 1F);

        AABB screenBB = new AABB(toSpacePos(0F, 0F), toSpacePos(width, height));
        getSimulator().forEachBody(
                body -> {
                    AABB aabb = new AABB();
                    BodyHelper.forEachFixture(
                            body,
                            fixture -> {
                                if (aabb.lowerBound.x == 0 && aabb.lowerBound.y == 0 && aabb.upperBound.x == 0 && aabb.upperBound.y == 0) {
                                    aabb.set(fixture.getAABB(0));
                                } else {
                                    aabb.combine(fixture.getAABB(0));
                                }
                            }
                    );
                    if (AABB.testOverlap(screenBB, aabb) || true) {
                        ShapeRenderer2D.drawBody(matrixStack, body, getSimulator().isSelected(body));
                    }
                }
        );

        if (this.state == State.DRAW) {
            renderDrawing(matrixStack);
        }

        if (this.state == State.DRAG) {
            renderDrag(matrixStack);
        }

        matrixStack.popPose();
    }

    private void toSquare(Vec2 start, Vec2 end) {
        Vec2 size = end.sub(start);
        float max = Math.max(Math.abs(size.x), Math.abs(size.y));

        if (size.x != 0F) {
            size.x /= Math.abs(size.x);
        }
        if (size.y != 0F) {
            size.y /= Math.abs(size.y);
        }

        size.mulLocal(max);
        end.set(start.add(size));
    }

    private void renderDrawing(MatrixStack matrixStack) {
        matrixStack.pushPose();

        Vec2 start, end;
        Matrix4f matrix;
        switch (this.drawingShape) {
            case CIRCLE:
                start = this.drawPoints.get(0).clone();
                end = this.drawPoints.get(1).clone();
                matrixStack.translate(start.x, -start.y, 0F);
                double angle = MyVec2.angle(end.sub(start), new Vec2(0F, 1F));
                if (hasShiftDown()) {
                    double step = angle / PER_STEP;
                    int steps = (int) Math.round(step);
                    angle = PER_STEP * steps;
                }
                if (end.sub(start).x < 0F) {
                    angle = -angle;
                }
                matrixStack.mulPose(
                        new net.minecraft.util.math.vector.Quaternion(0F, 0F, (float) angle, false)
                );
                matrix = matrixStack.last().pose();
                float radius = end.sub(start).length();
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
                List<Vec2> vertexes = this.drawPoints.stream().map(vec -> new Vec2(vec.x, -vec.y)).collect(Collectors.toList());
                RenderHelper.drawPolygon(matrix, vertexes, 1F, 1F, 1F, 0.3F);
                RenderHelper.drawPolygonFrame(matrix, vertexes, ShapeRenderer2D.SELECTED_FRAME_WIDTH, 1F, 1F, 1F, 1F);
                break;
        }

        matrixStack.popPose();
    }

    private void renderDrag(MatrixStack matrixStack) {
        matrixStack.pushPose();
        Vec2 start = new Vec2();
        this.draggingJoint.getAnchorB(start);
        start.y = -start.y;
        Vec2 end = this.draggingJoint.getTarget().clone();
        end.y = -end.y;
        RenderHelper.drawLine(matrixStack.last().pose(), start, end, 2F, 1F, 1F, 1F, 1F);
        matrixStack.popPose();
    }

    private void buildImGuiDrawing() {
        switch (this.drawingShape) {
            case CIRCLE:
                Vec2 start = this.drawPoints.get(0).clone();
                Vec2 end = this.drawPoints.get(1).clone();
                float radius = start.sub(end).length();
                ImGui.beginTooltip();
                ImGui.text(I18n.get("physicscontrol.gui.sim.tooltip.radius", radius));

                double angle = MyVec2.angle(end.sub(start), new Vec2(0F, 1F));
                if (hasShiftDown()) {
                    double step = angle / PER_STEP;
                    int steps = (int) Math.round(step);
                    angle = PER_STEP * steps;
                }
                if (end.sub(start).x < 0F) {
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

                Vec2 offset = end.sub(start);

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

    private Vec2 toSpacePos(float x, float y) {
        return new Vec2(
                (x - this.globalTranslate.x) / this.globalScale,
                -((y - this.globalTranslate.y) / this.globalScale)
        );
    }

    private Vec2 toSpacePos(double x, double y) {
        return new Vec2(
                (x - this.globalTranslate.x) / this.globalScale,
                -((y - this.globalTranslate.y) / this.globalScale)
        );
    }

    private Vec2 toSpacePos(Vec2 vec) {
        return this.toSpacePos(vec.x, vec.y);
    }

    private Vec2 toScreenPos(float x, float y) {
        return new Vec2(
                x * this.globalScale + this.globalTranslate.x,
                -(y * this.globalScale) + this.globalTranslate.y
        );
    }

    private Vec2 toScreenPos(double x, double y) {
        return new Vec2(
                x * this.globalScale + this.globalTranslate.x,
                -(y * this.globalScale) + this.globalTranslate.y
        );
    }

    private Vec2 toScreenPos(Vec2 vec) {
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
                        getSimulator().startMove();
                        getSimulator().moveSelected(MyVec2.divideLocal(new Vec2(deltaX, -deltaY), this.globalScale), true);
                        return;
                    }
                    return;
                case MOVING:
                    getSimulator().moveSelected(MyVec2.divideLocal(new Vec2(deltaX, -deltaY), this.globalScale), false);
                    return;
                case DRAW:
                    switch (this.drawingShape) {
                        case CIRCLE:
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
                case DRAG:
                    Vec2 pos = toSpacePos(mouseX, mouseY);
                    this.draggingJoint.setTarget(pos);
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
                            if (this.currentTool != Tools.DRAG) {
                                return false;
                            }
                        }
                        switch (this.currentTool) {
                            case DRAW_CIRCLE:
                                this.state = State.DRAW;
                                this.drawingShape = DrawShapes.CIRCLE;
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
                            case DRAG:
                                Vec2 pos = toSpacePos(mouseX, mouseY);
                                List<Body> results = getSimulator().pointTestSorted(pos);
                                if (!results.isEmpty() && results.get(0).getType() == BodyType.DYNAMIC) {
                                    this.state = State.DRAG;

                                    if (this.toolConfig.dragToolDragCenter) {
                                        getSimulator().setBodyPosLocal(results.get(0), pos);
                                    }

                                    MouseJointDef jointDef = new MouseJointDef();
                                    Body tempBody = getSimulator().getSpace().createBody(new BodyDef());
                                    jointDef.bodyA = tempBody;
                                    jointDef.bodyB = results.get(0);
                                    jointDef.target.set(pos);
                                    jointDef.maxForce = this.toolConfig.dragToolMaxForce;
                                    jointDef.dampingRatio = this.toolConfig.dragToolDampingRatio;
                                    jointDef.frequencyHz = this.toolConfig.dragToolFrequency;
                                    jointDef.collideConnected = true;

                                    if (this.toolConfig.dragToolDisableRotation) {
                                        results.get(0).setFixedRotation(true);
                                    }

                                    results.get(0).setAwake(true);
                                    results.get(0).setActive(true);

                                    this.draggingJoint = (MouseJoint) getSimulator().getSpace().createJoint(jointDef);

//                                    System.out.println(getSimulator().getSpace().getJointCount());
//                                    getSimulator().getSpace().destroyBody(tempBody);
//                                    System.out.println(getSimulator().getSpace().getJointCount());

                                    return true;
                                }
                                return false;
                        }
                        return false;
                }
                return false;
            case 1:
                switch (this.state) {
                    case DRAW:
                        switch (this.drawingShape) {
                            case POLYGON:
                                Vec2 new_vertex = toSpacePos(currentMouseX, currentMouseY);
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
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        switch (button) {
            case 0:
                switch (this.state) {
                    case NONE:
                        List<Body> results = getSimulator().pointTestSorted(toSpacePos(mouseX, mouseY));
                        if (results.isEmpty()) {
                            if (!hasControlDown()) {
                                getSimulator().unselectAll();
                            }
                        } else {
                            Body top = results.get(0);

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
                        getSimulator().stopMove();
                        return true;
                    case DRAW:
                        switch (this.drawingShape) {
                            case CIRCLE:
                            case BOX:
                                if (toScreenPos(this.drawPoints.get(0)).sub(toScreenPos(this.drawPoints.get(1))).abs().length() < 3) {
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
                    case DRAG:
                        this.draggingJoint.getBodyB().setFixedRotation(false);
                        getSimulator().getSpace().destroyBody(this.draggingJoint.getBodyA());
                        this.draggingJoint = null;
                        this.state = State.NONE;
                        return true;
                }
                return false;
            case 1:
                switch (this.state) {
                    case NONE:
                        List<Body> results = getSimulator().pointTestSorted(toSpacePos(mouseX, mouseY));
                        if (results.isEmpty()) {
                            return false;
                        }
                        int pcoId = ((BodyUserObj) results.get(0).getUserData()).getId();
                        getSimulator().select(results.get(0));
                        for (BodyEditGui gui : this.bodyEditGuis) {
                            if (gui.getbodyId() == pcoId) {
                                gui.reopenMainWindow();
                                return true;
                            }
                        }
                        this.bodyEditGuis.add(new BodyEditGui(pcoId));
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
            Vec2 old_pos = toSpacePos(mouseX, mouseY);
            float old_scale = this.globalScale;
            this.globalScale *= (float) (1 + this.scaleSpeed * delta);
            if (this.globalScale >= MAX_SCALE || this.globalScale <= MIN_SCALE) {
                this.globalScale = old_scale;
            } else {
                Vec2 new_pos = toSpacePos(mouseX, mouseY);
                Vec2 move = new_pos.sub(old_pos);
                move.y = -move.y;
                move.mulLocal(this.globalScale);
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
                            steps = (int) (1 / getSimulator().getSingleStepLength());
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
        Vec2 start, end;
        BodyDef body = null;
        Shape[] shapes = new Shape[1];
        switch (this.drawingShape) {
            case CIRCLE:
                start = this.drawPoints.get(0);
                end = this.drawPoints.get(1);
                double angle = MyVec2.angle(end.sub(start), new Vec2(0F, 1F));
                if (hasShiftDown()) {
                    double step = angle / PER_STEP;
                    int steps = (int) Math.round(step);
                    angle = PER_STEP * steps;
                }
                if (end.sub(start).x < 0F) {
                    angle = -angle;
                }
                shapes[0] = new CircleShape();
                shapes[0].setRadius(start.sub(end).length());
                body = new BodyDef();
                body.setPosition(start);
                body.setAngle((float) -angle);
                break;
            case BOX:
                start = this.drawPoints.get(0).clone();
                end = this.drawPoints.get(1).clone();
                if (hasShiftDown()) {
                    toSquare(start, end);
                }

                Vec2 offset = end.sub(start);
                shapes[0] = new PolygonShape();
                ((PolygonShape) shapes[0]).setAsBox(Math.abs(offset.x / 2F), Math.abs(offset.y / 2F));
                body = new BodyDef();
                body.setPosition(start.add(offset.mulLocal(0.5F)));
                break;
            case POLYGON:
                for (int i=0; i<this.drawPoints.size()-1; i++) { // remove repeat points
                    while (i + 1 < this.drawPoints.size() && this.drawPoints.get(i).sub(this.drawPoints.get(i + 1)).length() < 0.01F) {
                        this.drawPoints.remove(i);
                    }
                }

                if (drawPoints.size() < 3) {
                    break;
                }

                for (int i=1; i<this.drawPoints.size(); i++) {
                    this.drawPoints.get(i).subLocal(this.drawPoints.get(0));
                }
                Vec2 pos = this.drawPoints.get(0).clone();
                this.drawPoints.get(0).set(0F, 0F);

                List<PolygonShape> results = ShapeHelper.buildPolygonShape(this.drawPoints);
                if (results == null) {
                    break;
                }
                shapes = results.toArray(new Shape[0]);

                body = new BodyDef();
                body.setPosition(pos);

                body.setUserData(getSimulator().getNextUserObj(I18nHelper.getCollisionShapeName(shapes[0])));
                ((BodyUserObj) body.userData).setPolygonVertexCache(this.drawPoints.toArray(new Vec2[0]));
                break;
        }

        if (body != null) {
            body.setType(BodyType.DYNAMIC);
            if (hasAltDown()) {
                Body tempBody = getSimulator().getSpace().createBody(body);
                for (Shape shape : shapes) {
                    tempBody.createFixture(shape, 1F);
                }
                Collection<Body> results = getSimulator().contactTest(tempBody);
                getSimulator().getSpace().destroyBody(tempBody);

                getSimulator().unselectAll();
                for (Body b : results) {
                    getSimulator().select(b);
                }
            } else {
                getSimulator().executeCommand(
                        new CommandAddRigidBody(body, shapes)
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
    NONE, MOVING, DRAW, DRAG
}

enum DrawShapes {
    CIRCLE, BOX, POLYGON
}

enum Tools {
    DRAW_CIRCLE(0, 0, "physicscontrol.gui.sim.name.sphere", 0),
    DRAW_BOX(1, 0, "physicscontrol.gui.sim.name.box", 0),
    DRAW_POLYGON(2, 0, "physicscontrol.gui.sim.name.polygon", 0),
    DRAG(0, 1, "physicscontrol.gui.sim.tool.drag", 1);

    public float u, v;
    public String localizeName;
    public int group;

    Tools(int iconX, int iconY, String localizeName, int group) {
        this.u = iconX * 0.125F;
        this.v = iconY * 0.125F;
        this.localizeName = localizeName;
        this.group = group;
    }
}
