package com.shblock.physicscontrol.physics.material;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

public class Material {
    public final Item item;
    public final ResourceLocation texture;
    public final ResourceLocation sound;
    public final float density;
    public final float friction;
    public final float restitution;

    public Material(Item item, ResourceLocation texture, ResourceLocation sound, float density, float friction, float restitution) {
        this.item = item;
        this.texture = texture;
        this.sound = sound;
        this.density = density;
        this.friction = friction;
        this.restitution = restitution;
    }

    public ResourceLocation getId() {
        return this.item.getRegistryName();
    }

    public String getLocalizeName() {
        return "pc.physics.material." + item.getRegistryName();
    }
}
