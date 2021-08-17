package com.shblock.physicscontrol.physics.util;

import com.google.common.collect.Lists;
import com.shblock.physicscontrol.PhysicsControl;
import org.apache.logging.log4j.Level;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;

import java.util.ArrayList;
import java.util.List;

public class PolygonHelper {
    public static boolean isPointInTriangle(Vec2 a, Vec2 b, Vec2 c, Vec2 point) {
        Vec2 pa = a.sub(point);
        Vec2 pb = b.sub(point);
        Vec2 pc = c.sub(point);
        float t1 = Vec2.cross(pa, pb);
        float t2 = Vec2.cross(pb, pc);
        float t3 = Vec2.cross(pc, pa);
        return t1*t2 >= 0 && t1*t3 >= 0;
    }

    public static boolean isConcaveVertex(Vec2 last, Vec2 mid, Vec2 next) {
        Vec2 v1 = last.sub(mid);
        Vec2 v2 = next.sub(mid);

        float cross = v1.x*v2.y-v1.y*v2.x;
        return cross>0;
    }

    public static boolean isClockwise(Vec2[] list) {
        double d = 0D;
        for (int i=0; i<list.length-1; i++) {
            d += -0.5 * (list[i+1].y + list[i].y) * (list[i+1].x - list[i].x);
        }
        return d<=0;
    }

    public static List<Integer> cutEar(Vec2[] vertexList) {
        List<Integer> polygon = new ArrayList<>();
        if (isClockwise(vertexList)) {
            for (int i = 0; i < vertexList.length; i++) {
                polygon.add(vertexList.length - 1 - i);
            }
        } else {
            for (int i = 0; i < vertexList.length; i++) {
                polygon.add(i);
            }
        }
        List<Integer> result = cutEarMain(polygon, vertexList);
        if (result != null) {
            return result;
        }
        PhysicsControl.log(Level.WARN, "Failed to cut polygon to triangles!");
        return null;
    }

    private static List<Integer> cutEarMain(List<Integer> polygon, Vec2[] vertexList) {
        List<Integer> indexList = new ArrayList<>();
        while (polygon.size() > 3) {
            boolean didCutAny = false;
            for (int midIndex=0; midIndex<polygon.size(); midIndex++) {
                int lastIndex = midIndex == 0 ? polygon.size() - 1 : midIndex - 1;
                int nextIndex = midIndex == polygon.size() - 1 ? 0 : midIndex + 1;
                int mid = polygon.get(midIndex);
                int last = polygon.get(lastIndex);
                int next = polygon.get(nextIndex);
                Vec2 lastVec = vertexList[last];
                Vec2 midVec = vertexList[mid];
                Vec2 nextVec = vertexList[next];

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

//    public static boolean isConvex(Vec2[] vertexList) {
//        boolean tag = true;
//        int n = vertexList.length;
//        int j,k,t;
//        for(int i=0; i<n; i++) {
//            j = i;
//            k = i+1;
//            t = i+2;
//
//            if(k==n) {
//                k = 0;
//            }
//            if(t==n+1) {
//                t = 1;
//            }
//            if(t==n) {
//                t = 0;
//            }
//
//            Vec2 p1 = vertexList[k].sub(vertexList[j]);
//            Vec2 p2 = vertexList[t].sub(vertexList[k]);
//
//            //cross
//            float ans = p1.x*p2.y - p1.y*p2.x;
//            if(ans<0) {
//                tag = false;
//                break;
//            }
//        }
//        return tag;
//    }
//
//    public static List<Integer> cutToConvex(Vec2[] vertexList) {
//        List<Vec2> vertexes = Lists.newArrayList(vertexList);
//        List<Vec2> convex = new ArrayList<>();
//        List<Vec2[]> result = new ArrayList<>();
//
//        int i=0;
//        while (!vertexes.isEmpty()) {
//            while (!vertexes.isEmpty()) {
//                convex.add(vertexes.remove(0));
//                if (convex.size() > 3) {
//
//                }
//            }
//        }
//
//
//
//
//        PhysicsControl.log(Level.WARN, "Failed to cut polygon to convex shapes!");
//        return null;
//    }

    public static double calculateArea(PolygonShape shape) {
        int point_num = shape.m_count;
        if(point_num < 3)return 0.0;
        Vec2[] points = shape.m_vertices;
        double s = points[0].y * (points[point_num-1].x - points[1].x);
        for(int i = 1; i < point_num; ++i) {
            s += points[i].y * (points[i - 1].x - points[(i + 1) % point_num].x);
        }
        return Math.abs(s/2.0);
    }

    public static Vec2 calculateCentroid(Vec2[] vertexes) {
        double area = 0.0;

        double Cx = 0.0;
        double Cy = 0.0;

        Vec2 v0 = vertexes[0];

        for (int i = 1; i < vertexes.length - 1; i++)
        {
            Vec2 v1 = new Vec2(
                    vertexes[i].x - v0.x, vertexes[i].y - v0.y);

            Vec2 v2 = new Vec2(
                    vertexes[i + 1].x - v0.x, vertexes[i + 1].y - v0.y);

            double A = (v1.x * v2.y - v2.x * v1.y) / 2;
            area += A;

            double x = 0 + v1.x + v2.x; //x=(x1+x2+x3)/3
            double y = 0 + v1.y + v2.y; //y=(xy+y2+y3)/3

            Cx += A * x;
            Cy += A * y;
        }

        Cx = Cx / area / 3 + v0.x;
        Cy = Cy / area / 3 + v0.y;

        return new Vec2(Cx, Cy);
    }
}
