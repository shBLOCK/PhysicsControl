package com.shblock.physicscontrol.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nullable;

public class GuiClientHandler {
    public static void openPhysicsSimulatorGui(@Nullable ItemStack item) {
        Minecraft.getInstance().setScreen(new GuiPhysicsSimulator(item));
    }
}
