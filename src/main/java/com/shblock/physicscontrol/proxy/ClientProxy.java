package com.shblock.physicscontrol.proxy;

import com.shblock.physicscontrol.PhysicsControl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = PhysicsControl.MODID)
public class ClientProxy {
    public static void setup() {
        Minecraft.getInstance().getTextureManager().register(new ResourceLocation(PhysicsControl.MODID, "icons"), new SimpleTexture(new ResourceLocation(PhysicsControl.MODID, "textures/gui/icons.png")));
    }
}
