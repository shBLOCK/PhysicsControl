package com.shblock.physicscontrol.proxy;

import com.shblock.physicscontrol.PhysicsControl;
import com.shblock.physicscontrol.client.gui.PhysicsSimulator.GuiPhysicsSimulator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = PhysicsControl.MODID)
public class ClientProxy {
    private static void registerTexture(String name, String path) {
        Minecraft.getInstance().getTextureManager().register(new ResourceLocation(PhysicsControl.MODID, name), new SimpleTexture(new ResourceLocation(PhysicsControl.MODID, "textures/" + path + ".png")));
    }

    public static void setup() {
        registerTexture("icons", "gui/icons");
        registerTexture("widgets", "gui/widgets");
    }

    @SubscribeEvent
    public static void onInitGui(final GuiScreenEvent.InitGuiEvent.Post event) {
//        if (event.getGui() instanceof MainMenuScreen) {
//            Minecraft.getInstance().setScreen(new GuiPhysicsSimulator());
//        }
    }
}
