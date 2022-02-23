package com.shblock.physicscontrol;

import com.google.common.collect.Lists;
import com.shblock.physicscontrol.physics.material.Material;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.apache.logging.log4j.Level;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Config {
    public static final ForgeConfigSpec COMMON_CONFIG;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> MATERIALS_CONFIG;
    private static final List<? extends String> DEFAULT_MATERIALS = Lists.newArrayList(
            "stone, textures/block/stone, block.stone.break, 2.4, 0.9, 0.2",
            "cobblestone, textures/block/cobblestone, block.stone.break, 2.4, 0.9, 0.2",
            "slime_block, textures/block/slime_block, block.slime_block.hit, 1, 1, 0.95",
            "gold, textures/block/gold_block, block.metal.break, 7, 0.9, 0.2",
            "iron, textures/block/iron_block, block.metal.break, 6, 0.9, 0.2",
            "redstone, textures/block/redstone_block, block.metal.break, 5, 0.9, 0.2",
            "diamond, textures/block/diamond_block, block.metal.break, 8, 0.9, 0.2",
            "ice, textures/block/packed_ice, block.glass.break, 2.4, 0.1, 0.2",
            "glass, textures/block/glass, block.glass.break, 1.5, 0.2, 0.2"
    );

    static {
        ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();
        COMMON_BUILDER.comment("General settings").push("general");
        MATERIALS_CONFIG = COMMON_BUILDER
                .comment("Materials")
                .comment("Pattern: \"<ItemRegistryName>, <TextureName>, <SoundName(WIP)>, <Density(kg/m²)(>0)>, <Friction(>=0)>, <Restitution(>=0, should below 1)>\" (Split with \", \")")
                .defineList("materials", DEFAULT_MATERIALS, obj -> true);
        COMMON_BUILDER.pop();
        COMMON_CONFIG = COMMON_BUILDER.build();
    }

    public static final Map<ResourceLocation, Material> materials = new HashMap<>();

    public static void initMaterials() {
        for (String s : MATERIALS_CONFIG.get()) {
            String[] config = s.split(", ");
            if (config.length != 6) {
                PhysicsControl.log(Level.WARN, "Config material \"" + s + "\" is invalid, ignored! (Pattern invalid)");
                continue;
            }

            Item item = GameRegistry.findRegistry(Item.class).getValue(new ResourceLocation(config[0]));
            if (item == null) {
                PhysicsControl.log(Level.WARN, "Config material \"" + s + "\" is invalid, ignored! (Item invalid)");
                continue;
            }
            ResourceLocation texture = new ResourceLocation(config[1] + ".png");
            ResourceLocation sound = new ResourceLocation(config[2]);
            float density, friction, restitution;
            try {
                density = Float.parseFloat(config[3]);
                friction = Float.parseFloat(config[4]);
                restitution = Float.parseFloat(config[5]);
                if (density <= 0F) {
                    throw new NumberFormatException("Density below or equals zero");
                }
                if (friction < 0F) {
                    throw new NumberFormatException("Friction below zero");
                }
                if (restitution < 0F) {
                    throw new NumberFormatException("Restitution below zero");
                }
            } catch (NumberFormatException e) {
                PhysicsControl.log(Level.WARN, "Config material \"" + s + "\" is invalid, ignored! (Number invalid: " + e + " )");
                continue;
            }

            Material material = new Material(item, texture, sound, density, friction, restitution);
            materials.put(material.getId(), material);
        }
    }

    public static Material getMaterialFromId(ResourceLocation id) {
        return materials.get(id);
    }
}
