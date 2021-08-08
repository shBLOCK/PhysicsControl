package com.shblock.physicscontrol.client.gui.PhysicsSimulator;

import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.shblock.physicscontrol.PhysicsControl;
import com.shblock.physicscontrol.client.InteractivePhysicsSimulator2D;
import com.shblock.physicscontrol.command.CommandEditPcoProperty;
import com.shblock.physicscontrol.command.EditOperations2D;
import com.shblock.physicscontrol.physics.physics2d.CollisionObjectUserObj2D;
import imgui.ImGui;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import imgui.type.ImString;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

public class PcoEditGui {
    private static final int ICON = Minecraft.getInstance().getTextureManager().getTexture(new ResourceLocation(PhysicsControl.MODID, "icons")).getId();
    private static final int GLOBAL_INPUT_FLAG = ImGuiInputTextFlags.NoUndoRedo | ImGuiInputTextFlags.EnterReturnsTrue;

    private int guiId;
    private boolean isFirstFrame = true;
    private int pcoId; //TODO: improve performance? (don't search for the id every time)

    private static InteractivePhysicsSimulator2D getSimulator() {
        return InteractivePhysicsSimulator2D.getInstance();
    }

    private void executeOperation(EditOperations2D.EditOperationBase operation) {
        getSimulator().executeCommand(new CommandEditPcoProperty(this.pcoId, operation));
    }

    public PcoEditGui(int guiId, int pcoId) {
        this.guiId = guiId;
        this.pcoId = pcoId;
    }

    public boolean buildImGui() {
        PhysicsCollisionObject pco = InteractivePhysicsSimulator2D.getInstance().getPcoFromId(this.pcoId);
        if (pco == null) {
            return false;
        }
        CollisionObjectUserObj2D obj = (CollisionObjectUserObj2D) pco.getUserObject();

        ImBoolean pOpen = new ImBoolean(true);
        if (this.isFirstFrame) {
            ImGui.setNextWindowPos(ImGui.getMousePosX(), ImGui.getMousePosY());
            this.isFirstFrame = false;
        }
        boolean shouldBuild = ImGui.begin(I18n.get("physicscontrol.gui.sim.edit.title", obj.getName()) + "###" + guiId, pOpen, ImGuiWindowFlags.None);
        if (!pOpen.get()) {
            ImGui.end();
            return false;
        }
        ImString string = new ImString();
        if (shouldBuild) {
            //TODO: Using tree map!
            //TODO: Open a new window when shift-click the tree node!!!

            // Edit name
            ImGui.pushID("name");
            ImGui.text("name:");
            ImGui.sameLine();
            string.set(obj.getName());
            if (ImGui.inputText("", string, GLOBAL_INPUT_FLAG)) {
                executeOperation(new EditOperations2D.SetName(string.get()));
            }
            ImGui.popID();

            // Delete
            ImGui.image(ICON, 16F, 16F, 0F, 0.9375F, 0.0625F, 1F);
            ImGui.sameLine();
            if (ImGui.menuItem("Delete")) {
                getSimulator().deletePco(getSimulator().getPcoFromId(this.pcoId));
            }
        }
        ImGui.end();

        return true;
    }

    public int getPcoId() {
        return pcoId;
    }

    public void setPcoId(int pcoId) {
        this.pcoId = pcoId;
    }
}
