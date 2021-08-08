package com.shblock.physicscontrol.physics.util;

import com.jme3.bullet.collision.shapes.GImpactCollisionShape;
import com.jme3.bullet.collision.shapes.infos.CompoundMesh;
import com.jme3.bullet.collision.shapes.infos.IndexedMesh;
import com.jme3.math.Vector3f;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class MeshHelper {
    public static CompoundMesh getCompoundMesh(GImpactCollisionShape shape) {
        try {
            Field field = shape.getClass().getDeclaredField("nativeMesh");
            field.setAccessible(true);
            CompoundMesh result = (CompoundMesh) field.get(shape);
            field.setAccessible(false);
            return result;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            assert false : e;
            return null;
        }
    }

    public static ArrayList<IndexedMesh> getSubMeshes(CompoundMesh mesh) {
        try {
            Field field = mesh.getClass().getDeclaredField("submeshes");
            field.setAccessible(true);
            ArrayList obj_list = (ArrayList) field.get(mesh);
            ArrayList<IndexedMesh> result = new ArrayList<>();
            for (Object obj : obj_list) {
                result.add((IndexedMesh) obj);
            }
            field.setAccessible(false);
            return result;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            assert false : e;
            return null;
        }
    }

    public static IndexedMesh create2DPolygon(Vector2f... vertexes) {
        Vector3f[] converted_vertexes = new Vector3f[vertexes.length];
        for (int i=0; i< vertexes.length; i++) {
            converted_vertexes[i] = vertexes[i].toVec3();
        }

        List<Integer> indexes = PolygonHelper.cutEar(vertexes);
        if (indexes == null) {
            return null;
        }
        int[] indexesArray = new int[indexes.size()];
        for (int i=0; i<indexes.size(); i++) {
            indexesArray[i] = indexes.get(i);
        }
        return new IndexedMesh(converted_vertexes, indexesArray);
    }
}
