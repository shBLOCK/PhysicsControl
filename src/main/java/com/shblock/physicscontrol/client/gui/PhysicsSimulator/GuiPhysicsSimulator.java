package com.shblock.physicscontrol.client.gui.PhysicsSimulator;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.shblock.physicscontrol.PhysicsControl;
import com.shblock.physicscontrol.client.I18nHelper;
import com.shblock.physicscontrol.client.InteractivePhysicsSimulator2D;
import com.shblock.physicscontrol.client.gui.GlobalImGuiRenderer;
import com.shblock.physicscontrol.client.gui.ImGuiBase;
import com.shblock.physicscontrol.client.gui.RenderHelper;
import com.shblock.physicscontrol.command.*;
import com.shblock.physicscontrol.physics.physics.BodyUserObj;
import com.shblock.physicscontrol.physics.util.*;
import imgui.ImGui;
import net.minecraft.client.KeyboardListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ColorHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import org.apache.logging.log4j.Level;
import org.jbox2d.collision.AABB;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;
import org.jbox2d.dynamics.joints.MouseJoint;
import org.jbox2d.dynamics.joints.MouseJointDef;
import org.jbox2d.particle.ParticleColor;
import org.jbox2d.particle.ParticleDef;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.lwjgl.glfw.GLFW.*;

public class GuiPhysicsSimulator extends ImGuiBase implements INBTSerializable<CompoundNBT> {
    private static final float MAX_SCALE = 1E6F;
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
    private Body rotatingBody = null;
    private float bodyOrgRotation = 0F;
    private float mouseOrgRotation = 0F;
    private Body applyForceBody = null;
    private Vec2 applyForceLocalPoint = null;
    private Vec2 applyForceVec = null;

    private SimulatorConfig config = new SimulatorConfig();
    private ToolEditGui toolEditGui;
    private ParticleToolGui particleToolGui;
    private int lastParticleToolMouseButton;

    private int currentGuiId = "gui".hashCode();
    private final List<BodyEditGui> bodyEditGuis = new ArrayList<>();

    private double currentMouseX = 0, currentMouseY = 0;

    private boolean debugDraw = false;
    private int lastFrameRenderedBodyCount = 0;

    public GuiPhysicsSimulator() {
        super(new StringTextComponent("Physics Simulator"));
        new InteractivePhysicsSimulator2D(new World(new Vec2(0F, -9.8F)));

        try {
            autoLoad();
        } catch (Exception | AssertionError e) {
            PhysicsControl.log(Level.WARN, "Autoload failed!");
            e.printStackTrace();
            newSpace();
        }
    }

    public void newSpace() {
        globalScale = 100F;
        scaleSpeed = 0.05F;
        globalTranslate = new Vec2(0F, 0F);

        state = State.NONE;
        drawingShape = null;
        currentTool = Tools.DRAW_CIRCLE;
        drawPoints.clear();

        draggingJoint = null;

        config = new SimulatorConfig();
        toolEditGui = null;

        currentGuiId = "gui".hashCode();
        bodyEditGuis.clear();

        getSimulator().close();
        new InteractivePhysicsSimulator2D(new World(new Vec2(0F, -9.8F)));
    }

    public File saveToFile(String name) throws IOException {
        return SaveHelper.saveNBTFile(serializeNBT(), name);
    }

    public void loadFromFile(String name) throws IOException {
        deserializeNBT(SaveHelper.readNBTFile(name));
    }

    public File autoSave() throws IOException {
        return SaveHelper.saveNBTFile(serializeNBT(), SaveHelper.AUTOSAVE_FILENAME);
    }

    public boolean autoLoad() throws IOException {
        if (SaveHelper.contains(SaveHelper.AUTOSAVE_FILENAME)) {
            deserializeNBT(SaveHelper.readNBTFile(SaveHelper.AUTOSAVE_FILENAME));
            return true;
        }
        return false;
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
        try {
            autoSave();
        } catch (IOException e) {
            PhysicsControl.log("Autosave failed!");
            e.printStackTrace();
        }
        super.onClose();
        getSimulator().close();
    }

    @Override
    public void tick() {
        super.tick();
        getSimulator().tick();
    }

    public int getNextGuiId() {
        this.currentGuiId++;
        return this.currentGuiId;
    }

