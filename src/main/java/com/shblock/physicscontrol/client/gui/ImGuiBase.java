package com.shblock.physicscontrol.client.gui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public abstract class ImGuiBase extends Screen {
    protected ImGuiBase(ITextComponent p_i51108_1_) {
        super(p_i51108_1_);
    }

    public abstract void buildImGui();
}
