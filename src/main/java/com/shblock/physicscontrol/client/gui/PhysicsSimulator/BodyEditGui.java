package com.shblock.physicscontrol.client.gui.PhysicsSimulator;

import com.shblock.physicscontrol.PhysicsControl;
import com.shblock.physicscontrol.client.I18nHelper;
import com.shblock.physicscontrol.client.InteractivePhysicsSimulator2D;
import com.shblock.physicscontrol.client.gui.GlobalImGuiRenderer;
import com.shblock.physicscontrol.command.CommandEditBodyProperty;
import com.shblock.physicscontrol.command.EditOperations2D;
import com.shblock.physicscontrol.physics.physics.BodyUserObj;
import com.shblock.physicscontrol.physics.util.NBTSerializer;
import com.shblock.physicscontrol.physics.util.ShapeHelper;
import imgui.ImColor;
import imgui.ImGui;
import imgui.extension.implot.ImPlot;
import imgui.flag.*;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import imgui.type.ImString;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;
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
            boolean shouldBuild = ImGui.begin(I18n.get("physicscontrol.gui.sim.edit.title", obj.getName()) + "###" + "edit_gui_" + bodyId, pOpen, ImGuiWindowFlags.None);
            if (pOpen.get()) {
                if (shouldBuild) {
                    for (Module module : this.mainWindowModules) {
                        boolean shouldBuildModule = ImGui.collapsingHeader(I18n.get("physicscontrol.gui.sim.edit.module." + module.getId()) + "###" + module.getId());
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
                            ImGui.separator();
                        }
                    }
                }
            } else {
                this.displayMainWindow = false;
            }

            ImGui.end();
        }

        List<Integer> toRemove = new ArrayList<>();
        for (int windowId : this.modules.keySet()) {
            if (this.setToMousePosIds.contains(windowId)) {
                ImGui.setNextWindowPos(ImGui.getMousePosX(), ImGui.getMousePosY());
                this.setToMousePosIds.remove(new Integer(windowId));
            }
            Module module = this.modules.get(windowId);
            ImBoolean pOpen = new ImBoolean(true);
            boolean shouldRender = ImGui.begin(I18n.get("physicscontrol.gui.sim.edit.module." + module.getId() + "_window", obj.getName()) + "###" + windowId, pOpen);
            if (!pOpen.get()) {
                toRemove.add(windowId);
                ImGui.end();
                continue;
            }

            if (shouldRender) {
                module.build(this, body, obj);
            }

            ImGui.end();
        }
        for (int id : toRemove) {
            this.modules.remove(id);
        }

        return true;
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
    }

    public int getbodyId() {
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
                getSimulator().deleteBodyLocal(body);
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
            // Name
            ImGui.pushID("name");
            ImGui.alignTextToFramePadding();
            ImGui.text(I18n.get("physicscontrol.gui.sim.edit.module.appearance.name"));
            ImGui.sameLine();
            ImString string = new ImString(obj.getName());
            if (ImGui.inputText("", string, GLOBAL_INPUT_FLAG)) {
                gui.executeOperation(new EditOperations2D.SetName(string.get()));
            }
            ImGui.popID();

            ImGui.separator();

            // Color
            ImGui.pushID("color");
            float[] color = new float[]{obj.getFloatR(), obj.getFloatG(), obj.getFloatB(), obj.getFloatAlpha()};
            if (ImGui.colorPicker4(I18n.get("physicscontrol.gui.sim.edit.module.appearance.color"), color, ImGuiColorEditFlags.AlphaBar | ImGuiColorEditFlags.AlphaPreviewHalf | ImGuiColorEditFlags.PickerHueBar | ImGuiColorEditFlags.Uint8)) {
                gui.executeOperation(new EditOperations2D.SetColor(color[0], color[1], color[2], color[3]));
            }
            ImGui.popID();

            ImGui.separator();

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
            boolean isStatic = body.getType() == BodyType.STATIC;

            // Static
            ImGui.pushID("static");
            if (ImGui.checkbox(I18n.get("physicscontrol.gui.sim.edit.module.material.static"), isStatic)) {
                gui.executeOperation(new EditOperations2D.SetStatic(!isStatic));
                isStatic = !isStatic;
            }
            ImGui.popID();

            // Material
            ImGui.text("TODO: material selection!");

            if (!isStatic) {
                // Density
                ImGui.text(I18n.get("physicscontrol.gui.sim.edit.module.material.density"));
                ImFloat density = new ImFloat(body.getFixtureList().getDensity());
                if (ImGui.sliderScalar("##density", ImGuiDataType.Float, density, 0.001F, 100F, I18nHelper.localizeNumFormat("physicscontrol.gui.sim.edit.module.material.density.num"), ImGuiSliderFlags.Logarithmic)) {
                    if (density.get() <= 0F) {
                        density.set(0.001F);
                    }
                    gui.executeOperation(new EditOperations2D.SetDensity(density.get()));
                }

                // Mass
                ImGui.text(I18n.get("physicscontrol.gui.sim.edit.module.material.mass"));
                ImFloat mass = new ImFloat(body.getMass());
                if (ImGui.sliderScalar("##mass", ImGuiDataType.Float, mass, 0.001F, 1000F, I18nHelper.localizeNumFormat("physicscontrol.gui.sim.edit.module.material.mass.num"), ImGuiSliderFlags.Logarithmic)) {
                    if (mass.get() <= 0F) {
                        mass.set(0.001F);
                    }
                    gui.executeOperation(new EditOperations2D.SetMass(mass.get()));
                }
            }

            // Friction
            ImGui.text(I18n.get("physicscontrol.gui.sim.edit.module.material.friction"));
            ImFloat friction = new ImFloat(body.getFixtureList().getFriction());
            if (ImGui.sliderScalar("##friction", ImGuiDataType.Float, friction, 0F, 3F, I18nHelper.localizeNumFormat("physicscontrol.gui.sim.edit.module.material.friction.num"), ImGuiSliderFlags.None)) {
                if (friction.get() < 0F) {
                    friction.set(0F);
                }
                gui.executeOperation(new EditOperations2D.SetFriction(friction.get()));
            }

            // Restitution
            ImGui.text(I18n.get("physicscontrol.gui.sim.edit.module.material.restitution"));
            ImFloat restitution = new ImFloat(body.getFixtureList().getRestitution());
            if (ImGui.sliderScalar("##restitution", ImGuiDataType.Float, restitution, 0F, 1F, I18nHelper.localizeNumFormat("physicscontrol.gui.sim.edit.module.material.restitution.num"), ImGuiSliderFlags.None)) {
                if (restitution.get() < 0F) {
                    restitution.set(0F);
                }
                gui.executeOperation(new EditOperations2D.SetRestitution(restitution.get()));
            }
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
            boolean isStatic = body.getType() == BodyType.STATIC;

            String apply = I18n.get("physicscontrol.gui.sim.edit.module.movement.apply");
            String setCurrent = I18n.get("physicscontrol.gui.sim.edit.module.movement.set_current");

            if (!isStatic) {
                // Set linear velocity
                ImGui.alignTextToFramePadding();
                ImGui.text(I18n.get("physicscontrol.gui.sim.edit.module.movement.linear_velocity"));
                ImGui.pushItemWidth(200F);
                ImGui.dragFloat2("##linear_velocity", linearVelocity, 0.2F, -100F, 100F, I18nHelper.localizeNumFormat("physicscontrol.gui.sim.edit.module.movement.linear_velocity.num"), ImGuiSliderFlags.Logarithmic);
                if (ImGui.button(apply + "##apply_linear_velocity")) {
                    gui.executeOperation(new EditOperations2D.SetLinearVelocity(new Vec2(this.linearVelocity[0], this.linearVelocity[1])));
                }
                ImGui.popItemWidth();

                ImGui.separator();

                // Set angular velocity
                ImGui.alignTextToFramePadding();
                ImGui.text(I18n.get("physicscontrol.gui.sim.edit.module.movement.angular_velocity"));
                ImGui.pushItemWidth(100F);
                ImGui.dragFloat("##angular_velocity", angularVelocity, 0.1F, (float) (-Math.PI * 4F), (float) (Math.PI * 4F), I18nHelper.localizeNumFormat("physicscontrol.gui.sim.edit.module.movement.angular_velocity.num"), ImGuiSliderFlags.Logarithmic);
                if (ImGui.button(apply + "##apply_angular_velocity")) {
                    gui.executeOperation(new EditOperations2D.SetAngularVelocity(angularVelocity[0]));
                }
                ImGui.popItemWidth();

                ImGui.separator();
            }

            // Set pos
            ImGui.alignTextToFramePadding();
            ImGui.text(I18n.get("physicscontrol.gui.sim.edit.module.movement.position"));
            ImGui.pushItemWidth(200F);
            ImGui.inputFloat2("##position", this.position, I18nHelper.localizeNumFormat("physicscontrol.gui.sim.edit.module.movement.position.num"), ImGuiInputTextFlags.EnterReturnsTrue);
            ImGui.popItemWidth();
            ImGui.sameLine();
            if (ImGui.button(setCurrent+ "##set_current_position")) {
                Vec2 pos = body.getPosition();
                this.position[0] = pos.x;
                this.position[1] = pos.y;
            }
            ImGui.pushItemWidth(200F);
            if (ImGui.button(apply + "##apply_position")) {
                gui.executeOperation(new EditOperations2D.SetPos(new Vec2(this.position[0], this.position[1])));
            }
            ImGui.popItemWidth();

            ImGui.separator();

            // Set rotation
            ImGui.alignTextToFramePadding();
            ImGui.text(I18n.get("physicscontrol.gui.sim.edit.module.movement.rotation"));
            ImGui.pushItemWidth(100F);
            ImGui.inputFloat("##rotation", this.rotation, 0.03F, 0.1F, I18nHelper.localizeNumFormat("physicscontrol.gui.sim.edit.module.movement.rotation.num"), ImGuiInputTextFlags.EnterReturnsTrue);
            ImGui.popItemWidth();
            ImGui.sameLine();
            if (ImGui.button( setCurrent+ "##set_current_rotation")) {
                this.rotation.set(body.getAngle());
            }
            ImGui.pushItemWidth(100F);
            if (ImGui.button(apply + "##apply_rotation")) {
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
            int mask = body.getFixtureList().getFilterData().maskBits;
            for (int i=0; i<16; i++) {
                int group = 1 << i;
                boolean hasGroup = (mask & group) != 0;
                if (ImGui.selectable(I18n.get(PREFIX + "layer", i + 1), hasGroup, ImGuiSelectableFlags.SpanAllColumns)) {
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
            if (ImGui.button(I18n.get(PREFIX + "select_all") + "##select_all")) {
                gui.executeOperation(new EditOperations2D.SetCollisionGroup(0b1111111111111111));
            }
            ImGui.popStyleColor(3);

            ImGui.sameLine();

            ImGui.pushStyleColor(ImGuiCol.Button, ImColor.hslToColor(0F, 0.6F, 0.6F));
            ImGui.pushStyleColor(ImGuiCol.ButtonHovered, ImColor.hslToColor(0F, 0.7F, 0.7F));
            ImGui.pushStyleColor(ImGuiCol.ButtonActive, ImColor.hslToColor(0F, 0.8F, 0.8F));
            if (ImGui.button(I18n.get(PREFIX + "unselect_all") + "##unselect_all")) {
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
        private static final int FLAGS = ImGuiTableFlags.SizingFixedFit | ImGuiTableFlags.NoHostExtendX | ImGuiTableFlags.RowBg | ImGuiTableFlags.Borders | ImGuiTableFlags.NoBordersInBody;

        private Vec2 origin = null;

        public ModuleMoveDistance() {}

        public static void column(String a, String b) {
            ImGui.tableNextRow();
            ImGui.tableSetColumnIndex(0);
            ImGui.text(a);
            ImGui.tableSetColumnIndex(1);
            ImGui.text(b);
        }

        @Override
        public void build(BodyEditGui gui, Body body, BodyUserObj obj) {
            Vec2 pos = body.getPosition();

            if (this.origin == null) {
                this.origin = pos.clone();
            }

            String numFormat = I18nHelper.localizeNumFormat("physicscontrol.gui.sim.edit.module.move_distance.num");
            if (ImGui.beginTable("move_distance_table", 2, FLAGS)) {
                column(I18n.get("physicscontrol.gui.sim.edit.module.move_distance.distance"), String.format(numFormat, pos.sub(this.origin).abs().length()));
                column(I18n.get("physicscontrol.gui.sim.edit.module.move_distance.distance_x"), String.format(numFormat, Math.abs(pos.x - this.origin.x)));
                column(I18n.get("physicscontrol.gui.sim.edit.module.move_distance.distance_y"), String.format(numFormat, Math.abs(pos.y - this.origin.y)));
                ImGui.endTable();
            }

            if (ImGui.button(I18n.get("physicscontrol.gui.sim.edit.module.move_distance.reset"))) {
                this.origin = pos.clone();
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
            return nbt;
        }

        @Override
        public void deserializeNBT(CompoundNBT nbt) {
            super.deserializeNBT(nbt);
            this.origin = NBTSerializer.vec2FromNBT(nbt.getCompound("origin"));
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
}
