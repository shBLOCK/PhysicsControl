package com.shblock.physicscontrol.physics.user_obj;

import com.shblock.physicscontrol.Config;
import com.shblock.physicscontrol.physics.material.Material;
import com.shblock.physicscontrol.physics.util.BodyHelper;
import com.shblock.physicscontrol.physics.util.NBTSerializer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.World;

import javax.annotation.Nullable;
import java.util.Random;

import static org.lwjgl.opengl.GL11.*;

public class BodyUserObj extends UserObjBase {
    protected static final Random RANDOM = new Random();
    private static final Random SOUND_RANDOMIZER = new Random();

    private String name;
    private int zLevel;
    public int r, g, b, alpha;
    private int[] lastColor = null; // last color before change the material type

    private Vec2[] polygonVertexCache = null;

    private Material material;

    /**
     * Dummy constructor for fromNBT, DON'T USE THIS!
     */
    public BodyUserObj() {
        super(-1);
    }

    public BodyUserObj(int id, String name) {
        super(id);
        this.zLevel = id;
        this.name = name;
        randomColor();
    }

    public void moveZLevelUp(World space) {
        if (this.zLevel >= space.getBodyCount() - 1) {
            return;
        }

        BodyUserObj closest = null;
        Body body = space.getBodyList();
        while (body != null) {
            BodyUserObj u_obj = (BodyUserObj) body.getUserData();
            if (u_obj != null) {
                if (closest == null) {
                    if (u_obj.zLevel > this.zLevel) {
                        closest = u_obj;
                    }
                } else {
                    if (u_obj.zLevel > this.zLevel && u_obj.zLevel < closest.zLevel) {
                        closest = u_obj;
                    }
                }
            }
            body = body.m_next;
        }
        if (closest != null) {
            int old = this.zLevel;
            this.zLevel = closest.zLevel;
            closest.zLevel = old;
        } else {
            this.zLevel++;
        }
    }

    public void moveZLevelDown(World space) {
        if (this.zLevel <= 0) {
            return;
        }

        BodyUserObj closest = null;
        Body body = space.getBodyList();
        while (body != null) {
            BodyUserObj u_obj = (BodyUserObj) body.getUserData();
            if (u_obj != null) {
                if (closest == null) {
                    if (u_obj.zLevel < this.zLevel) {
                        closest = u_obj;
                    }
                } else {
                    if (u_obj.zLevel < this.zLevel && u_obj.zLevel > closest.zLevel) {
                        closest = u_obj;
                    }
                }
            }
            body = body.m_next;
        }
        if (closest != null) {
            int old = this.zLevel;
            this.zLevel = closest.zLevel;
            closest.zLevel = old;
        } else {
            this.zLevel--;
        }
    }

    public int getZLevel() {
        return this.zLevel;
    }

    public float getFloatR() {
        return this.r / 255F;
    }

    public float getFloatG() {
        return this.g / 255F;
    }

    public float getFloatB() {
        return this.b / 255F;
    }

    public float getFloatAlpha() {
        return this.alpha / 255F;
    }

    public float[] getColor4f() {
        return new float[]{
                getFloatR(),
                getFloatG(),
                getFloatB(),
                getFloatAlpha()
        };
    }

    public void randomColor() {
        r = RANDOM.nextInt(156) + 50;
        g = RANDOM.nextInt(156) + 50;
        b = RANDOM.nextInt(156) + 50;
        alpha = 255;
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = super.serializeNBT();
        nbt.putString("name", name);
        nbt.putInt("z_level", zLevel);
        nbt.putInt("r", r);
        nbt.putInt("g", g);
        nbt.putInt("b", b);
        nbt.putInt("alpha", alpha);
        if (this.lastColor != null) {
            nbt.putIntArray("last_color", this.lastColor);
        }
        if (this.polygonVertexCache != null) {
            nbt.put("polygon_vertex_cache", NBTSerializer.toNBT(this.polygonVertexCache));
        }
        if (this.material != null) {
            nbt.putString("material", this.material.getId().toString());
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        super.deserializeNBT(nbt);
        this.name = nbt.getString("name");
        this.zLevel = nbt.getInt("z_level");
        this.r = nbt.getInt("r");
        this.g = nbt.getInt("g");
        this.b = nbt.getInt("b");
        this.alpha = nbt.getInt("alpha");
        if (nbt.contains("last_color")) {
            this.lastColor = nbt.getIntArray("last_color");
        } else {
            this.lastColor = null;
        }
        if (nbt.contains("polygon_vertex_cache")) {
            this.polygonVertexCache = NBTSerializer.vec2listFromNBT(nbt.get("polygon_vertex_cache"));
        }
        if (nbt.contains("material")) {
            this.material = Config.getMaterialFromId(new ResourceLocation(nbt.getString("material")));
        } else {
            this.material = null;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Vec2[] getPolygonVertexCache() {
        return polygonVertexCache;
    }

    public void setPolygonVertexCache(Vec2[] polygonVertexCache) {
        this.polygonVertexCache = polygonVertexCache;
    }

    public boolean hasMaterial() {
        return this.material != null;
    }

    public Material getMaterial() {
        return this.material;
    }

    public void setMaterial(Body body, @Nullable Material material) {
        this.material = material;

        if (this.material != null) {
            this.lastColor = new int[]{r, g, b, alpha};
            this.r = 255;
            this.g = 255;
            this.b = 255;
            this.alpha = 255;
            BodyHelper.forEachFixture(
                    body,
                    fixture -> {
                        fixture.setDensity(material.density);
                        fixture.setFriction(material.friction);
                        fixture.setRestitution(material.restitution);
                    }
            );
            body.resetMassData();
        } else {
            if (this.lastColor != null) {
                this.r = this.lastColor[0];
                this.g = this.lastColor[1];
                this.b = this.lastColor[2];
                this.alpha = this.lastColor[3];
                this.lastColor = null;
            } else {
                randomColor();
            }
        }
    }

    public String getMaterialId() {
        return this.material == null ? null : this.material.getId().toString();
    }

    @OnlyIn(Dist.CLIENT)
    public String getMaterialName() {
        return this.material == null ? null : I18n.get(this.material.getLocalizeName());
    }

    public Item getMaterialItem() {
        return this.material == null ? null : this.material.item;
    }

    public ResourceLocation getTexture() {
        return this.material == null ? null : this.material.texture;
    }

    public float[] getPixelAt(Vec2 pos) {
        if (getTexture() != null) {
            Texture texture = Minecraft.getInstance().textureManager.getTexture(getTexture());
            if (texture != null) {
                glBindTexture(GL_TEXTURE_2D, texture.getId());
                float[] color = new float[4];
                glReadPixels((int) pos.x, (int) pos.y, 1, 1, GL_RGBA, GL_FLOAT, color);
                return color;
            }
        }

        return null;
    }

    @OnlyIn(Dist.CLIENT)
    public void playCollideSoundUI(float volume) {
        if (this.material != null) {
            Minecraft.getInstance().getSoundManager().play(SimpleSound.forUI(new SoundEvent(this.material.sound), SOUND_RANDOMIZER.nextFloat() + 0.5F, volume)); //TODO: random pitch, volume based on the collision force?
        }
    }
}
