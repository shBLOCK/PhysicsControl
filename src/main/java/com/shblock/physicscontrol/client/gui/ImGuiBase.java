package com.shblock.physicscontrol.client.gui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public abstract class ImGuiBase extends Screen {
//    private final GlobalImGuiRenderer guiRenderer = GlobalImGuiRenderer.getInstance();

    protected ImGuiBase(ITextComponent p_i51108_1_) {
        super(p_i51108_1_);
        GlobalImGuiRenderer.tryInitInstance();
    }

//    @Override
//    public void render(MatrixStack matrixStack, int combinedLight, int combinedOverlay, float particleTick) {
//        guiRenderer.render();
//    }

    public abstract void buildImGui();

//    @Override
//    public void onClose() {
//        guiRenderer.closeGui();
//        super.onClose();
//    }
}
