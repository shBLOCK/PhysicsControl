package com.shblock.physicscontrol;

import com.shblock.physicscontrol.proxy.ClientProxy;
import com.shblock.physicscontrol.proxy.CommonProxy;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(PhysicsControl.MODID)
public class PhysicsControl {
    public static final String NAME = "Physics Control";
    public static final String MODID = "physicscontrol";

    private static final Logger LOGGER = LogManager.getLogger(NAME);

    public PhysicsControl() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::onCommonSetup);
        modEventBus.addListener(this::onClientSetup);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void onCommonSetup(final FMLCommonSetupEvent event) {
        CommonProxy.setup();
    }

    private void onClientSetup(final FMLClientSetupEvent event) {
        ClientProxy.setup();
    }

    public static void log(String message) {
        LOGGER.log(Level.INFO, message);
    }

    public static void log(Level level, String message) {
        LOGGER.log(level, message);
    }
}
