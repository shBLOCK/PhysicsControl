package com.shblock.physicscontrol.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.StringTextComponent;

public class GuiClientHandler {
    public static void openPhysicsSimulatorGui() {
        Minecraft.getInstance().setScreen(new GuiPhysicsSimulator(new StringTextComponent("imgui-test")));
    }
}
