package com.shblock.physicscontrol.client.gui;

import com.shblock.physicscontrol.client.gui.PhysicsSimulator.GuiPhysicsSimulator;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

public class GuiClientHandler {
    public static void openPhysicsSimulatorGui() {
        Minecraft.getInstance().setScreen(new GuiPhysicsSimulator());
    }
}
