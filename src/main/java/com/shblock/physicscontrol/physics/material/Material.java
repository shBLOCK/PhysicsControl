package com.shblock.physicscontrol.physics.material;

import com.shblock.physicscontrol.PhysicsControl;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

public abstract class Material extends ForgeRegistryEntry<Material> { //TODO
    public final Item item;

    public Material(ResourceLocation regName, Item item) {
        setRegistryName(regName);
        this.item = item;
    }

    public Material(String name, Item item) {
        this(new ResourceLocation(PhysicsControl.MODID, name), item);
    }

    public float calcFriction(double surfaceArea, float friction, float itemAmount) {
        return friction;
    }

    public float calcRestitution(double surfaceArea, float restitution, float itemAmount) {
        return restitution;
    }

    public String getLocalizeName() {
        ResourceLocation regName = getRegistryName();
        return regName.getNamespace() + ".physics.material." + regName.getPath() + ".name";
    }
}
