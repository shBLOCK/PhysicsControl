package com.shblock.physicscontrol.physics.user_obj;

import com.shblock.physicscontrol.Config;
import com.shblock.physicscontrol.physics.material.Material;
import com.shblock.physicscontrol.physics.util.NBTSerializer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;
import org.jbox2d.particle.ParticleGroup;

import javax.annotation.Nullable;
import java.util.*;

public class ElasticGroupUserObj implements INBTSerializable<CompoundNBT> {
    /**
     * 0b0123   int[]{0, 1, 2, 3}
     *
     *    0      1
     *
     *
     *    2      3
     */
    private static final Map<Integer, int[]> DATA = new HashMap<>();
    static {
        DATA.put(0b1110, new int[]{0, 2, 1});
        DATA.put(0b0111, new int[]{2, 3, 1});
        DATA.put(0b1011, new int[]{0, 2, 3});
        DATA.put(0b1101, new int[]{0, 3, 1});
        DATA.put(0b1111, new int[]{0, 2, 1, 1, 2, 3});
    }
    private static final int[] DEFAULT = new int[0];
    private static int[] getVertexes(int key) {
        return DATA.getOrDefault(key, DEFAULT);
    }

    private Vec2[] uvArray;
    private int[] mesh;
    private Material material;

    private int lastParticleCount;

    public ElasticGroupUserObj() {}

    public static ElasticGroupUserObj create(World world, ParticleGroup group, @Nullable Material material) {
        ElasticGroupUserObj instance = new ElasticGroupUserObj();

        instance.material = material;

        instance.lastParticleCount = group.getParticleCount();

        instance.buildMesh(world, group);

        return instance;
    }

    private void buildMesh(World world, ParticleGroup group) {
        if (group.getParticleCount() < 3) {
            this.uvArray = new Vec2[0];
            this.mesh = new int[0];
            return;
        }

        int first = group.getBufferIndex();
        int last = first + group.getParticleCount();
        Vec2[] posBuf = world.getParticlePositionBuffer();
        Object[] objBuf = world.getParticleUserDataBuffer();

        List<Float> cols = new ArrayList<>();
        List<Float> rows = new ArrayList<>();
        for (int i=first; i<last; i++) {
            Vec2 pos = posBuf[i];
            if (!cols.contains(pos.x)) {
                cols.add(pos.x);
            }
            if (!rows.contains(pos.y)) {
                rows.add(pos.y);
            }
        }
        cols.sort(Float::compare);
        rows.sort(Float::compare);

        int[][] map = new int[cols.size()][rows.size()]; // Every point represent the uv index of that point, or -1 if there's no particle at that point
        for (int[] row : map) { // Fill the map with -1
            Arrays.fill(row, -1);
        }
        List<Vec2> uvList = new ArrayList<>();
        for (int i=first; i<last; i++) {
            Vec2 pos = posBuf[i];
            int col = cols.indexOf(pos.x);
            int row = rows.indexOf(pos.y);
            if (col == -1 || row == -1) {
                assert false : "col: " + col + ", row: " + row;
                continue;
            }
            ElasticParticleUserObj obj = (ElasticParticleUserObj) objBuf[i];
            uvList.add(new Vec2(obj.u, obj.v));
            int index = uvList.size() - 1;
            obj.uvIndex = index;
            map[col][row] = index;
        }
        this.uvArray = uvList.toArray(new Vec2[0]);

        List<Integer> meshList = new ArrayList<>();
        for (int y=0; y<rows.size() - 1; y++) {
            for (int x=0; x<cols.size() - 1; x++) {
                int ll = map[x    ][y    ];
                int ul = map[x + 1][y    ];
                int lu = map[x    ][y + 1];
                int uu = map[x + 1][y + 1];

                int key = (ll == -1 ? 0 : 1) +
                          (ul == -1 ? 0 : 2) +
                          (lu == -1 ? 0 : 4) +
                          (uu == -1 ? 0 : 8);
                for (int vertex : getVertexes(key)) {
                    switch (vertex) {
                        case 0:
                            meshList.add(ll);
                            break;
                        case 1:
                            meshList.add(ul);
                            break;
                        case 2:
                            meshList.add(lu);
                            break;
                        case 3:
                            meshList.add(uu);
                            break;
                    }
                }
            }
        }
        this.mesh = new int[meshList.size()];
        for (int i=0; i<meshList.size(); i++) {
            this.mesh[i] = meshList.get(i);
        }
    }

    /**
     * Rebuild the mesh if current particle count in the group not equals lastParticleCount
     * @return if the mesh has been rebuilt
     */
    public boolean update(World world, ParticleGroup group) {
        if (group.getParticleCount() != lastParticleCount) {
            lastParticleCount = group.getParticleCount();
            buildMesh(world, group);
            return true;
        }
        return false;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public ResourceLocation getTexture() {
        return this.material == null ? null : this.material.texture;
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.put("uv_array", NBTSerializer.toNBT(this.uvArray));
        nbt.putIntArray("mesh", this.mesh);
        if (this.material != null) {
            nbt.putString("material", this.material.getId().toString());
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        this.uvArray = NBTSerializer.vec2listFromNBT(nbt.get("uv_array"));
        this.mesh = nbt.getIntArray("mesh");
        if (nbt.contains("material")) {
            this.material = Config.getMaterialFromId(new ResourceLocation(nbt.getString("material")));
        } else {
            this.material = null;
        }
    }

    public Vec2[] getUvArray() {
        return uvArray;
    }

    public int[] getMesh() {
        return mesh;
    }

//    public Vec2[] getVertexes(World world, ParticleGroup group) {
//        Vec2[] posBuf = world.getParticlePositionBuffer();
//        Object[] objBuf = world.getParticleUserDataBuffer();
//
//        Vec2[] vertexes = new Vec2[uvArray.length];
//        for (int i=0; i<uvArray.length; i++) {
//            vertexes[i] = uvArray[i].clone();
//        }
//
//        int first = group.getBufferIndex();
//        int last = first + group.getParticleCount();
//        for (int i=first; i<last; i++) {
//            vertexes[((ElasticParticleUserObj) objBuf[i]).uvIndex] = posBuf[i].clone();
//        }
//
//        return vertexes;
//    }
}
