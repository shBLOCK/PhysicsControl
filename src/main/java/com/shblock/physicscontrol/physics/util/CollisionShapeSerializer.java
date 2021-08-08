package com.shblock.physicscontrol.physics.util;

import com.jme3.bullet.collision.shapes.*;
import com.jme3.bullet.collision.shapes.infos.ChildCollisionShape;
import com.jme3.bullet.collision.shapes.infos.CompoundMesh;
import com.jme3.bullet.collision.shapes.infos.IndexedMesh;
import com.jme3.math.Vector3f;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.FloatNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class CollisionShapeSerializer {
    public static CompoundNBT toNBT(CollisionShape shape) {
        CompoundNBT nbt = new CompoundNBT();
        nbt.put("scale", NBTSerializer.toNBT(shape.getScale(null)));
        nbt.putFloat("margin", shape.getMargin());
        String type = "";
        if (shape instanceof Box2dShape) {
            type = "box2d";
            Box2dShape cs = (Box2dShape) shape;
            nbt.put("half_extents", NBTSerializer.toNBT(cs.getHalfExtents(null)));
        } else if (shape instanceof BoxCollisionShape) {
            type = "box";
            BoxCollisionShape cs = (BoxCollisionShape) shape;
            nbt.put("half_extents", NBTSerializer.toNBT(cs.getHalfExtents(null)));
        } else if (shape instanceof CapsuleCollisionShape) {
            type = "capsule";
            CapsuleCollisionShape cs = (CapsuleCollisionShape) shape;
            nbt.putFloat("radius", cs.getRadius());
            nbt.putFloat("height", cs.getHeight());
            nbt.putInt("axis", cs.getAxis());
        } else if (shape instanceof CompoundCollisionShape) {
            type = "compound";
            CompoundCollisionShape cs = (CompoundCollisionShape) shape;
            ListNBT list_nbt = new ListNBT();
            for (ChildCollisionShape child_shape : cs.listChildren()) {
                list_nbt.add(childShapeToNBT(child_shape));
            }
            nbt.put("childes", list_nbt);
        } else if (shape instanceof ConeCollisionShape) {
            type = "cone";
            ConeCollisionShape cs = (ConeCollisionShape) shape;
            nbt.putFloat("radius", cs.getRadius());
            nbt.putFloat("height", cs.getHeight());
            nbt.putInt("axis", cs.getAxis());
        } else if (shape instanceof CylinderCollisionShape) {
            type = "cylinder";
            CylinderCollisionShape cs = (CylinderCollisionShape) shape;
            nbt.put("half_extents", NBTSerializer.toNBT(cs.getHalfExtents(null)));
            nbt.putInt("axis", cs.getAxis());
        } else if (shape instanceof EmptyShape) {
            type = "empty";
        } else if (shape instanceof GImpactCollisionShape) {
            type = "g_impact";
            GImpactCollisionShape cs = (GImpactCollisionShape) shape;
            nbt.put(
                    "mesh",
                    NBTSerializer.toNBT(
                            MeshHelper.getCompoundMesh(cs)
                    )
            );
        } else if (shape instanceof HeightfieldCollisionShape) {
            type = "height_field";
            HeightfieldCollisionShape cs = (HeightfieldCollisionShape) shape;
            Class<? extends HeightfieldCollisionShape> clz = cs.getClass();
            int a = 0;
            for (Field field : clz.getDeclaredFields()) {
                try {
                    field.setAccessible(true);
                    switch (field.getName()) {
                        case "heightStickLength":
                        case "heightStickWidth":
                        case "upAxis":
                            nbt.putInt(field.getName(), field.getInt(cs));
                            break;
                        case "flipQuadEdges":
                        case "flipTriangleWinding":
                        case "useDiamond":
                        case "useZigzag":
                            nbt.putBoolean(field.getName(), field.getBoolean(cs));
                            break;
                        case "heightfieldData":
                            ListNBT fl = new ListNBT();
                            for (float v : ((float[]) field.get(cs))) {
                                fl.add(FloatNBT.valueOf(v));
                            }
                            nbt.put(field.getName(), fl);
                            break;
                    }
                    field.setAccessible(false);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        } else if (shape instanceof HullCollisionShape) {
            type = "hull";
            HullCollisionShape cs = (HullCollisionShape) shape;
            ListNBT fl = new ListNBT();
            for (float h : cs.copyHullVertices()) {
                fl.add(FloatNBT.valueOf(h));
            }
            nbt.put("hull_vertices", fl);
        } else if (shape instanceof MeshCollisionShape) {
            type = "mesh";
            MeshCollisionShape cs = (MeshCollisionShape) shape;
            nbt.putByteArray("bvh", cs.serializeBvh());
            try {
                Field field = cs.getClass().getDeclaredField("nativeMesh");
                field.setAccessible(true);
                CompoundMesh mesh = (CompoundMesh) field.get(cs);
                field.setAccessible(false);
                nbt.put(
                        "mesh",
                        NBTSerializer.toNBT(
                                mesh
                        )
                );
            } catch (IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }
        } else if (shape instanceof MultiSphere) {
            type = "multi_sphere";
            MultiSphere cs = (MultiSphere) shape;
            ListNBT spheres = new ListNBT();
            for (int i=0; i<cs.countSpheres(); i++) {
                CompoundNBT sn = new CompoundNBT();
                sn.put("center", NBTSerializer.toNBT(cs.copyCenter(i, null)));
                sn.putFloat("radius", cs.getRadius(i));
                spheres.add(sn);
            }
            nbt.put("spheres", spheres);
        } else if (shape instanceof PlaneCollisionShape) {
            type = "plane";
            PlaneCollisionShape cs = (PlaneCollisionShape) shape;
            nbt.put("plane", NBTSerializer.toNBT(cs.getPlane()));
        } else if (shape instanceof SimplexCollisionShape) {
            type = "simplex";
            SimplexCollisionShape cs = (SimplexCollisionShape) shape;
            ListNBT vertices = new ListNBT();
            for (float d : cs.copyVertices()) {
                vertices.add(FloatNBT.valueOf(d));
            }
            nbt.put("vertices", vertices);
        } else if (shape instanceof SphereCollisionShape) {
            type = "sphere";
            SphereCollisionShape cs = (SphereCollisionShape) shape;
            nbt.putFloat("radius", cs.getRadius());
        }
        nbt.putString("type", type);
        return nbt;
    }

    public static CollisionShape fromNBT(CompoundNBT nbt) {
        CollisionShape shape;
        switch (nbt.getString("type")) {
            case "box2d":
                shape = new Box2dShape(
                        NBTSerializer.vec3FromNBT(
                                nbt.getList("half_extents", Constants.NBT.TAG_FLOAT)
                        )
                );
                break;
            case "box":
                shape = new BoxCollisionShape(
                        NBTSerializer.vec3FromNBT(
                                nbt.getList("half_extents", Constants.NBT.TAG_FLOAT)
                        )
                );
                break;
            case "capsule":
                shape = new CapsuleCollisionShape(
                        nbt.getFloat("radius"),
                        nbt.getFloat("height"),
                        nbt.getInt("axis")
                );
                break;
            case "compound":
                shape = new CompoundCollisionShape();
                CompoundCollisionShape finalShape = (CompoundCollisionShape) shape;
                nbt.getList("childes", Constants.NBT.TAG_COMPOUND).forEach(
                        shape_nbt -> {
                            ChildCollisionShape child = childShapeFromNBT((CompoundNBT) shape_nbt);
                            finalShape.addChildShape(
                                    child.getShape(),
                                    child.copyOffset(null),
                                    child.copyRotationMatrix(null)
                            );
                        }
                );
                break;
            case "cone":
                shape = new ConeCollisionShape(
                        nbt.getFloat("radius"),
                        nbt.getFloat("height"),
                        nbt.getInt("axis")
                );
                break;
            case "cylinder":
                shape = new CylinderCollisionShape(
                        NBTSerializer.vec3FromNBT(nbt.getList("half_extents", Constants.NBT.TAG_FLOAT)),
                        nbt.getInt("axis")
                );
                break;
            case "empty":
                shape = new EmptyShape(false /* unused dummy value */);
                break;
            case "g_impact":
                CompoundMesh compoundMesh = NBTSerializer.cMeshFromNBT(nbt.getCompound("mesh"));
                IndexedMesh[] meshes = MeshHelper.getSubMeshes(compoundMesh).toArray(new IndexedMesh[0]);
                shape = new GImpactCollisionShape(
                        meshes
                );
                break;
            case "height_field":
                ListNBT hmn = nbt.getList("heightfieldData", Constants.NBT.TAG_FLOAT);
                float[] hm = new float[hmn.size()];
                for (int i=0; i<hmn.size(); i++) {
                    hm[i] = hmn.getFloat(i);
                }
                shape = new HeightfieldCollisionShape(
                        nbt.getInt("heightStickLength"),
                        nbt.getInt("heightStickWidth"),
                        hm,
                        NBTSerializer.vec3FromNBT(nbt.getList("scale", Constants.NBT.TAG_FLOAT)), // handle it here instead of down below
                        nbt.getInt("upAxis"),
                        nbt.getBoolean("flipQuadEdges"),
                        nbt.getBoolean("flipTriangleWinding"),
                        nbt.getBoolean("useDiamond"),
                        nbt.getBoolean("useZigzag")
                );
                break;
            case "hull":
                ListNBT hv = nbt.getList("hull_vertices", Constants.NBT.TAG_FLOAT);
                float[] vs = new float[hv.size()];
                for (int i=0; i<hv.size(); i++) {
                    vs[i] = hv.getFloat(i);
                }
                shape = new HullCollisionShape(vs);
                break;
            case "mesh":
                CompoundMesh mesh = NBTSerializer.cMeshFromNBT(nbt.getCompound("mesh"));
                IndexedMesh[] sub_meshes = MeshHelper.getSubMeshes(mesh).toArray(new IndexedMesh[0]);
                shape = new MeshCollisionShape(
                        nbt.getByteArray("bvh"),
                        sub_meshes
                );
                break;
            case "multi_sphere":
                ListNBT spheres = nbt.getList("spheres", Constants.NBT.TAG_COMPOUND);
                List<Vector3f> centers = new ArrayList<>();
                List<Float> radii = new ArrayList<>();
                for (INBT sphere : spheres) {
                    centers.add(NBTSerializer.vec3FromNBT(((CompoundNBT)sphere).getList("center", Constants.NBT.TAG_FLOAT)));
                    radii.add(((CompoundNBT) sphere).getFloat("radius"));
                }
                shape = new MultiSphere(centers, radii);
                break;
            case "plane":
                shape = new PlaneCollisionShape(
                      NBTSerializer.planeFromNBT(nbt.getCompound("plane"))
                );
                break;
            case "simplex":
                ListNBT vertices_nbt = nbt.getList("vertices", Constants.NBT.TAG_FLOAT);
                Vector3f[] vertices = new Vector3f[vertices_nbt.size() / 3];
                for (int i=0; i<vertices_nbt.size()/3; i++) {
                    vertices[i] = new Vector3f(
                            vertices_nbt.getFloat(i * 3),
                            vertices_nbt.getFloat(i * 3 + 1),
                            vertices_nbt.getFloat(i * 3 + 2)
                    );
                }
                shape = new SimplexCollisionShape(vertices);
                break;
            case "sphere":
                shape = new SphereCollisionShape(nbt.getFloat("radius"));
                break;
            default:
                throw new IllegalArgumentException("Unknown collision shape type: " + nbt.getString("type"));
        }
        if (!(shape instanceof HeightfieldCollisionShape)) {
            shape.setScale(NBTSerializer.vec3FromNBT(nbt.getList("scale", Constants.NBT.TAG_FLOAT)));
        }
        if (!(shape instanceof SphereCollisionShape) && !(shape instanceof CapsuleCollisionShape)){
            shape.setMargin(nbt.getFloat("margin"));
        }
        return shape;
    }

    public static CompoundNBT childShapeToNBT(ChildCollisionShape shape) {
        CompoundNBT nbt = new CompoundNBT();
        nbt.put("offset", NBTSerializer.toNBT(shape.copyOffset(null)));
        nbt.put("rotation", NBTSerializer.toNBT(shape.copyRotationMatrix(null)));
        nbt.put("shape", toNBT(shape.getShape()));
        return nbt;
    }

    public static ChildCollisionShape childShapeFromNBT(CompoundNBT nbt) {
        return new ChildCollisionShape(
                NBTSerializer.vec3FromNBT(nbt.getList("offset", Constants.NBT.TAG_FLOAT)),
                NBTSerializer.matrix3FromNBT(nbt.getList("rotation", Constants.NBT.TAG_FLOAT)),
                fromNBT(nbt.getCompound("shape"))
        );
    }
}
