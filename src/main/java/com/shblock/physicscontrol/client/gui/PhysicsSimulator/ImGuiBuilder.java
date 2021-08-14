package com.shblock.physicscontrol.client.gui.PhysicsSimulator;

import com.shblock.physicscontrol.PhysicsControl;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.ImVec4;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiMouseButton;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImInt;
import imgui.type.ImString;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

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

            if (tool.group != last_group) {
                if (tool != Tools.values()[0]) {
                    ImGui.separator();
                }
            }

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
            }

            last_group = tool.group;
        }

        ImGui.end();

        return newToolGui;
    }

    private static String filePromptMessage = null;
    private static String fileErrorMessage = null;

    private static final ImString saveName = new ImString();

    private static final ImInt selectedFile = new ImInt(0);

    private static String autoBackupPromptMessage = null;
    private static String autoBackupErrorMessage = null;

    private static void clearFileMessage() {
        filePromptMessage = null;
        fileErrorMessage = null;
        autoBackupPromptMessage = null;
        autoBackupErrorMessage = null;
    }

    private static void addFileMessageText() {
        if (filePromptMessage != null) {
            ImGui.textWrapped(filePromptMessage);
        }
        if (fileErrorMessage != null) {
            ImGui.textColored(1F, 0F, 0F, 1F, fileErrorMessage);
        }
    }

    private static void addAutoBackupMessageText() {
        if (autoBackupPromptMessage != null) {
            ImGui.textWrapped(autoBackupPromptMessage);
        }
        if (autoBackupErrorMessage != null) {
            ImGui.textColored(1F, 0F, 0F, 1F, autoBackupErrorMessage);
        }
    }

    private static String getAutoBackupName() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss");
        return String.format(SaveHelper.BACKUP_FILENAME, sdf.format(new Date()));
    }

    public static void buildFileUI() {
        ImGui.begin("###file", ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoScrollbar | ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.AlwaysAutoResize);

        // New
        ImGui.pushID("new_button");
        if (ImGui.imageButton(ICON, 32, 32, 0F, 0.25F, 0.125F, 0.375F)) {
            ImGui.popID();
            clearFileMessage();
            ImGui.openPopup(I18n.get("physicscontrol.gui.sim.file.new.modal"));
        } else {
            ImGui.popID();
        }
        if (ImGui.isItemHovered()) {
            ImGui.setTooltip(I18n.get("physicscontrol.gui.sim.file.new.tooltip"));
        }
        if (ImGui.beginPopupModal(I18n.get("physicscontrol.gui.sim.file.new.modal"), ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoDocking | ImGuiWindowFlags.AlwaysAutoResize)) {
            ImGui.textWrapped(I18n.get("physicscontrol.gui.sim.file.new.modal.message"));
            ImGui.separator();
            float buttonWidth = ImGui.getFontSize() * 7.0f;
            if (ImGui.button(I18n.get("physicscontrol.gui.sim.file.new.modal.yes"), buttonWidth, 0.0f)) {
                GuiPhysicsSimulator.tryGetInstance().newSpace();
                ImGui.closeCurrentPopup();
            }
            ImGui.sameLine();
            if (ImGui.button(I18n.get("physicscontrol.gui.sim.file.new.modal.no"), buttonWidth, 0.0f)) {
                ImGui.closeCurrentPopup();
            }
            ImGui.endPopup();
        }

        // Save
        ImGui.pushID("save_button");
        if (ImGui.imageButton(ICON, 32, 32, 0.125F, 0.25F, 0.25F, 0.375F)) {
            ImGui.popID();
            clearFileMessage();
            ImGui.openPopup("##save");
        } else {
            ImGui.popID();
        }
        if (ImGui.isItemHovered()) {
            ImGui.setTooltip(I18n.get("physicscontrol.gui.sim.file.save.tooltip"));
        }
        if (ImGui.beginPopup("##save")) {
            ImGui.text(I18n.get("physicscontrol.gui.sim.file.save.name"));
            ImGui.sameLine();
            ImGui.inputText("##name", saveName);
            if (ImGui.button(I18n.get("physicscontrol.gui.sim.file.save.button") + "##save")) {
                fileErrorMessage = null;
                try {
                    File file = GuiPhysicsSimulator.tryGetInstance().saveToFile(saveName.get());
                    filePromptMessage = I18n.get("physicscontrol.gui.sim.file.save.success", file.getName());
                } catch (Exception e) {
                    PhysicsControl.log(Level.WARN, "Failed to save space!");
                    e.printStackTrace();
                    filePromptMessage = null;
                    fileErrorMessage = I18n.get("physicscontrol.gui.sim.file.save.error", e);
                }
            }

            addFileMessageText();

            ImGui.endPopup();
        }

        // Load
        ImGui.pushID("load_button");
        if (ImGui.imageButton(ICON, 32, 32, 0.25F, 0.25F, 0.375F, 0.375F)) {
            ImGui.popID();
            clearFileMessage();
            ImGui.openPopup("##load");
        } else {
            ImGui.popID();
        }
        if (ImGui.isItemHovered()) {
            ImGui.setTooltip(I18n.get("physicscontrol.gui.sim.file.load.tooltip"));
        }
        if (ImGui.beginPopup("##load")) {
            String[] files = SaveHelper.getNBTFileList();
            if (selectedFile.get() >= files.length) {
                selectedFile.set(files.length - 1);
            }
            ImGui.listBox("##file_list", selectedFile, files);

            if (ImGui.button(I18n.get("physicscontrol.gui.sim.file.load.button") + "##save")) {
                fileErrorMessage = null;
                if (selectedFile.get() == -1) {
                    fileErrorMessage = I18n.get("physicscontrol.gui.sim.file.load.error.did_not_select");
                } else {
                    try {
                        autoBackupErrorMessage = null;
                        File backup = GuiPhysicsSimulator.tryGetInstance().saveToFile(getAutoBackupName());
                        autoBackupPromptMessage = I18n.get("physicscontrol.gui.sim.file.load.backup.success", backup.getName());
                    } catch (Exception e) {
                        PhysicsControl.log("Auto backup failed!");
                        e.printStackTrace();
                        autoBackupPromptMessage = null;
                        autoBackupErrorMessage = I18n.get("physicscontrol.gui.sim.file.load.backup.error", e);
                    }

                    try {
                        GuiPhysicsSimulator.tryGetInstance().loadFromFile(files[selectedFile.get()]);
                        filePromptMessage = I18n.get("physicscontrol.gui.sim.file.load.success");
                    } catch (Exception | AssertionError e) {
                        PhysicsControl.log(Level.WARN, "Failed to load space!");
                        e.printStackTrace();
                        filePromptMessage = null;
                        fileErrorMessage = I18n.get("physicscontrol.gui.sim.file.load.error", e);
                    }
                }
            }

            addFileMessageText();
            addAutoBackupMessageText();

            ImGui.endPopup();
        }

        ImGui.end();
    }
}
