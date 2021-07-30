package com.shblock.physicscontrol;

import com.shblock.physicscontrol.item.ItemPhysicsSimulator;
import com.shblock.physicscontrol.proxy.ClientProxy;
import com.shblock.physicscontrol.proxy.CommonProxy;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.IForgeRegistry;
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

    @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> event) {
        }

        @SubscribeEvent
        public static void onItemRegistry(final RegistryEvent.Register<Item> event) {
            PhysicsControl.log("PCREGITEM!");
            IForgeRegistry<Item> register = event.getRegistry();
            register.register(new ItemPhysicsSimulator());
        }
    }
}