    @Override
    public void render(MatrixStack matrixStack, int combinedLight, int combinedOverlay, float particleTick) {
        super.render(matrixStack, combinedLight, combinedOverlay, particleTick);
        getSimulator().frame(particleTick);

        ParticleRender2D.render(this.width, this.height, this.globalTranslate, this.globalScale, getSimulator().getSpace(), this.config);
        renderSpace(matrixStack, getSimulator().getSpace());

        drawScaleMeasure(matrixStack);

        for (BodyEditGui gui : this.bodyEditGuis) {
            gui.render(matrixStack);
        }

        if (this.state == State.DRAW_PARTICLES) {
            int operation = -1;
            if (this.lastParticleToolMouseButton == 0) {
                operation = hasAltDown() ? ParticleToolGui.EDIT : ParticleToolGui.CREATE;
            } else if (this.lastParticleToolMouseButton == 1) {
                operation = ParticleToolGui.DELETE;
            }
            if (operation != -1) {
                this.particleToolGui.execute(this, getSimulator(), toSpacePos(currentMouseX, currentMouseY), operation, this.config);
            }
        }
    }

    private void renderSpace(MatrixStack matrixStack, World space) {
        this.debugDraw = false;

        matrixStack.pushPose();

//        matrixStack.mulPose();
        matrixStack.translate(this.globalTranslate.x, this.globalTranslate.y, 0F);
        matrixStack.scale(this.globalScale, this.globalScale, 1F);

        Vec2 lower = toSpacePos(0F, 0F);
        lower.y = -lower.y;
        Vec2 upper = toSpacePos(width, height);
        upper.y = -upper.y;
        AABB screenBB = new AABB(lower, upper);
        this.lastFrameRenderedBodyCount = 0;
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
                    aabb.lowerBound.y = -aabb.lowerBound.y;
                    aabb.upperBound.y = -aabb.upperBound.y;
                    float tmp = aabb.lowerBound.y;
                    aabb.lowerBound.y = aabb.upperBound.y;
                    aabb.upperBound.y = tmp;
                    if (debugDraw) {
                        RenderHelper.drawBoxFrame(matrixStack.last().pose(), lower.x, lower.y, upper.x, upper.y, 5, 0, 1, 0, 1);
                        RenderHelper.drawBoxFrame(matrixStack.last().pose(), aabb.lowerBound.x, aabb.lowerBound.y, aabb.upperBound.x, aabb.upperBound.y, 2, 0, 0, 1, 1);
                    }
                    if (AABBHelper.isOverlapping2D(aabb, screenBB)) {
                        ShapeRenderer2D.drawBody(matrixStack, body, getSimulator().isSelected(body));
                        this.lastFrameRenderedBodyCount++;
                    }
                }
        );

