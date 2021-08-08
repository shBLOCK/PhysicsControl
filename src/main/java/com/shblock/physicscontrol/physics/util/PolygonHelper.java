package com.shblock.physicscontrol.physics.util;

import com.google.common.collect.Lists;
import com.shblock.physicscontrol.PhysicsControl;
import org.apache.logging.log4j.Level;

import java.util.ArrayList;
import java.util.List;

public class PolygonHelper {
    public static boolean isPointInTriangle(Vector2f a, Vector2f b, Vector2f c, Vector2f point) {
        Vector2f pa = a.subtract(point);
        Vector2f pb = b.subtract(point);
        Vector2f pc = c.subtract(point);
        float t1 = pa.cross(pb);
        float t2 = pb.cross(pc);
        float t3 = pc.cross(pa);
        return t1*t2 >= 0 && t1*t3 >= 0;
    }

    public static boolean isConcaveVertex(Vector2f last, Vector2f mid, Vector2f next)
    {
        Vector2f v1 = last.subtract(mid);
        Vector2f v2 = next.subtract(mid);

        float cross = v1.x*v2.y-v1.y*v2.x;
        return cross>0;
    }

    public static List<Integer> cutEar(Vector2f[] vertexList) {
        List<Integer> polygon = new ArrayList<>();
        for (int i=0; i< vertexList.length; i++) {
            polygon.add(i);
        }
        List<Integer> result = cutEarMain(polygon, vertexList);
        if (result != null) {
            return result;
        }

        polygon = new ArrayList<>();
        for (int i=0; i< vertexList.length; i++) {
            polygon.add(vertexList.length - 1 - i);
        }
        result = cutEarMain(polygon, vertexList);
        if (result != null) {
            return result;
        }
        PhysicsControl.log(Level.WARN, "Failed to cut polygon to triangles!");
        return null;
    }

    private static List<Integer> cutEarMain(List<Integer> polygon, Vector2f[] vertexList) {
        List<Integer> indexList = new ArrayList<>();
        while (polygon.size() > 3) {
            boolean didCutAny = false;
            for (int midIndex=0; midIndex<polygon.size(); midIndex++) {
                int lastIndex = midIndex == 0 ? polygon.size() - 1 : midIndex - 1;
                int nextIndex = midIndex == polygon.size() - 1 ? 0 : midIndex + 1;
                int mid = polygon.get(midIndex);
                int last = polygon.get(lastIndex);
                int next = polygon.get(nextIndex);
                Vector2f lastVec = vertexList[last];
                Vector2f midVec = vertexList[mid];
                Vector2f nextVec = vertexList[next];

                if (isConcaveVertex(lastVec, midVec, nextVec)) {
                    continue;
                }

                boolean isAnyInTri = false;
                for (int p=0; p<polygon.size(); p++) {
                    if (p == lastIndex || p == midIndex || p == nextIndex) {
                        continue;
                    }
                    if (isPointInTriangle(lastVec, midVec, nextVec, vertexList[polygon.get(p)])) {
                        isAnyInTri = true;
                        break;
                    }
                }
                if (isAnyInTri) {
                    continue;
                }

                indexList.add(last);
                indexList.add(mid);
                indexList.add(next);
                polygon.remove(midIndex);
                didCutAny = true;
                break;
            }
            if (!didCutAny) {
                return null;
            }
        }
        indexList.addAll(polygon); // add the last 3 vertexes
        return indexList;
    }
}
