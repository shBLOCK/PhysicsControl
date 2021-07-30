package com.shblock.physicscontrol.item;

import com.shblock.physicscontrol.PhysicsControl;
import com.shblock.physicscontrol.client.gui.GuiClientHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class ItemPhysicsSimulator extends Item {
    public static final String NAME = "physics_simulator";

    public ItemPhysicsSimulator() {
        super(new Properties().tab(ItemGroup.TAB_TOOLS)); //TODO: make a mod item tab
        setRegistryName(PhysicsControl.MODID, NAME);
    }

    @Override
    public int getItemStackLimit(ItemStack stack) {
        return 1;
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (world.isClientSide()) {
            DistExecutor.runWhenOn(Dist.CLIENT, () -> GuiClientHandler::openPhysicsSimulatorGui);
        }
        return super.use(world, player, hand);
    }
}