//        System.out.println(this.lastFrameRenderedBodyCount);

        switch (this.state) {
            case DRAW:
                renderDrawing(matrixStack);
                break;
            case DRAG:
                renderDrag(matrixStack);
                break;
            case ROTATE:
                renderRotating(matrixStack);
                break;
            case GIVE_FORCE:
                renderGiveForce(matrixStack);
                break;
        }

        for (BodyEditGui gui : this.bodyEditGuis) {
            gui.renderSpace(matrixStack);
        }

        if (this.currentTool == Tools.PARTICLE && this.particleToolGui != null) {
            Vec2 pos = toSpacePos(currentMouseX, currentMouseY);
            pos.y = -pos.y;
            this.particleToolGui.render(matrixStack, pos, this.config);
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
                double angle = calculateRotation(start, end);
                if (hasShiftDown()) {
                    angle = stepAngle(angle);
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

    private void renderRotating(MatrixStack matrixStack) {
        matrixStack.pushPose();

        Vec2 center = this.rotatingBody.getPosition().clone();
        Vec2 edge = toSpacePos(currentMouseX, currentMouseY);
        float radius = edge.sub(center).length();
        float angle = (float) calculateRotation(center, edge);

        matrixStack.translate(center.x, -center.y, 0F);
        matrixStack.mulPose(new Quaternion(0F, 0F, (float) (this.mouseOrgRotation + Math.PI), false));
        Matrix4f matrix = matrixStack.last().pose();
        float delta = angle - this.mouseOrgRotation;
        if (hasShiftDown()) {
            delta = (float) stepAngle(delta);
        }
        float sectorSize = (float) (delta / Math.PI / 2F);
        if (sectorSize > 0.5F) {
            sectorSize--;
        }
        if (sectorSize < -0.5F) {
            sectorSize++;
        }
        RenderHelper.drawSector(matrix, radius, -sectorSize, 1F, 1F, 1F, 0.5F);
        RenderHelper.drawLine(matrix, new Vec2(0F, 0F), new Vec2(0F, radius), 2, 0, 1, 0, 1);

//        float a = (float) (angle + Math.PI);
//        if (hasShiftDown()) {
//            a = (float) stepAngle(a);
//        }
        matrixStack.mulPose(new Quaternion(0F, 0F, (float) (sectorSize * Math.PI * 2), false));
        matrix = matrixStack.last().pose();
        RenderHelper.drawLine(matrix, new Vec2(0F, 0F), new Vec2(0F, radius), 2, 1, 0, 0, 1);
        matrixStack.popPose();
    }

    private void renderGiveForce(MatrixStack matrixStack) {
        updateGiveForce();

        matrixStack.pushPose();
        Vec2 start = this.applyForceBody.getWorldPoint(this.applyForceLocalPoint);
        start.y = -start.y;
        Matrix4f matrix = matrixStack.last().pose();

        float scale = 1 / this.globalScale / config.giveForceStrength;
        Vec2 temp = this.applyForceVec.clone();
        temp.y = -temp.y;
        RenderHelper.drawArrow(matrix, start, start.add(temp.mul(scale)), 1F, 1F, 1F, 0.8F);

        matrixStack.popPose();
    }

    private void updateGiveForce() {
        Vec2 mp = new Vec2(currentMouseX, currentMouseY);
        Vec2 mpWorld = toSpacePos(mp);
        Vec2 startPointWorldPos = this.applyForceBody.getWorldPoint(this.applyForceLocalPoint);
        Vec2 startPointScreenPos = toScreenPos(startPointWorldPos);
        this.applyForceVec = startPointScreenPos.sub(mp).mul(config.giveForceStrength);
        this.applyForceVec.x = -this.applyForceVec.x;
        if (config.giveForceIsStatic) {
            this.applyForceVec.normalize();
            this.applyForceVec.mulLocal(config.giveForceStaticForce);
        }

        if (hasShiftDown()) {
            double a = calculateRotation(new Vec2(0F, 1F), this.applyForceVec);
            a = stepAngle(a);
            float length = this.applyForceVec.length();
            this.applyForceVec = new Vec2(Math.sin(a) * length, Math.cos(a) * length);
        }
    }

    @Override
    public void buildImGui() {
        ImGui.showDemoWindow();

        ToolEditGui newGui = ImGuiBuilder.buildToolSelectorUI();
        if (this.toolEditGui == null && newGui != null) {
            this.toolEditGui = newGui;
        }

        ImGuiBuilder.buildFileUI();

        switch (this.state) {
            case DRAW:
                buildImGuiDrawing();
                break;
            case ROTATE:
                buildImGuiRotating();
                break;
            case GIVE_FORCE:
                buildImGuiGiveForce();
                break;
        }

        for (int i=0; i<this.bodyEditGuis.size(); i++) {
            if (!this.bodyEditGuis.get(i).buildImGui()) {
                this.bodyEditGuis.remove(i);
                i--;
            }
        }

        if (this.toolEditGui != null) {
            if (!this.toolEditGui.buildImGui(this.config)) {
                this.toolEditGui = null;
            }
        }

        if (this.currentTool == Tools.PARTICLE) {
            if (this.particleToolGui == null) {
                this.particleToolGui = new ParticleToolGui();
            }
            this.particleToolGui.buildImGui(this.config);
        }
    }

    private void buildImGuiDrawing() {
        switch (this.drawingShape) {
            case CIRCLE:
                Vec2 start = this.drawPoints.get(0).clone();
                Vec2 end = this.drawPoints.get(1).clone();
                float radius = start.sub(end).length();
                ImGui.beginTooltip();
                ImGui.text(String.format(I18nHelper.localizeNumFormat("physicscontrol.gui.sim.tooltip.radius"), radius));

                double angle = MyVec2.angle(end.sub(start), new Vec2(0F, 1F));
                if (hasShiftDown()) {
                    double step = angle / PER_STEP;
                    int steps = (int) Math.round(step);
                    angle = PER_STEP * steps;
                }
                if (end.sub(start).x < 0F) {
                    angle = -angle;
                }
                ImGui.text(String.format(I18nHelper.localizeNumFormat("physicscontrol.gui.sim.tooltip.angle"), Math.toDegrees(angle)));

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
                ImGui.text(String.format(I18nHelper.localizeNumFormat("physicscontrol.gui.sim.tooltip.width"), Math.abs(offset.x)));
                ImGui.text(String.format(I18nHelper.localizeNumFormat("physicscontrol.gui.sim.tooltip.height"), Math.abs(offset.y)));
                ImGui.endTooltip();
                return;
            case POLYGON:
        }
    }

    private void buildImGuiRotating() {
        Vec2 center = this.rotatingBody.getPosition().clone();
        Vec2 edge = toSpacePos(currentMouseX, currentMouseY);
        float angle = (float) calculateRotation(center, edge);
        float delta = angle - this.mouseOrgRotation;
        if (hasShiftDown()) {
            delta = (float) stepAngle(delta);
        }
        float sectorSize = (float) (delta / Math.PI / 2F);
        if (sectorSize > 0.5F) {
            sectorSize--;
        }
        if (sectorSize < -0.5F) {
            sectorSize++;
        }
        ImGui.setTooltip(String.format(I18nHelper.localizeNumFormat("physicscontrol.gui.sim.tooltip.rotate_body"), Math.abs(Math.toDegrees(sectorSize * Math.PI * 2))));
    }

    private void buildImGuiGiveForce() {
        ImGui.setTooltip(String.format(I18nHelper.localizeNumFormat("physicscontrol.gui.sim.tooltip.give_force.strength"), this.applyForceVec.length()));
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

    public void drawCenteredString(MatrixStack matrixStack, String text, int x, int y, float[] color) {
        int r = (int) (color[0] * 255F);
        int g = (int) (color[1] * 255F);
        int b = (int) (color[2] * 255F);
        int a = (int) (color[3] * 255F);
        if (r > 255) r = 255;
        if (g > 255) g = 255;
        if (b > 255) b = 255;
        if (a > 255) a = 255;
        if (r < 0) r = 0;
        if (g < 0) g = 0;
        if (b < 0) b = 0;
        if (a < 0) a = 0;
        int col = ColorHelper.PackedColor.color(a, r, g, b);
        drawCenteredString(matrixStack, this.font, text, x, y, col);
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

    public Vec2 toSpacePos(Vec2 vec) {
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

    public Vec2 toScreenPos(Vec2 vec) {
        return this.toScreenPos(vec.x, vec.y);
    }

    private double calculateRotation(Vec2 center, Vec2 edge) {
        double angle = MyVec2.angle(edge.sub(center), new Vec2(0F, 1F));
        if (edge.sub(center).x < 0F) {
            angle = -angle;
        }
        return angle;
    }

    private double stepAngle(double angle) {
        if (hasShiftDown()) {
            double step = angle / PER_STEP;
            int steps = (int) Math.round(step);
            angle = PER_STEP * steps;
        }
        return angle;
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
                    return;
                case ROTATE:
                    Vec2 center = this.rotatingBody.getPosition();
                    Vec2 edge = toSpacePos(currentMouseX, currentMouseY);
                    float currentAngle = (float) calculateRotation(center, edge);
                    float angle = currentAngle - this.mouseOrgRotation;
                    if (hasShiftDown()) {
                        angle = (float) stepAngle(angle);
                    }
                    getSimulator().executeCommand(new CommandRotateBody(this.rotatingBody, this.bodyOrgRotation - angle, false));
                    return;
                case GIVE_FORCE:
                    // Updated in updateGiveForce()
                    return;
//                case DRAW_PARTICLES:
//                    if (this.lastParticleToolMouseButton != 1) {
//                        this.particleToolGui.execute(this, getSimulator(), toSpacePos(mouseX, mouseY), this.lastParticleToolMouseButton, this.config, false);
//                    }
//                    return;
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.currentTool == Tools.PARTICLE) {
            if (button == 0 || button == 1) {
                this.state = State.DRAW_PARTICLES;
                this.lastParticleToolMouseButton = button;
//                this.particleToolGui.execute(this, getSimulator(), toSpacePos(mouseX, mouseY), this.lastParticleToolMouseButton, this.config, true);
                return true;
            }
        }

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

                                    getSimulator().executeCommand(new CommandDragBody(null));

                                    if (this.config.dragToolDragCenter) {
                                        getSimulator().setBodyPosLocal(results.get(0), pos);
                                    }

                                    MouseJointDef jointDef = new MouseJointDef();
                                    jointDef.bodyA = getSimulator().getSpace().createBody(new BodyDef());
                                    jointDef.bodyB = results.get(0);
                                    jointDef.target.set(pos);
                                    jointDef.maxForce = this.config.dragToolMaxForce;
                                    jointDef.dampingRatio = this.config.dragToolDampingRatio;
                                    jointDef.frequencyHz = this.config.dragToolFrequency;
                                    jointDef.collideConnected = true;

                                    if (this.config.dragToolDisableRotation) {
                                        results.get(0).setAngularVelocity(0F);
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
                            case ROTATE:
                                Vec2 mousePos = toSpacePos(mouseX, mouseY);
                                List<Body> objects = getSimulator().pointTestSorted(mousePos);
                                if (!objects.isEmpty()) {
                                    this.state = State.ROTATE;
                                    Body body = objects.get(0);
                                    getSimulator().freezeBody(body);

                                    this.bodyOrgRotation = body.getAngle();

                                    this.rotatingBody = body;
                                    Vec2 center = body.getPosition().clone();
                                    Vec2 edge = mousePos.clone();
                                    this.mouseOrgRotation = (float) calculateRotation(center, edge);

                                    getSimulator().executeCommand(new CommandRotateBody(body, this.bodyOrgRotation, true));
                                }
                                return false;
                            case GIVE_FORCE:
                                Vec2 mp = toSpacePos(mouseX, mouseY);
                                List<Body> objs = getSimulator().pointTestSorted(mp);
                                if (!objs.isEmpty()) {
                                    if (objs.get(0).getType() == BodyType.DYNAMIC) {
                                        this.state = State.GIVE_FORCE;
                                        this.applyForceBody = objs.get(0);
                                        this.applyForceLocalPoint = this.config.giveForceOnCenter ? new Vec2() : applyForceBody.getLocalPoint(mp);
                                        this.applyForceVec = new Vec2();
                                        return true;
                                    }
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
//        switch (this.currentTool) {
//            case PARTICLE:
//                if (button == 0 || button == 1) {
//                    Vec2 pos = toSpacePos(mouseX, mouseY);
//                    for (int i = 0; i < 1; i++) {
//                        ParticleDef pd = new ParticleDef();
//                        pd.color = button == 0 ? new ParticleColor((byte) -65, (byte) 56, (byte) 76, (byte) 127) : new ParticleColor((byte) 118, (byte) -100, (byte) -100, (byte) 127);
//                        pd.position.set(pos);
//                        getSimulator().getSpace().createParticle(pd);
//                    }
//                    return true;
//                }
//                return false;
//        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.state == State.DRAW_PARTICLES) {
            if (button == this.lastParticleToolMouseButton) {
                this.state = State.NONE;
                return true;
            }
        }

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
                        getSimulator().executeCommand(new CommandDragBody(this.draggingJoint.getBodyB()));
                        this.draggingJoint = null;
                        this.state = State.NONE;
                        return true;
                    case ROTATE:
                        getSimulator().unfreezeBody(this.rotatingBody);
                        this.rotatingBody = null;
                        this.bodyOrgRotation = 0F;
                        this.mouseOrgRotation = 0F;
                        this.state = State.NONE;
                        return true;
                    case GIVE_FORCE:
                        getSimulator().executeCommand(new CommandGiveForce(this.applyForceBody, this.applyForceLocalPoint, this.applyForceVec));
                        this.applyForceBody = null;
                        this.applyForceLocalPoint = null;
                        this.applyForceVec = null;
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
                        getSimulator().unselectAll();
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
            if (this.currentTool == Tools.PARTICLE) {
                this.config.particleToolSize += delta * 0.2F;
                if (config.particleToolSize > 10F) {
                    config.particleToolSize = 10F;
                } else if (config.particleToolSize < 0.1F) {
                    config.particleToolSize = 0.1F;
                }
            } else if (getSimulator().isPointOnAnySelected(toSpacePos(mouseX, mouseY))) {
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
        switch (keyCode) {
            case GLFW_KEY_LEFT_SHIFT:
            case GLFW_KEY_RIGHT_SHIFT:
                mouseMoved(currentMouseX, currentMouseY); // to update the drawing or rotating when the mouse didn't move
                return true;
        }
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
            case GLFW_KEY_C:
                if (hasControlDown()) {
                    if (getSimulator().isAnySelected()) {
                        getKeyboardHandler().setClipboard(getSimulator().copyBodies(getSimulator().getSelectedBodies(), toSpacePos(currentMouseX, currentMouseY)).toString());
                        return true;
                    }
                }
                return false;
            case GLFW_KEY_V:
                if (hasControlDown()) {
                    try {
                        getSimulator().pasteBodies(JsonToNBT.parseTag(getKeyboardHandler().getClipboard()), toSpacePos(currentMouseX, currentMouseY));
                        return true;
                    } catch (Exception | AssertionError e) {
                        PhysicsControl.log(Level.WARN, "Paste bodies failed!");
                        e.printStackTrace();
                    }
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
                    case GIVE_FORCE:
                        this.applyForceBody = null;
                        this.applyForceLocalPoint = null;
                        this.applyForceVec = null;
                        this.state = State.NONE;
                        return true;
                }
                return false;
            case GLFW_KEY_LEFT_SHIFT:
            case GLFW_KEY_RIGHT_SHIFT:
                mouseMoved(currentMouseX, currentMouseY); // to update the drawing or rotating when the mouse didn't move
                return true;


            case GLFW_KEY_R:
                PhysicsControl.log("reload shader");
                ParticleRender2D.loadShader();
                return true;
        }
        return false;
    }

    private KeyboardListener getKeyboardHandler() {
        return this.minecraft.keyboardHandler;
    }

    public void endShape() {
        Vec2 start, end;
        BodyDef body = null;
        Shape[] shapes = new Shape[1];
        switch (this.drawingShape) {
            case CIRCLE:
                start = this.drawPoints.get(0);
                end = this.drawPoints.get(1);
                double angle = calculateRotation(start, end);
                if (hasShiftDown()) {
                    angle = stepAngle(angle);
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

                Vec2 centroid = PolygonHelper.calculateCentroid(this.drawPoints.toArray(new Vec2[0]));
                for (Vec2 vec : this.drawPoints) {
                    vec.subLocal(centroid);
                }
                pos.addLocal(centroid);

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

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putFloat("global_scale", this.globalScale);
        nbt.putFloat("scale_speed", this.scaleSpeed);
        nbt.put("global_translate", NBTSerializer.toNBT(this.globalTranslate));
        nbt.putInt("current_tool", this.currentTool.ordinal());
        nbt.put("config", this.config.serializeNBT());

        ListNBT guiList = new ListNBT();
        for (BodyEditGui gui : this.bodyEditGuis) {
            guiList.add(gui.serializeNBT());
        }
        nbt.put("body_edit_guis", guiList);

        nbt.put("simulator", getSimulator().serializeNBT());

        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        this.globalScale = nbt.getFloat("global_scale");
        this.scaleSpeed = nbt.getFloat("scale_speed");
        this.globalTranslate = NBTSerializer.vec2FromNBT(nbt.getCompound("global_translate"));
        this.state = State.NONE;
        this.drawingShape = null;
        this.currentTool = Tools.values()[nbt.getInt("current_tool")];
        this.config = new SimulatorConfig();
        this.config.deserializeNBT(nbt.getCompound("config"));
        this.toolEditGui = null;
        this.currentGuiId = "gui".hashCode();

        this.bodyEditGuis.clear();
        ListNBT guiList = nbt.getList("body_edit_guis", Constants.NBT.TAG_COMPOUND);
        for (int i=0; i<guiList.size(); i++) {
            BodyEditGui gui = new BodyEditGui(0);
            gui.deserializeNBT(guiList.getCompound(i));
            this.bodyEditGuis.add(gui);
        }

        getSimulator().deserializeNBT(nbt.getCompound("simulator"));
    }

    public float getGlobalScale() {
        return this.globalScale;
    }
}

enum State {
    NONE, MOVING, DRAW, DRAG, ROTATE, GIVE_FORCE, DRAW_PARTICLES
}

enum DrawShapes {
    CIRCLE, BOX, POLYGON
}

enum Tools {
    DRAW_CIRCLE(0, 0, "physicscontrol.gui.sim.name.sphere", 0),
    DRAW_BOX(1, 0, "physicscontrol.gui.sim.name.box", 0),
    DRAW_POLYGON(2, 0, "physicscontrol.gui.sim.name.polygon", 0),
    PARTICLE(0, 3, "physicscontrol.gui.sim.tool.particle", 2),
    DRAG(0, 1, "physicscontrol.gui.sim.tool.drag", 3),
    ROTATE(1, 1, "physicscontrol.gui.sim.tool.rotate", 3),
    GIVE_FORCE(2, 1, "physicscontrol.gui.sim.tool.give_force", 3);

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
