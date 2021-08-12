package com.shblock.physicscontrol.client.gui.PhysicsSimulator;

import com.shblock.physicscontrol.PhysicsControl;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.ImVec4;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiMouseButton;
import imgui.flag.ImGuiWindowFlags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

// Just because the Gui class is becoming too long
class ImGuiBuilder {
    private static final int ICON = Minecraft.getInstance().getTextureManager().getTexture(new ResourceLocation(PhysicsControl.MODID, "icons")).getId();

    private static GuiPhysicsSimulator getGui() {
        return GuiPhysicsSimulator.tryGetInstance();
    }

    protected static ToolEditGui buildToolSelectorUI() {
        ToolEditGui newToolGui = null;

        ImGui.begin("ToolBar", ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoScrollbar | ImGuiWindowFlags.NoCollapse);
        ImVec2 windowPos = new ImVec2();
        ImGui.getWindowPos(windowPos);
        ImVec2 windowSize = new ImVec2();
        ImGui.getWindowSize(windowSize);
        ImVec2 itemSpacing = new ImVec2();
        ImGui.getStyle().getItemSpacing(itemSpacing);
        float windowX2 = windowPos.x + windowSize.x;
        int last_group = 0;
        for (Tools tool : Tools.values()) {
            int i = tool.ordinal();

            ImGui.pushID(i);
            if (getGui().getCurrentTool() == tool) {
                ImVec4 color = ImGui.getStyle().getColor(ImGuiCol.ButtonActive);
                ImGui.imageButton(ICON, 32, 32, tool.u, tool.v, tool.u + 0.125F, tool.v + 0.125F, 0, color.x, color.y, color.z, color.w);
            } else {
                if (ImGui.imageButton(ICON, 32, 32, tool.u, tool.v, tool.u + 0.125F, tool.v + 0.125F, 0)) {
                    getGui().setCurrentTool(tool);
                }
            }
            ImGui.popID();

            if (ImGui.isItemHovered()) {
                ImGui.beginTooltip();
                ImGui.text(I18n.get("physicscontrol.gui.sim.tooltip.tool_base", I18n.get(tool.localizeName)));
                ImGui.endTooltip();

                if (ImGui.isMouseClicked(ImGuiMouseButton.Right)) {
                    newToolGui = new ToolEditGui(tool);
                }
            }

            if (tool.group == last_group) {
                ImVec2 lastButtonPos = new ImVec2();
                ImGui.getItemRectMax(lastButtonPos);
                float lastButtonX2 = lastButtonPos.x;
                float nextButtonX2 = lastButtonX2 + itemSpacing.x + 32F;
                if (i + 1 < Tools.values().length && nextButtonX2 < windowX2) {
                    ImGui.sameLine();
                }
            } else {
                ImGui.separator();
            }
        }

        ImGui.end();

        return newToolGui;
    }
}
