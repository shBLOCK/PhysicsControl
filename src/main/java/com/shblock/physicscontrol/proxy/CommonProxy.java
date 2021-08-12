package com.shblock.physicscontrol.proxy;

import com.shblock.physicscontrol.PhysicsControl;
import com.shblock.physicscontrol.command.CommandSerializer;
import com.shblock.physicscontrol.command.EditOperations2D;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = PhysicsControl.MODID)
public class CommonProxy {
    public static void setup() {
        CommandSerializer.init();
        EditOperations2D.init();
    }

//    @SubscribeEvent
//    public static void onRegistryBuild(final RegistryEvent.NewRegistry event) {
//        new RegistryBuilder().setType(Material.class).setName(new ResourceLocation(PhysicsControl.MODID, "materials")).create();
//    }
//
//    @SubscribeEvent
//    public static void onMaterialRegister(final RegistryEvent.Register<Material> event) {
//        event.getRegistry().registerAll(
//                new Material(Items.STONE, )
//        );
//    }
}
