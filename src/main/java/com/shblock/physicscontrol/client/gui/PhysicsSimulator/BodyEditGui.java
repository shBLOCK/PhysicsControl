package com.shblock.physicscontrol.client.gui.PhysicsSimulator;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.shblock.physicscontrol.Config;
import com.shblock.physicscontrol.PhysicsControl;
import com.shblock.physicscontrol.client.I18nHelper;
import com.shblock.physicscontrol.client.InteractivePhysicsSimulator2D;
import com.shblock.physicscontrol.client.gui.GlobalImGuiRenderer;
import com.shblock.physicscontrol.client.gui.ImGuiImpl;
import com.shblock.physicscontrol.client.gui.RenderHelper;
import com.shblock.physicscontrol.command.CommandBodyToElastic;
import com.shblock.physicscontrol.command.CommandDeleteBodies;
import com.shblock.physicscontrol.command.CommandEditBodyProperty;
import com.shblock.physicscontrol.command.EditOperations2D;
import com.shblock.physicscontrol.motionsensor.MotionSensorHandler;
import com.shblock.physicscontrol.physics.material.Material;
import com.shblock.physicscontrol.physics.user_obj.BodyUserObj;
import com.shblock.physicscontrol.physics.util.NBTSerializer;
import com.shblock.physicscontrol.physics.util.ShapeHelper;
import imgui.*;
import imgui.extension.implot.ImPlot;
import imgui.flag.*;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import imgui.type.ImString;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraftforge.common.util.INBTSerializable;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BodyEditGui implements INBTSerializable<CompoundNBT> {
    private static final Map<String, Class<? extends Module>> moduleRegistry = new HashMap<>();

    private static final int ICON = Minecraft.getInstance().getTextureManager().getTexture(new ResourceLocation(PhysicsControl.MODID, "icons")).getId();
    private static final int GLOBAL_INPUT_FLAG = ImGuiInputTextFlags.NoUndoRedo | ImGuiInputTextFlags.EnterReturnsTrue | ImGuiInputTextFlags.CallbackResize;

    private boolean moveToMouse = true;
    private boolean displayMainWindow = true;
    private int bodyId;
//    private boolean isFirstFrame = true;

    private List<Module> mainWindowModules = new ArrayList<>();
    private Map<Integer, Module> modules = new HashMap<>();
    private List<Integer> setToMousePosIds = new ArrayList<>();

    private static InteractivePhysicsSimulator2D getSimulator() {
        return InteractivePhysicsSimulator2D.getInstance();
    }

    private void executeOperation(EditOperations2D.EditOperationBase operation) {
        getSimulator().executeCommand(new CommandEditBodyProperty(this.bodyId, operation));
    }

    public BodyEditGui(int bodyId) {
        this.bodyId = bodyId;
        createMainWindowModules();
    }

    public boolean buildImGui() {
        ImDrawList drawList = ImGuiImpl.getDrawListForImpl();

        Body body = InteractivePhysicsSimulator2D.getInstance().getBodyFromId(this.bodyId);
        if (body == null) {
            return false;
        }
        BodyUserObj obj = (BodyUserObj) body.getUserData();

        if (this.displayMainWindow) {
            ImBoolean pOpen = new ImBoolean(true);
            if (this.moveToMouse) {
                ImGui.setNextWindowPos(ImGui.getMousePosX(), ImGui.getMousePosY());
                this.moveToMouse = false;
            }
            boolean shouldBuild = ImGuiImpl.beginWithBg(I18n.get("physicscontrol.gui.sim.edit.title", obj.getName()) + "###" + "edit_gui_" + bodyId, pOpen, ImGuiWindowFlags.None);
            if (pOpen.get()) {
                if (shouldBuild) {
                    for (Module module : this.mainWindowModules) {
                        boolean shouldBuildModule = ImGuiImpl.collapsingHeader(drawList, I18n.get("physicscontrol.gui.sim.edit.module." + module.getId()) + "###" + module.getId());
//                        if (isFirstFrame)
//                            ImGuiImpl.setNextWindowCollapsed();
//                        boolean shouldBuildModule = ImGuiImpl.beginChildWithBg(I18n.get("physicscontrol.gui.sim.edit.module." + module.getId()) + "###" + module.getId(), ImGui.getWindowWidth(), 64);
                        if (ImGui.isItemClicked() && GlobalImGuiRenderer.io.getKeyShift()) {
                            try {
                                addModule(module.getClass().newInstance());
                            } catch (InstantiationException | IllegalAccessException e) {
                                e.printStackTrace();
                                assert false : e;
                            }
                        }
                        if (shouldBuildModule) {
                            module.build(this, body, obj);
                            if (InteractivePhysicsSimulator2D.getInstance().getBodyFromId(this.bodyId) == null) { // happens when body has been deleted in the gui
                                ImGuiImpl.endWithBg();
                                return false;
                            }
                            ImGuiImpl.separator(drawList);
                        }
                    }
                }
            } else {
                this.displayMainWindow = false;
            }

            ImGuiImpl.endWithBg();
        }

        List<Integer> toRemove = new ArrayList<>();
        for (int windowId : this.modules.keySet()) {
            if (this.setToMousePosIds.contains(windowId)) {
                ImGui.setNextWindowPos(ImGui.getMousePosX(), ImGui.getMousePosY());
                this.setToMousePosIds.remove(new Integer(windowId));
            }
            Module module = this.modules.get(windowId);
            ImBoolean pOpen = new ImBoolean(true);
            boolean shouldRender = ImGuiImpl.beginWithBg(I18n.get("physicscontrol.gui.sim.edit.module." + module.getId() + "_window", obj.getName()) + "###" + windowId, pOpen);
            if (!pOpen.get()) {
                toRemove.add(windowId);
                ImGuiImpl.endWithBg();
                continue;
            }

            if (shouldRender) {
                module.build(this, body, obj);
            }

            ImGuiImpl.endWithBg();
        }
        for (int id : toRemove) {
            this.modules.remove(id);
        }

        return true;
    }

    public void render(MatrixStack matrixStack) {
        Body body = InteractivePhysicsSimulator2D.getInstance().getBodyFromId(this.bodyId);
        if (body == null) {
            return;
        }
        BodyUserObj obj = (BodyUserObj) body.getUserData();
        this.mainWindowModules.forEach(module -> module.render(matrixStack, body, obj));
        this.modules.values().forEach(module -> module.render(matrixStack, body, obj));
    }

    public void renderSpace(MatrixStack matrixStack) {
        Body body = InteractivePhysicsSimulator2D.getInstance().getBodyFromId(this.bodyId);
        if (body == null) {
            return;
        }
        BodyUserObj obj = (BodyUserObj) body.getUserData();
        this.mainWindowModules.forEach(module -> module.renderSpace(matrixStack, body, obj));
        this.modules.values().forEach(module -> module.renderSpace(matrixStack, body, obj));
    }

    public void reopenMainWindow() {
        this.displayMainWindow = true;
        this.moveToMouse = true;
    }

    private void addModule(Module module) {
        int id = GuiPhysicsSimulator.tryGetInstance().getNextGuiId();
        this.modules.put(id, module);
        this.setToMousePosIds.add(id);
    }

    public void createMainWindowModules() {
        this.mainWindowModules.add(new ModuleTools());
        this.mainWindowModules.add(new ModuleAppearance());
        this.mainWindowModules.add(new ModuleMaterial());
        this.mainWindowModules.add(new ModuleMovement());
        this.mainWindowModules.add(new ModuleInformation());
        this.mainWindowModules.add(new ModuleCollision());
        this.mainWindowModules.add(new ModuleMoveDistance());
        this.mainWindowModules.add(new ModulePlot());
        this.mainWindowModules.add(new ModuleMotionSensor());
    }

    public int getBodyId() {
        return bodyId;
    }

    public static void register(Class<? extends Module> clz) {
        try {
            moduleRegistry.put(clz.newInstance().getId(), clz);
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            assert false;
        }
    }

    private static Module moduleFromId(String id) {
        try {
            return moduleRegistry.get(id).newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            assert false;
        }
        return null;
    }

    public static void init() {
        register(ModuleTools.class);
        register(ModuleAppearance.class);
        register(ModuleMaterial.class);
        register(ModuleMovement.class);
        register(ModuleInformation.class);
        register(ModuleCollision.class);
        register(ModuleMoveDistance.class);
        register(ModulePlot.class);
        register(ModuleMotionSensor.class);
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putBoolean("display_main_window", this.displayMainWindow);
        nbt.putInt("body_id", this.bodyId);
        ListNBT list = new ListNBT();
        for (Module module : this.modules.values()) {
            CompoundNBT mn = module.serializeNBT();
            mn.putString("type", module.getId());
            list.add(mn);
        }
        nbt.put("modules", list);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        this.displayMainWindow = nbt.getBoolean("display_main_window");
        this.bodyId = nbt.getInt("body_id");
        this.modules.clear();
//        FIXME: fix the crash bug and then re enable this part
//        ListNBT list = nbt.getList("modules", Constants.NBT.TAG_COMPOUND);
//        for (int i=0; i<list.size(); i++) {
//            int guiId = GuiPhysicsSimulator.tryGetInstance().getNextGuiId();
//            CompoundNBT mn = list.getCompound(i);
//            Module module = moduleFromId(mn.getString("type"));
//            module.deserializeNBT(mn);
//            this.modules.put(guiId, module);
//        }
    }

    private static abstract class Module implements INBTSerializable<CompoundNBT> {
        public abstract void build(BodyEditGui gui, Body body, BodyUserObj obj);

        public void render(MatrixStack matrixStack, Body body, BodyUserObj obj) {}

        public void renderSpace(MatrixStack matrixStack, Body body, BodyUserObj obj) {}

        public abstract String getId();

        @Override
        public CompoundNBT serializeNBT() {
            return new CompoundNBT();
        }

        @Override
        public void deserializeNBT(CompoundNBT nbt) { }
    }

    private static class ModuleTools extends Module {
        public ModuleTools() {}

        @Override
        public void build(BodyEditGui gui, Body body, BodyUserObj obj) {
            // Delete
            ImGui.image(ICON, 16F, 16F, 0F, 0.9375F, 0.0625F, 1F);
            ImGui.sameLine();
            if (ImGui.menuItem(I18n.get("physicscontrol.gui.sim.edit.module.tools.delete"))) {
                getSimulator().executeCommand(new CommandDeleteBodies(Lists.newArrayList(body)));
            }

            // To Elastic
            if (ImGui.menuItem(I18n.get("physicscontrol.gui.sim.edit.module.tools.to_elastic"))) {
                getSimulator().executeCommand(new CommandBodyToElastic(body));
            }
        }

        @Override
        public String getId() {
            return "tools";
        }
    }

    private static class ModuleAppearance extends Module {
        public ModuleAppearance() {}

        @Override
        public void build(BodyEditGui gui, Body body, BodyUserObj obj) {
            ImDrawList drawList = ImGuiImpl.getDrawListForImpl();

            // Name
            ImGui.pushID("name");
            ImGui.alignTextToFramePadding();
            ImGui.text(I18n.get("physicscontrol.gui.sim.edit.module.appearance.name"));
            ImGui.sameLine();
            ImString string = new ImString(obj.getName());
            if (ImGuiImpl.inputText(drawList, "", string, GLOBAL_INPUT_FLAG)) {
                gui.executeOperation(new EditOperations2D.SetName(string.get()));
            }
            ImGui.popID();

            ImGuiImpl.separator(drawList);

            // Color
            ImGui.pushID("color");
            float[] color = new float[]{obj.getFloatR(), obj.getFloatG(), obj.getFloatB(), obj.getFloatAlpha()};
            if (ImGui.colorPicker4(I18n.get("physicscontrol.gui.sim.edit.module.appearance.color"), color, ImGuiColorEditFlags.AlphaBar | ImGuiColorEditFlags.AlphaPreviewHalf | ImGuiColorEditFlags.PickerHueBar | ImGuiColorEditFlags.Uint8)) {
                gui.executeOperation(new EditOperations2D.SetColor(color[0], color[1], color[2], color[3]));
            }
            ImGui.popID();

            ImGuiImpl.separator(drawList);

            // Z-Level
            ImGui.text(I18n.get("physicscontrol.gui.sim.edit.module.appearance.z_level"));
            if (ImGui.arrowButton("##z_up", ImGuiDir.Up)) {
                getSimulator().changeZLevel(body, 1);
            }
            ImGui.text(Integer.toString(obj.getZLevel()));
            if (ImGui.arrowButton("##z_down", ImGuiDir.Down)) {
                getSimulator().changeZLevel(body, -1);
            }
        }

        @Override
        public String getId() {
            return "appearance";
        }
    }

    private static class ModuleMaterial extends Module {
        public ModuleMaterial() {}

        @Override
        public void build(BodyEditGui gui, Body body, BodyUserObj obj) {
            ImDrawList drawList = ImGuiImpl.getDrawListForImpl();

            boolean isStatic = body.getType() == BodyType.STATIC;

            // Material
            buildMaterialSelector(gui, body, obj);

            ImGuiImpl.separator(drawList);

            // Static
            ImGui.pushID("static");
            if (ImGuiImpl.checkbox(drawList, I18n.get("physicscontrol.gui.sim.edit.module.material.static"), isStatic)) {
                gui.executeOperation(new EditOperations2D.SetStatic(!isStatic));
                isStatic = !isStatic;
            }
            ImGui.popID();

            if (!isStatic) {
                // Density
                ImGui.text(I18n.get("physicscontrol.gui.sim.edit.module.material.density"));
                ImFloat density = new ImFloat(body.getFixtureList().getDensity());
                if (ImGuiImpl.sliderScalar(drawList, "##density", ImGuiDataType.Float, density, 0.001F, 100F, I18nHelper.localizeNumFormat("physicscontrol.gui.sim.edit.module.material.density.num"), ImGuiSliderFlags.Logarithmic)) {
                    if (density.get() <= 0F) {
                        density.set(0.001F);
                    }
                    gui.executeOperation(new EditOperations2D.SetDensity(density.get()));
                }

                // Mass
                ImGui.text(I18n.get("physicscontrol.gui.sim.edit.module.material.mass"));
                ImFloat mass = new ImFloat(body.getMass());
                if (ImGuiImpl.sliderScalar(drawList, "##mass", ImGuiDataType.Float, mass, 0.001F, 1000F, I18nHelper.localizeNumFormat("physicscontrol.gui.sim.edit.module.material.mass.num"), ImGuiSliderFlags.Logarithmic)) {
                    if (mass.get() <= 0F) {
                        mass.set(0.001F);
                    }
                    gui.executeOperation(new EditOperations2D.SetMass(mass.get()));
                }

                ImGuiImpl.separator(drawList);
            }

            // Friction
            ImGui.text(I18n.get("physicscontrol.gui.sim.edit.module.material.friction"));
            ImFloat friction = new ImFloat(body.getFixtureList().getFriction());
            if (ImGuiImpl.sliderScalar(drawList, "##friction", ImGuiDataType.Float, friction, 0F, 3F, I18nHelper.localizeNumFormat("physicscontrol.gui.sim.edit.module.material.friction.num"), ImGuiSliderFlags.None)) {
                if (friction.get() < 0F) {
                    friction.set(0F);
                }
                gui.executeOperation(new EditOperations2D.SetFriction(friction.get()));
            }

            // Restitution
            ImGui.text(I18n.get("physicscontrol.gui.sim.edit.module.material.restitution"));
            ImFloat restitution = new ImFloat(body.getFixtureList().getRestitution());
            if (ImGuiImpl.sliderScalar(drawList, "##restitution", ImGuiDataType.Float, restitution, 0F, 1F, I18nHelper.localizeNumFormat("physicscontrol.gui.sim.edit.module.material.restitution.num"), ImGuiSliderFlags.None)) {
                if (restitution.get() < 0F) {
                    restitution.set(0F);
                }
                gui.executeOperation(new EditOperations2D.SetRestitution(restitution.get()));
            }
        }

        private void buildMaterialSelector(BodyEditGui gui, Body body, BodyUserObj obj) {
            ImDrawList drawList = ImGuiImpl.getDrawListForImpl();

            ImVec2 windowPos = new ImVec2();
            ImGui.getWindowPos(windowPos);
            ImVec2 windowSize = new ImVec2();
            ImGui.getWindowSize(windowSize);
            ImVec2 itemSpacing = new ImVec2();
            ImGui.getStyle().getItemSpacing(itemSpacing);
            float windowX2 = windowPos.x + windowSize.x;

            for (Map.Entry<ResourceLocation, Material> entry : Config.materials.entrySet()) {
                Material material = entry.getValue();
                ResourceLocation id = entry.getKey();

                TextureManager manager = Minecraft.getInstance().getTextureManager();
                Texture texture = manager.getTexture(material.texture);
                if (texture == null) {
                    manager.bind(material.texture);
                    texture = manager.getTexture(material.texture);
                }
                int image = texture.getId();

                boolean selected = false;
                if (obj.getMaterial() != null) {
                    if (obj.getMaterial().getId().equals(material.getId())) {
                        selected = true;
                    }
                }
                ImGui.pushID("material_button:" + id.toString());
//                if (selected) {
//                    ImVec4 color = ImGuiBuilder.SELECTED_COLOR;
//                    ImGui.pushStyleColor(ImGuiCol.Button, color.x, color.y, color.z, color.w);
//                    if (ImGuiImpl.imageButton(drawList, image, 32, 32, 0F, 0F, 1F, 1F, 5, color)) {
//                        gui.executeOperation(new EditOperations2D.SetMaterial(null));
//                        selected = false;
//                    }
//                    ImGui.popStyleColor(1);
//                } else {
//                    if (ImGuiImpl.imageButton(drawList, image, 32, 32, 0F, 0F, 1F, 1F, 5)) {
//                        gui.executeOperation(new EditOperations2D.SetMaterial(material));
//                        selected = false;
//                    }
//                }
                if (ImGuiImpl.selector(drawList, selected, image, 32, 32, 0F, 0F, 1F, 1F, 10)) {
                    ImGuiImpl.playClickSound();
                    gui.executeOperation(new EditOperations2D.SetMaterial(selected ? null : material));
                    selected = false;
                }
                ImGui.popID();

                if (ImGui.isItemHovered()) {
                    ImGui.beginTooltip();
                    ImGui.text(I18n.get(material.getLocalizeName()));
                    ImGui.endTooltip();

                    if (ImGui.isMouseClicked(ImGuiMouseButton.Right)) {
                        //TODO: show material property here
                    }
                }

                ImVec2 lastButtonPos = new ImVec2();
                ImGui.getItemRectMax(lastButtonPos);
                float lastButtonX2 = lastButtonPos.x;
                float nextButtonX2 = lastButtonX2 + itemSpacing.x + 32F;
                if (nextButtonX2 < windowX2) {
                    ImGui.sameLine();
                }
            }

            ImGui.newLine();
        }

        @Override
        public String getId() {
            return "material";
        }
    }

    private static class ModuleMovement extends Module {
        private final float[] linearVelocity = new float[]{0F, 0F};
        private final float[] angularVelocity = new float[]{0F};
        private final float[] position = new float[]{0F, 0F};
        private final ImFloat rotation = new ImFloat(0F);

        public ModuleMovement() {}

        @Override
        public void build(BodyEditGui gui, Body body, BodyUserObj obj) {
            ImDrawList drawList = ImGuiImpl.getDrawListForImpl();
            boolean isStatic = body.getType() == BodyType.STATIC;

            String apply = I18n.get("physicscontrol.gui.sim.edit.module.movement.apply");
            String setCurrent = I18n.get("physicscontrol.gui.sim.edit.module.movement.set_current");
            //TODO: implement StopMovement!!!
            if (!isStatic) {
                // Set linear velocity
                ImGui.alignTextToFramePadding();
                ImGui.text(I18n.get("physicscontrol.gui.sim.edit.module.movement.linear_velocity"));
                ImGui.pushItemWidth(200F);
                ImGuiImpl.dragFloat2(drawList, "##linear_velocity", linearVelocity, 0.2F, -100F, 100F, I18nHelper.localizeNumFormat("physicscontrol.gui.sim.edit.module.movement.linear_velocity.num"), ImGuiSliderFlags.Logarithmic);
                if (ImGuiImpl.button(drawList, apply + "##apply_linear_velocity")) {
                    gui.executeOperation(new EditOperations2D.SetLinearVelocity(new Vec2(this.linearVelocity[0], this.linearVelocity[1])));
                }
                ImGui.popItemWidth();

                ImGuiImpl.separator(drawList);

                // Set angular velocity
                ImGui.alignTextToFramePadding();
                ImGui.text(I18n.get("physicscontrol.gui.sim.edit.module.movement.angular_velocity"));
                ImGui.pushItemWidth(100F);
                ImGuiImpl.dragFloat(drawList, "##angular_velocity", angularVelocity, 0.1F, (float) (-Math.PI * 4F), (float) (Math.PI * 4F), I18nHelper.localizeNumFormat("physicscontrol.gui.sim.edit.module.movement.angular_velocity.num"), ImGuiSliderFlags.Logarithmic);
                if (ImGuiImpl.button(drawList, apply + "##apply_angular_velocity")) {
                    gui.executeOperation(new EditOperations2D.SetAngularVelocity(angularVelocity[0]));
                }
                ImGui.popItemWidth();

                ImGuiImpl.separator(drawList);
            }

            if (ImGuiImpl.button(drawList, I18n.get("physicscontrol.gui.sim.edit.module.movement.stop_movement"))) {
                gui.executeOperation(new EditOperations2D.StopMovement());
            }

            ImGuiImpl.separator(drawList);

            // Set pos
            ImGui.alignTextToFramePadding();
            ImGui.text(I18n.get("physicscontrol.gui.sim.edit.module.movement.position"));
            ImGui.pushItemWidth(200F);
            ImGuiImpl.inputFloat2(drawList, "##position", this.position, I18nHelper.localizeNumFormat("physicscontrol.gui.sim.edit.module.movement.position.num"), ImGuiInputTextFlags.EnterReturnsTrue);
            ImGui.popItemWidth();
            ImGui.sameLine();
            if (ImGuiImpl.button(drawList, setCurrent+ "##set_current_position")) {
                Vec2 pos = body.getPosition();
                this.position[0] = pos.x;
                this.position[1] = pos.y;
            }
            ImGui.pushItemWidth(200F);
            if (ImGuiImpl.button(drawList, apply + "##apply_position")) {
                gui.executeOperation(new EditOperations2D.SetPos(new Vec2(this.position[0], this.position[1])));
            }
            ImGui.popItemWidth();

            ImGuiImpl.separator(drawList);

            // Set rotation
            ImGui.alignTextToFramePadding();
            ImGui.text(I18n.get("physicscontrol.gui.sim.edit.module.movement.rotation"));
            ImGui.pushItemWidth(100F);
            ImGuiImpl.inputFloat(drawList, "##rotation", this.rotation, 0.03F, 0.1F, I18nHelper.localizeNumFormat("physicscontrol.gui.sim.edit.module.movement.rotation.num"), ImGuiInputTextFlags.EnterReturnsTrue);
            ImGui.popItemWidth();
            ImGui.sameLine();
            if (ImGuiImpl.button(drawList,  setCurrent+ "##set_current_rotation")) {
                this.rotation.set(body.getAngle());
            }
            ImGui.pushItemWidth(100F);
            if (ImGuiImpl.button(drawList, apply + "##apply_rotation")) {
                gui.executeOperation(new EditOperations2D.SetRotation(this.rotation.get()));
            }
            ImGui.popItemWidth();
        }

        @Override
        public String getId() {
            return "movement";
        }
    }

    private static class ModuleInformation extends Module {
        private static final int FLAGS = ImGuiTableFlags.SizingFixedFit | ImGuiTableFlags.NoHostExtendX | ImGuiTableFlags.RowBg | ImGuiTableFlags.Borders | ImGuiTableFlags.NoBordersInBody;
        private static final String PREFIX = "physicscontrol.gui.sim.edit.module.information.";

        public ModuleInformation() {}

        public static void column(String a, String b) {
            ImGui.tableNextRow();
            ImGui.tableSetColumnIndex(0);
            ImGui.text(a);
            ImGui.tableSetColumnIndex(1);
            ImGui.text(b);
        }

        public static String localize(String key, float num) {
            return String.format(I18nHelper.localizeNumFormat(PREFIX + key), num);
        }

        public static String localize(String key, Vec2 num) {
            return String.format(I18nHelper.localizeNumFormat(PREFIX + key), num.x, num.y);
        }

        @Override
        public void build(BodyEditGui gui, Body body, BodyUserObj obj) {
            boolean isStatic = body.getType() == BodyType.STATIC;

            if (ImGui.beginTable("information_table", 2, FLAGS)) {
                Vec2 value;

                // Surface area
                column(I18n.get(PREFIX + "surface_area"), localize("surface_area.num", (float) ShapeHelper.getSurfaceArea2D(body.getFixtureList().getShape())));
                if (!isStatic) {
                    // Density
                    column(I18n.get(PREFIX + "density"), localize("density.num", body.getFixtureList().getDensity()));
                    // Mass
                    column(I18n.get(PREFIX + "mass"), localize("mass.num", body.getMass()));
                }
                // Position
                value = body.getPosition();
                column(I18n.get(PREFIX + "position"), localize("position.num", value));
                if (!isStatic) {
                    // Linear velocity
                    value = body.getLinearVelocity();
                    column(I18n.get(PREFIX + "linear_velocity"), localize("linear_velocity.num", value));
                }
                // Rotation
                column(I18n.get(PREFIX + "rotation"), localize("rotation.num", body.getAngle()));
                if (!isStatic) {
                    // Angular velocity
                    column(I18n.get(PREFIX + "angular_velocity"), localize("angular_velocity.num", body.getAngularVelocity()));
                }

                //TODO: display all kinds of energy
//                if (isStatic) {
//                    // Kinetic energy
//                    column(I18n.get(PREFIX + "kinetic_energy"), localize("kinetic_energy.num", (float) ((PhysicsRigidBody) body).kineticEnergy()));
//                    // Mechanical Energy
//                    column(I18n.get(PREFIX + "mechanical_energy"), localize("mechanical_energy.num", (float) ((PhysicsRigidBody) body).mechanicalEnergy()));
//                }

                ImGui.endTable();
            }
        }

        @Override
        public String getId() {
            return "information";
        }
    }

    private static class ModuleCollision extends Module {
        private static final String PREFIX = "physicscontrol.gui.sim.edit.module.collision.";

        public ModuleCollision() {}

        @Override
        public void build(BodyEditGui gui, Body body, BodyUserObj obj) {
            ImDrawList drawList = ImGuiImpl.getDrawListForImpl();
            int mask = body.getFixtureList().getFilterData().maskBits;
            for (int i=0; i<16; i++) {
                int group = 1 << i;
                boolean hasGroup = (mask & group) != 0;
                if (ImGuiImpl.selectable(drawList, I18n.get(PREFIX + "layer", i + 1), hasGroup, ImGuiSelectableFlags.SpanAllColumns)) {
                    int newGroup;
                    if (hasGroup) {
                        newGroup = mask - group;
                    } else {
                        newGroup = mask + group;
                    }
                    gui.executeOperation(new EditOperations2D.SetCollisionGroup(newGroup));
                }
            }

            ImGui.pushStyleColor(ImGuiCol.Button, ImColor.hslToColor(0.33F, 0.6F, 0.6F));
            ImGui.pushStyleColor(ImGuiCol.ButtonHovered, ImColor.hslToColor(0.33F, 0.7F, 0.7F));
            ImGui.pushStyleColor(ImGuiCol.ButtonActive, ImColor.hslToColor(0.33F, 0.8F, 0.8F));
            if (ImGuiImpl.button(drawList, I18n.get(PREFIX + "select_all") + "##select_all")) {
                gui.executeOperation(new EditOperations2D.SetCollisionGroup(0b1111111111111111));
            }
            ImGui.popStyleColor(3);

            ImGui.sameLine();

            ImGui.pushStyleColor(ImGuiCol.Button, ImColor.hslToColor(0F, 0.6F, 0.6F));
            ImGui.pushStyleColor(ImGuiCol.ButtonHovered, ImColor.hslToColor(0F, 0.7F, 0.7F));
            ImGui.pushStyleColor(ImGuiCol.ButtonActive, ImColor.hslToColor(0F, 0.8F, 0.8F));
            if (ImGuiImpl.button(drawList, I18n.get(PREFIX + "unselect_all") + "##unselect_all")) {
                gui.executeOperation(new EditOperations2D.SetCollisionGroup(0));
            }
            ImGui.popStyleColor(3);
        }


        @Override
        public String getId() {
            return "collision";
        }
    }

    private static class ModuleMoveDistance extends Module {
        private static final int TABLE_FLAGS = ImGuiTableFlags.SizingFixedFit | ImGuiTableFlags.NoHostExtendX | ImGuiTableFlags.RowBg | ImGuiTableFlags.Borders;
        private static final int COLOR_FLAGS = ImGuiColorEditFlags.AlphaPreviewHalf | ImGuiColorEditFlags.NoInputs | ImGuiColorEditFlags.NoLabel | ImGuiColorEditFlags.AlphaBar;

        private Vec2 origin = null;
        private Vec2 bodyPos = null;
        private float len, lenX, lenY;
        private boolean drawLen, drawLenX, drawLenY;
        private float[] colLen = new float[]{0F, 1F, 0F, 1F};
        private float[] colLenX = new float[]{1F, 0F, 0F, 1F};
        private float[] colLenY = new float[]{0F, 0F, 1F, 1F};

        public ModuleMoveDistance() {}

        private static void header() {
            ImGui.tableSetupColumn(I18n.get("physicscontrol.gui.sim.edit.module.move_distance.header.type"));
            ImGui.tableSetupColumn(I18n.get("physicscontrol.gui.sim.edit.module.move_distance.header.data"));
            ImGui.tableSetupColumn(I18n.get("physicscontrol.gui.sim.edit.module.move_distance.header.render"));
            ImGui.tableHeadersRow();
        }

        private static void column(String a, String b) {
            ImGui.tableNextRow();
            ImGui.tableSetColumnIndex(0);
            ImGui.text(a);
            ImGui.tableSetColumnIndex(1);
            ImGui.text(b);
        }

        private enum ColumnType {
            LEN, LEN_X, LEN_Y
        }

        private void buildRenderOptions(ColumnType type) {
            ImDrawList drawList = ImGuiImpl.getDrawListForImpl();

            ImGui.tableSetColumnIndex(2);
            switch (type) {
                case LEN:
                    if (ImGuiImpl.checkbox(drawList, "##len_check", this.drawLen))
                        this.drawLen = !this.drawLen;
                    ImGui.sameLine();
                    ImGui.colorEdit4("##len_col", this.colLen, COLOR_FLAGS);
                    break;
                case LEN_X:
                    if (ImGuiImpl.checkbox(drawList, "##lenx_check", this.drawLenX))
                        this.drawLenX = !this.drawLenX;
                    ImGui.sameLine();
                    ImGui.colorEdit4("##lenx_col", this.colLenX, COLOR_FLAGS);
                    break;
                case LEN_Y:
                    if (ImGuiImpl.checkbox(drawList, "##leny_check", this.drawLenY))
                        this.drawLenY = !this.drawLenY;
                    ImGui.sameLine();
                    ImGui.colorEdit4("##leny_col", this.colLenY, COLOR_FLAGS);
                    break;
            }
        }

        @Override
        public void build(BodyEditGui gui, Body body, BodyUserObj obj) {
            ImDrawList drawList = ImGuiImpl.getDrawListForImpl();

            String numFormat = I18nHelper.localizeNumFormat("physicscontrol.gui.sim.edit.module.move_distance.num");
            if (ImGui.beginTable("move_distance_table", 3, TABLE_FLAGS)) {
                header();
                column(I18n.get("physicscontrol.gui.sim.edit.module.move_distance.distance"), String.format(numFormat, this.len));
                buildRenderOptions(ColumnType.LEN);
                column(I18n.get("physicscontrol.gui.sim.edit.module.move_distance.distance_x"), String.format(numFormat, this.lenX));
                buildRenderOptions(ColumnType.LEN_X);
                column(I18n.get("physicscontrol.gui.sim.edit.module.move_distance.distance_y"), String.format(numFormat, this.lenY));
                buildRenderOptions(ColumnType.LEN_Y);
                ImGui.endTable();
            }

            if (ImGuiImpl.button(drawList, I18n.get("physicscontrol.gui.sim.edit.module.move_distance.reset"))) {
                this.origin = this.bodyPos.clone();
            }
        }

        private boolean haveDataForRender() {
            return this.origin != null && this.bodyPos != null;
        }

        private void drawLengthText(MatrixStack matrixStack, Vec2 start, Vec2 end, float len, float[] color) {
            matrixStack.pushPose();

            GuiPhysicsSimulator gui = GuiPhysicsSimulator.tryGetInstance();
            String numFormat = I18nHelper.localizeNumFormat("physicscontrol.gui.sim.edit.module.move_distance.num");
            Vec2 a = gui.toScreenPos(start);
            Vec2 b = gui.toScreenPos(end);
            Vec2 mid = a.add(b.sub(a).mul(0.5F));

            matrixStack.translate(mid.x, mid.y, 0F);
            matrixStack.scale(0.75F, 0.75F, 0.75F);
            gui.drawCenteredString(matrixStack, String.format(numFormat, len), 0, 0, color);

            matrixStack.popPose();
        }

        @Override
        public void render(MatrixStack matrixStack, Body body, BodyUserObj obj) {
            if (drawLen) {
                drawLengthText(matrixStack, this.origin, this.bodyPos, this.len, this.colLen);
            }
            if (drawLenX) {
                drawLengthText(matrixStack, this.origin, alignToX(), this.lenX, this.colLenX);
            }
            if (drawLenY) {
                drawLengthText(matrixStack, this.origin, alignToY(), this.lenY, this.colLenY);
            }
        }

        private void drawLine(Matrix4f matrix, Vec2 start, Vec2 end, float[] color) {
            RenderHelper.drawLine(matrix, start, end, 2F, color[0], color[1], color[2], color[3]);
        }

        private void drawTwoSidedArrow(Matrix4f matrix, Vec2 start, Vec2 end, float[] color) {
            RenderHelper.drawArrow(matrix, start, end, color[0], color[1], color[2], color[3]);
            RenderHelper.drawArrow(matrix, end, start, color[0], color[1], color[2], color[3]);
        }

        private Vec2 flipY(Vec2 vec) {
            return new Vec2(vec.x, -vec.y);
        }

        private Vec2 alignToX() {
            return new Vec2(this.bodyPos.x, this.origin.y);
        }

        private Vec2 alignToY() {
            return new Vec2(this.origin.x, this.bodyPos.y);
        }

        @Override
        public void renderSpace(MatrixStack matrixStack, Body body, BodyUserObj obj) {
            Vec2 pos = body.getPosition();

            if (this.origin == null) {
                this.origin = pos.clone();
            }
            this.bodyPos = body.getPosition();
            this.len = pos.sub(this.origin).abs().length();
            this.lenX = Math.abs(pos.x - this.origin.x);
            this.lenY = Math.abs(pos.y - this.origin.y);

            if (haveDataForRender()) {
                Matrix4f matrix = matrixStack.last().pose();

                if (drawLen) {
                    drawTwoSidedArrow(matrix, flipY(this.origin), flipY(this.bodyPos), this.colLen);
                }
                if (drawLenX) {
                    Vec2 xPos = alignToX();
                    drawTwoSidedArrow(matrix, flipY(this.origin), flipY(xPos), this.colLenX);
                    if (xPos.y > this.bodyPos.y) {
                        xPos.y += this.lenY * 0.05F;
                    } else {
                        xPos.y -= this.lenY * 0.05F;
                    }
                    drawLine(matrix, flipY(this.bodyPos), flipY(xPos), this.colLenX);
                }
                if (drawLenY) {
                    Vec2 yPos = alignToY();
                    drawTwoSidedArrow(matrix, flipY(this.origin), flipY(yPos), this.colLenY);
                    if (yPos.x > this.bodyPos.x) {
                        yPos.x += this.lenX * 0.05F;
                    } else {
                        yPos.x -= this.lenX * 0.05F;
                    }
                    drawLine(matrix, flipY(this.bodyPos), flipY(yPos), this.colLenY);
                }
            }
        }

        @Override
        public String getId() {
            return "move_distance";
        }

        @Override
        public CompoundNBT serializeNBT() {
            CompoundNBT nbt = super.serializeNBT();
            nbt.put("origin", NBTSerializer.toNBT(this.origin));
            nbt.putBoolean("draw_len", this.drawLen);
            nbt.putBoolean("draw_len_x", this.drawLenX);
            nbt.putBoolean("draw_len_y", this.drawLenY);
            nbt.put("col_len", NBTSerializer.toNBT(this.colLen));
            nbt.put("col_len_x", NBTSerializer.toNBT(this.colLenX));
            nbt.put("col_len_y", NBTSerializer.toNBT(this.colLenY));
            return nbt;
        }

        @Override
        public void deserializeNBT(CompoundNBT nbt) {
            super.deserializeNBT(nbt);
            this.origin = NBTSerializer.vec2FromNBT(nbt.getCompound("origin"));
            this.drawLen = nbt.getBoolean("draw_len");
            this.drawLenX = nbt.getBoolean("draw_len_x");
            this.drawLenY = nbt.getBoolean("draw_len_y");
            this.colLen = NBTSerializer.floatArrayFromNBT(nbt.get("col_len"));
            this.colLenX = NBTSerializer.floatArrayFromNBT(nbt.get("col_len_x"));
            this.colLenY = NBTSerializer.floatArrayFromNBT(nbt.get("col_len_y"));
        }
    }

    private static class ModulePlot extends Module {
        public ModulePlot() {}

        @Override
        public void build(BodyEditGui gui, Body body, BodyUserObj obj) {
            if (ImPlot.beginPlot("test")) {
                ImPlot.plotLine("a", new Double[]{1D, 2D, 0.5D}, new Double[]{2D, 0.5D, 1D});
                ImPlot.endPlot();
            }
        }

        @Override
        public String getId() {
            return "plot";
        }
    }

    private static class ModuleMotionSensor extends Module {
        public ModuleMotionSensor() {}

        @Override
        public void build(BodyEditGui gui, Body body, BodyUserObj obj) {
            boolean hadSensor = obj.getMotionSensor() != null;
            if (ImGui.button(hadSensor ? "unbind" : "bind")) {
                if (hadSensor) {
                    MotionSensorHandler.removeSensorInstance(obj.getMotionSensor().deviceId);
                    body.setGravityScale(1);
                }
                obj.setMotionSensor(hadSensor ? null : MotionSensorHandler.addSensorInstance("WT5300003667"));
                body.setGravityScale(0);
            }
        }

        @Override
        public String getId() {
            return "motion_sensor";
        }
    }
}
