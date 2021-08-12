package com.shblock.physicscontrol.client.gui.PhysicsSimulator;

import com.shblock.physicscontrol.client.I18nHelper;
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

    public boolean buildImGui(ToolConfig config) {
        switch (this.tool) {
            case DRAG:
                if (this.isFirstRender) {
                    ImGui.openPopup("##tool_edit_gui");
                    this.isFirstRender = false;
                }
                if (ImGui.beginPopup("##tool_edit_gui")) {
                    //Max Force
                    ImGui.text(I18n.get(PREFIX + "drag.max_force"));
                    ImGui.sameLine();
                    ImFloat max_force = new ImFloat(config.dragToolMaxForce);
                    if (ImGui.sliderScalar("##max_force", ImGuiDataType.Float, max_force, 1E2F, 1E8F, I18nHelper.localizeNumFormat(PREFIX + "drag.max_force.num"), ImGuiSliderFlags.Logarithmic)) {
                        config.dragToolMaxForce = max_force.get();
                    }

                    //Damping Ratio
                    ImGui.text(I18n.get(PREFIX + "drag.damping"));
                    ImGui.sameLine();
                    ImFloat damping = new ImFloat(config.dragToolDampingRatio);
                    if (ImGui.sliderScalar("##damping", ImGuiDataType.Float, damping, 0F, 2F, I18nHelper.localizeNumFormat(PREFIX + "drag.damping.num"), ImGuiSliderFlags.None)) {
                        config.dragToolDampingRatio = damping.get();
                    }

                    //Disable Roatation
                    if (ImGui.checkbox(I18n.get(PREFIX + "drag.disable_rotation"), config.dragToolDisableRotation)) {
                        config.dragToolDisableRotation = !config.dragToolDisableRotation;
                    }

                    //Drag Center
                    if (ImGui.checkbox(I18n.get(PREFIX + "drag.drag_center"), config.dragToolDragCenter)) {
                        config.dragToolDragCenter = !config.dragToolDragCenter;
                    }

                    //Frequency
                    ImGui.text(I18n.get(PREFIX + "drag.frequency"));
                    ImGui.sameLine();
                    ImFloat frequency = new ImFloat(config.dragToolFrequency);
                    if (ImGui.sliderScalar("##frequency", ImGuiDataType.Float, frequency, 0.5F, 60F, I18nHelper.localizeNumFormat(PREFIX + "drag.frequency.num"), ImGuiSliderFlags.None)) {
                        config.dragToolFrequency = frequency.get();
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
