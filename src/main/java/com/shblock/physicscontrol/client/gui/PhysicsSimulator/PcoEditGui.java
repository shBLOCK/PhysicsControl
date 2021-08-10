package com.shblock.physicscontrol.client.gui.PhysicsSimulator;

import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Vector3f;
import com.shblock.physicscontrol.PhysicsControl;
import com.shblock.physicscontrol.client.InteractivePhysicsSimulator2D;
import com.shblock.physicscontrol.client.gui.GlobalImGuiRenderer;
import com.shblock.physicscontrol.command.CommandEditPcoProperty;
import com.shblock.physicscontrol.command.EditOperations2D;
import com.shblock.physicscontrol.physics.physics2d.CollisionObjectUserObj2D;
import com.shblock.physicscontrol.physics.util.QuaternionUtil;
import com.shblock.physicscontrol.physics.util.Vector2f;
import imgui.ImColor;
import imgui.ImGui;
import imgui.extension.implot.ImPlot;
import imgui.flag.*;
import imgui.type.ImBoolean;
import imgui.type.ImDouble;
import imgui.type.ImFloat;
import imgui.type.ImString;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.LanguageMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PcoEditGui {
    private static final int ICON = Minecraft.getInstance().getTextureManager().getTexture(new ResourceLocation(PhysicsControl.MODID, "icons")).getId();
    private static final int GLOBAL_INPUT_FLAG = ImGuiInputTextFlags.NoUndoRedo | ImGuiInputTextFlags.EnterReturnsTrue | ImGuiInputTextFlags.CallbackResize;

    private boolean moveToMouse = true;
    private boolean displayMainWindow = true;
    private int pcoId; //TODO: improve performance? (don't search for the id every time)

    private List<Module> mainWindowModules = new ArrayList<>();
    private Map<Integer, Module> modules = new HashMap<>();
    private List<Integer> setToMousePosIds = new ArrayList<>();

    private static InteractivePhysicsSimulator2D getSimulator() {
        return InteractivePhysicsSimulator2D.getInstance();
    }

    private void executeOperation(EditOperations2D.EditOperationBase operation) {
        getSimulator().executeCommand(new CommandEditPcoProperty(this.pcoId, operation));
    }

    public PcoEditGui(int pcoId) {
        this.pcoId = pcoId;
        createMainWindowModules();
    }

    public boolean buildImGui() {
        PhysicsCollisionObject pco = InteractivePhysicsSimulator2D.getInstance().getPcoFromId(this.pcoId);
        if (pco == null) {
            return false;
        }
        CollisionObjectUserObj2D obj = (CollisionObjectUserObj2D) pco.getUserObject();

        if (this.displayMainWindow) {
            ImBoolean pOpen = new ImBoolean(true);
            if (this.moveToMouse) {
                ImGui.setNextWindowPos(ImGui.getMousePosX(), ImGui.getMousePosY());
                this.moveToMouse = false;
            }
            boolean shouldBuild = ImGui.begin(I18n.get("physicscontrol.gui.sim.edit.title", obj.getName()) + "###" + "edit_gui_" + pcoId, pOpen, ImGuiWindowFlags.None);
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
                            module.build(this, pco, obj);
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
                module.build(this, pco, obj);
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
        this.mainWindowModules.add(new ModulePlot());
    }

    private static abstract class Module {


        public abstract void build(PcoEditGui gui, PhysicsCollisionObject pco, CollisionObjectUserObj2D obj);

        public abstract String getId();
    }

    private static class ModuleTools extends Module {
        public ModuleTools() {}

        @Override
        public void build(PcoEditGui gui, PhysicsCollisionObject pco, CollisionObjectUserObj2D obj) {
            // Delete
            ImGui.image(ICON, 16F, 16F, 0F, 0.9375F, 0.0625F, 1F);
            ImGui.sameLine();
            if (ImGui.menuItem(I18n.get("physicscontrol.gui.sim.edit.module.tools.delete"))) {
                getSimulator().deletePco(pco);
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
        public void build(PcoEditGui gui, PhysicsCollisionObject pco, CollisionObjectUserObj2D obj) {
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
                getSimulator().changeZLevel(pco, 1);
            }
            ImGui.text(Integer.toString(obj.getZLevel()));
            if (ImGui.arrowButton("##z_down", ImGuiDir.Down)) {
                getSimulator().changeZLevel(pco, -1);
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
        public void build(PcoEditGui gui, PhysicsCollisionObject pco, CollisionObjectUserObj2D obj) {
            // Static
            ImGui.pushID("static");
            if (ImGui.checkbox(I18n.get("physicscontrol.gui.sim.edit.module.material.static"), pco.isStatic())) {
                gui.executeOperation(new EditOperations2D.SetStatic(!pco.isStatic()));
            }
            ImGui.popID();

            // Material
            ImGui.text("TODO: material selection!");

            if (!pco.isStatic()) {
                // Density
                ImGui.text(I18n.get("physicscontrol.gui.sim.edit.module.material.density"));
                ImDouble density = new ImDouble(obj.getDensity());
                if (ImGui.sliderScalar("##density", ImGuiDataType.Double, density, 0.001D, 100D, "%" + I18n.get("physicscontrol.gui.sim.edit.module.material.density.num"), ImGuiSliderFlags.Logarithmic)) {
                    if (density.get() <= 0D) {
                        density.set(0.001D);
                    }
                    gui.executeOperation(new EditOperations2D.SetDensity(density.get()));
                }

                // Mass
                ImGui.text(I18n.get("physicscontrol.gui.sim.edit.module.material.mass"));
                ImFloat mass = new ImFloat(((PhysicsRigidBody) pco).getMass());
                if (ImGui.sliderScalar("##mass", ImGuiDataType.Float, mass, 0.001F, 1000F, "%" + I18n.get("physicscontrol.gui.sim.edit.module.material.mass.num"), ImGuiSliderFlags.Logarithmic)) {
                    if (mass.get() <= 0F) {
                        mass.set(0.001F);
                    }
                    gui.executeOperation(new EditOperations2D.SetMass(mass.get()));
                }
            }

            // Friction
            ImGui.text(I18n.get("physicscontrol.gui.sim.edit.module.material.friction"));
            ImFloat friction = new ImFloat(pco.getFriction());
            if (ImGui.sliderScalar("##friction", ImGuiDataType.Float, friction, 0F, 3F, "%" + I18n.get("physicscontrol.gui.sim.edit.module.material.friction.num"), ImGuiSliderFlags.None)) {
                if (friction.get() < 0F) {
                    friction.set(0F);
                }
                gui.executeOperation(new EditOperations2D.SetFriction(friction.get()));
            }

            // Restitution
            ImGui.text(I18n.get("physicscontrol.gui.sim.edit.module.material.restitution"));
            ImFloat restitution = new ImFloat(pco.getRestitution());
            if (ImGui.sliderScalar("##restitution", ImGuiDataType.Float, restitution, 0F, 1F, "%" + I18n.get("physicscontrol.gui.sim.edit.module.material.restitution.num"), ImGuiSliderFlags.None)) {
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
        public void build(PcoEditGui gui, PhysicsCollisionObject pco, CollisionObjectUserObj2D obj) {
            String apply = I18n.get("physicscontrol.gui.sim.edit.module.movement.apply");
            String setCurrent = I18n.get("physicscontrol.gui.sim.edit.module.movement.set_current");

            if (!pco.isStatic()) {
                // Set linear velocity
                ImGui.alignTextToFramePadding();
                ImGui.text(I18n.get("physicscontrol.gui.sim.edit.module.movement.linear_velocity"));
                ImGui.pushItemWidth(200F);
                ImGui.dragFloat2("##linear_velocity", linearVelocity, 0.2F, -100F, 100F, "%" + I18n.get("physicscontrol.gui.sim.edit.module.movement.linear_velocity.num"), ImGuiSliderFlags.Logarithmic);
                if (ImGui.button(apply + "##apply_linear_velocity")) {
                    gui.executeOperation(new EditOperations2D.SetLinearVelocity(new Vector2f(this.linearVelocity[0], this.linearVelocity[1])));
                }
                ImGui.popItemWidth();

                ImGui.separator();

                // Set angular velocity
                ImGui.alignTextToFramePadding();
                ImGui.text(I18n.get("physicscontrol.gui.sim.edit.module.movement.angular_velocity"));
                ImGui.pushItemWidth(100F);
                ImGui.dragFloat("##angular_velocity", angularVelocity, 0.1F, (float) (-Math.PI * 4F), (float) (Math.PI * 4F), "%" + I18n.get("physicscontrol.gui.sim.edit.module.movement.angular_velocity.num"), ImGuiSliderFlags.Logarithmic);
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
            ImGui.inputFloat2("##position", this.position, "%" + I18n.get("physicscontrol.gui.sim.edit.module.movement.position.num"), ImGuiInputTextFlags.EnterReturnsTrue);
            ImGui.popItemWidth();
            ImGui.sameLine();
            if (ImGui.button(setCurrent+ "##set_current_position")) {
                Vector3f pos = pco.getPhysicsLocation(null);
                this.position[0] = pos.x;
                this.position[1] = pos.y;
            }
            ImGui.pushItemWidth(200F);
            if (ImGui.button(apply + "##apply_position")) {
                gui.executeOperation(new EditOperations2D.SetPos(new Vector2f(this.position[0], this.position[1])));
            }
            ImGui.popItemWidth();

            ImGui.separator();

            // Set rotation
            ImGui.alignTextToFramePadding();
            ImGui.text(I18n.get("physicscontrol.gui.sim.edit.module.movement.rotation"));
            ImGui.pushItemWidth(100F);
            ImGui.inputFloat("##rotation", this.rotation, 0.03F, 0.1F, "%" + I18n.get("physicscontrol.gui.sim.edit.module.movement.rotation.num"), ImGuiInputTextFlags.EnterReturnsTrue);
            ImGui.popItemWidth();
            ImGui.sameLine();
            if (ImGui.button( setCurrent+ "##set_current_rotation")) {
                this.rotation.set((float) QuaternionUtil.getZRadians(pco.getPhysicsRotation(null)));
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
            return String.format(I18n.get(PREFIX + key).replace('&', '%'), num);
        }

        public static String localize(String key, Vector3f num) {
            return String.format(I18n.get(PREFIX + key).replace('&', '%'), num.x, num.y);
        }

        @Override
        public void build(PcoEditGui gui, PhysicsCollisionObject pco, CollisionObjectUserObj2D obj) {
            int flags = ImGuiTableFlags.SizingFixedFit | ImGuiTableFlags.NoHostExtendX | ImGuiTableFlags.RowBg | ImGuiTableFlags.Borders | ImGuiTableFlags.NoBordersInBody;

            if (ImGui.beginTable("table", 2, flags)) {
                Vector3f value;

                // Surface area
                column(I18n.get(PREFIX + "surface_area"), localize("surface_area.num", (float) obj.getSurfaceArea()));
                if (!pco.isStatic()) {
                    // Density
                    column(I18n.get(PREFIX + "density"), localize("density.num", (float) obj.getDensity()));
                    // Mass
                    column(I18n.get(PREFIX + "mass"), localize("mass.num", ((PhysicsRigidBody) pco).getMass()));
                }
                // Position
                value = pco.getPhysicsLocation(null);
                column(I18n.get(PREFIX + "position"), localize("position.num", value));
                if (!pco.isStatic()) {
                    // Linear velocity
                    value = ((PhysicsRigidBody) pco).getLinearVelocity(null);
                    column(I18n.get(PREFIX + "linear_velocity"), localize("linear_velocity.num", value));
                }
                // Rotation
                column(I18n.get(PREFIX + "rotation"), localize("rotation.num", (float) QuaternionUtil.getZRadians(pco.getPhysicsRotation(null))));
                if (!pco.isStatic()) {
                    // Angular velocity
                    column(I18n.get(PREFIX + "angular_velocity"), localize("angular_velocity.num", ((PhysicsRigidBody) pco).getAngularVelocityLocal(null).z));
                }
                //TODO: momentum? (linear and angular)

                if (!pco.isStatic()) {
                    // Kinetic energy
                    column(I18n.get(PREFIX + "kinetic_energy"), localize("kinetic_energy.num", (float) ((PhysicsRigidBody) pco).kineticEnergy()));
                    // Mechanical Energy
                    column(I18n.get(PREFIX + "mechanical_energy"), localize("mechanical_energy.num", (float) ((PhysicsRigidBody) pco).mechanicalEnergy()));
                }

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
        public void build(PcoEditGui gui, PhysicsCollisionObject pco, CollisionObjectUserObj2D obj) {
            for (int i=0; i<16; i++) {
                int group = 1 << i;
                boolean hasGroup = (pco.getCollideWithGroups() & group) != 0;
                if (ImGui.selectable(I18n.get(PREFIX + "layer", i + 1), hasGroup, ImGuiSelectableFlags.SpanAllColumns)) {
                    int newGroup;
                    if (hasGroup) {
                        newGroup = pco.getCollideWithGroups() - group;
                    } else {
                        newGroup = pco.getCollideWithGroups() + group;
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

    private static class ModulePlot extends Module {
        public ModulePlot() {}

        @Override
        public void build(PcoEditGui gui, PhysicsCollisionObject pco, CollisionObjectUserObj2D obj) {
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

    public int getPcoId() {
        return pcoId;
    }
}
