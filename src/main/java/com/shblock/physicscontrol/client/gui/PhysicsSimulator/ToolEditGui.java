package com.shblock.physicscontrol.client.gui.PhysicsSimulator;

import com.shblock.physicscontrol.client.I18nHelper;
import com.shblock.physicscontrol.client.gui.ImGuiImpl;
import imgui.ImDrawList;
import imgui.ImGui;
import imgui.flag.ImGuiDataType;
import imgui.flag.ImGuiSliderFlags;
import imgui.type.ImFloat;
import net.minecraft.client.resources.I18n;

public class ToolEditGui {
    private static final String PREFIX = "physicscontrol.gui.sim.tool.config.";

    private final Tools tool;

    private boolean isFirstRender = true;

    public ToolEditGui(Tools tool) {
        this.tool = tool;
    }

    private boolean startPopup() {
        if (this.isFirstRender) {
            ImGui.openPopup("##tool_edit_gui");
            this.isFirstRender = false;
        }
        return ImGui.beginPopup("##tool_edit_gui");
    }

    public boolean buildImGui(SimulatorConfig config) {
        ImDrawList drawList = ImGuiImpl.getDrawListForImpl();

        switch (this.tool) {
            case DRAG:
                if (startPopup()) {
                    // Max Force
                    ImGui.text(I18n.get(PREFIX + "drag.max_force"));
                    ImGui.sameLine();
                    ImFloat max_force = new ImFloat(config.dragToolMaxForce);
                    if (ImGuiImpl.sliderScalar(drawList, "##max_force", ImGuiDataType.Float, max_force, 1E2F, 1E8F, I18nHelper.localizeNumFormat(PREFIX + "drag.max_force.num"), ImGuiSliderFlags.Logarithmic)) {
                        config.dragToolMaxForce = max_force.get();
                    }

                    // Damping Ratio
                    ImGui.text(I18n.get(PREFIX + "drag.damping"));
                    ImGui.sameLine();
                    ImFloat damping = new ImFloat(config.dragToolDampingRatio);
                    if (ImGuiImpl.sliderScalar(drawList, "##damping", ImGuiDataType.Float, damping, 0F, 2F, I18nHelper.localizeNumFormat(PREFIX + "drag.damping.num"), ImGuiSliderFlags.None)) {
                        config.dragToolDampingRatio = damping.get();
                    }

                    // Disable Roatation
                    if (ImGuiImpl.checkbox(drawList, I18n.get(PREFIX + "drag.disable_rotation"), config.dragToolDisableRotation)) {
                        config.dragToolDisableRotation = !config.dragToolDisableRotation;
                    }

                    // Drag Center
                    if (ImGuiImpl.checkbox(drawList, I18n.get(PREFIX + "drag.drag_center"), config.dragToolDragCenter)) {
                        config.dragToolDragCenter = !config.dragToolDragCenter;
                    }

                    // Frequency
                    ImGui.text(I18n.get(PREFIX + "drag.frequency"));
                    ImGui.sameLine();
                    ImFloat frequency = new ImFloat(config.dragToolFrequency);
                    if (ImGuiImpl.sliderScalar(drawList, "##frequency", ImGuiDataType.Float, frequency, 0.5F, 60F, I18nHelper.localizeNumFormat(PREFIX + "drag.frequency.num"), ImGuiSliderFlags.None)) {
                        config.dragToolFrequency = frequency.get();
                    }

                    ImGui.endPopup();
                    return true;
                } else {
                    return false;
                }
            case GIVE_FORCE:
                if (startPopup()) {
                    // Is static force
                    if (ImGuiImpl.checkbox(drawList, I18n.get(PREFIX + "give_force.is_static"), config.giveForceIsStatic)) {
                        config.giveForceIsStatic = !config.giveForceIsStatic;
                    }

                    // Strength
                    ImGui.text(I18n.get(PREFIX + "give_force.strength"));
                    ImGui.sameLine();
                    ImFloat strength = new ImFloat(config.giveForceStrength);
                    if (ImGuiImpl.sliderScalar(drawList, "##strength", ImGuiDataType.Float, strength, 0.1F, 100F, I18nHelper.localizeNumFormat(PREFIX + "give_force.strength.num"), ImGuiSliderFlags.Logarithmic)) {
                        config.giveForceStrength = strength.get();
                    }

                    // Static force
                    ImGui.text(I18n.get(PREFIX + "give_force.static_force"));
                    ImGui.sameLine();
                    ImFloat staticForce = new ImFloat(config.giveForceStaticForce);
                    if (ImGuiImpl.sliderScalar(drawList, "##static_force", ImGuiDataType.Float, staticForce, 0.1F, 100000F, I18nHelper.localizeNumFormat(PREFIX + "give_force.static_force.num"), ImGuiSliderFlags.Logarithmic)) {
                        config.giveForceStaticForce = staticForce.get();
                    }

                    // On center
                    if (ImGuiImpl.checkbox(drawList, I18n.get(PREFIX + "give_force.on_center"), config.giveForceOnCenter)) {
                        config.giveForceOnCenter = !config.giveForceOnCenter;
                    }

                    ImGui.endPopup();
                    return true;
                } else {
                    return false;
                }
        }
        return false;
    }
}
