package com.shblock.physicscontrol.client.gui;

import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImVec4;
import imgui.flag.ImGuiCol;
import net.minecraft.client.Minecraft;

public class ImGuiUtil {
    private static final Minecraft MC = Minecraft.getInstance();
    private static int scale = 4;

    public static boolean isInMainViewport() {
        return ImGui.getWindowViewport().getID() == ImGui.getMainViewport().getID();
    }

    public static void infiniteRepeatImage(ImDrawList drawList, int image, float x0, float y0, float x1, float y1, float u0, float v0, float u1, float v1, int color) {
        drawList.addImage(image, x0, y0, x1, y1, u0, v0, u1, v1, color); //TODO
    }

    public static void resizeableImage(ImDrawList drawList, int image, int imageSize, int edgeT, int edgeD, int edgeL, int edgeR, float xA, float yA, int w, int h, int u0, int v0, int u1, int v1, int color) {
        float imgSize = imageSize;

        int pixW = w / scale;
        int pixH = h / scale;
        w = pixW * scale;
        h = pixH * scale;

        int midW = pixW - edgeL - edgeR;
        int midH = pixH - edgeT - edgeD;

        float xB = xA + edgeL * scale;
        float xC = xB + midW * scale;
        float xE = xA + w;
        float xD = xE - edgeR * scale;

        float yB = yA + edgeT * scale;
        float yC = yB + midH * scale;
        float yE = yA + h;
        float yD = yE - edgeD * scale;

        int maxMidW = u1 - u0 - edgeL - edgeR;
        int maxMidH = v1 - v0 - edgeT - edgeD;
        if (midW > maxMidW)
            midW = maxMidW;
        if (midH > maxMidH)
            midH = maxMidH;

        float uA = u0 / imgSize;
        float uB = (u0 + edgeL) / imgSize;
        float uC = (u0 + edgeL + midW) / imgSize;
        float uD = (u1 - edgeR) / imgSize;
        float uE = u1 / imgSize;

        float vA = v0 / imgSize;
        float vB = (v0 + edgeT) / imgSize;
        float vC = (v0 + edgeT + midH) / imgSize;
        float vD = (v1 - edgeD) / imgSize;
        float vE = v1 / imgSize;

        drawList.addImage(image, xA, yA, xB, yB, uA, vA, uB, vB, color); // top left
        drawList.addImage(image, xD, yA, xE, yB, uD, vA, uE, vB, color); // top right
        drawList.addImage(image, xA, yD, xB, yE, uA, vD, uB, vE, color); // bottom left
        drawList.addImage(image, xD, yD, xE, yE, uD, vD, uE, vE, color); // bottom right
        infiniteRepeatImage(drawList, image, xB, yA, xC, yB, uB, vA, uC, vB, color); // top
        infiniteRepeatImage(drawList, image, xB, yD, xC, yE, uB, vD, uC, vE, color); // bottom
        infiniteRepeatImage(drawList, image, xA, yB, xB, yC, uA, vB, uB, vC, color); // left
        infiniteRepeatImage(drawList, image, xD, yB, xE, yC, uD, vB, uE, vC, color); // right
        infiniteRepeatImage(drawList, image, xB, yB, xC, yC, uB, vB, uC, vC, color); // middle
    }

    public static void resizeableImage(ImDrawList drawList, int image, int imageSize, int edgeT, int edgeD, int edgeL, int edgeR, float xA, float yA, int w, int h, int u0, int v0, int u1, int v1) {
        resizeableImage(drawList, image, imageSize, edgeT, edgeD, edgeL, edgeR, xA, yA, w, h, u0, v0, u1, v1, 0xFFFFFFFF);
    }

    public static void resizeableImage(ImDrawList drawList, int image, int imageSize, int edge, float xA, float yA, int w, int h, int u0, int v0, int u1, int v1, int color) {
        resizeableImage(drawList, image, imageSize, edge, edge, edge, edge, xA, yA, w, h, u0, v0, u1, v1, color);
    }

    public static void resizeableImage(ImDrawList drawList, int image, int imageSize, int edge, float xA, float yA, int w, int h, int u0, int v0, int u1, int v1) {
        resizeableImage(drawList, image, imageSize, edge, edge, edge, edge, xA, yA, w, h, u0, v0, u1, v1, 0xFFFFFFFF);
    }

    public static void resizeableImage64x(ImDrawList drawList, int image, int imageSize, float x0, float y0, float w, float h, int chunkX, int chunkY, int edgeT, int edgeD, int edgeL, int edgeR, int color) {
        resizeableImage(
                drawList,
                image,
                imageSize * 64,
                edgeT, edgeD, edgeL, edgeR,
                x0, y0,
                (int) w, (int) h,
                chunkX * 64, chunkY * 64, chunkX * 64 + 64, chunkY * 64 + 64,
                color
        );
    }

    public static void resizeableImage64x(ImDrawList drawList, int image, int imageSize, float x0, float y0, float w, float h, int chunkX, int chunkY, int edgeT, int edgeD, int edgeL, int edgeR) {
        resizeableImage64x(drawList, image, imageSize, x0, y0, w, h, chunkX, chunkY, edgeT, edgeD, edgeL, edgeR, 0xFFFFFFFF);
    }

    public static void resizeableImage64x(ImDrawList drawList, int image, int imageSize, float x0, float y0, float w, float h, int chunkX, int chunkY, int edge, int color) {
        resizeableImage64x(drawList, image, imageSize, x0, y0, w, h, chunkX, chunkY, edge, edge, edge, edge, color);
    }

    public static void resizeableImage64x(ImDrawList drawList, int image, int imageSize, float x0, float y0, float w, float h, int chunkX, int chunkY, int edge) {
        resizeableImage64x(drawList, image, imageSize, x0, y0, w, h, chunkX, chunkY, edge, 0xFFFFFFFF);
    }

    public static void text(ImDrawList drawList, float x, float y, String text) {
        ImVec4 col = ImGui.getStyle().getColor(ImGuiCol.Text);
        drawList.addText(x, y, ImGui.colorConvertFloat4ToU32(col.x, col.y, col.z, col.w), text);
    }

//    public static void centeredText(ImDrawList drawList, int x, int y, int color, String text) {
//        drawList.addText(x, y, color, text);
//    }
//
//    public static void centeredText(ImDrawList drawList, int x, int y, String text) {
//        ImVec4 col = ImGui.getStyle().getColor(ImGuiCol.Text);
//        centeredText(drawList, x, y, ImGui.colorConvertFloat4ToU32(col.x, col.y, col.z, col.w), text);
//    }

    public static void icon(ImDrawList drawList, int image, int imgSize, int iconSize, int iconX, int iconY, float x0, float y0, float x1, float y1) {
        drawList.addImage(
                image,
                x0, y0, x1, y1,
                (iconX * iconSize) / (float) imgSize,
                (iconY * iconSize) / (float) imgSize,
                (iconX * iconSize + iconSize) / (float) imgSize,
                (iconY * iconSize + iconSize) / (float) imgSize
        );
    }

    public static void iconWH(ImDrawList drawList, int image, int imgSize, int iconSize, int iconX, int iconY, float x, float y, float w, float h) {
        icon(drawList, image, imgSize, iconSize, iconX, iconY, x, y, x + w, y + h);
    }

    public static void icon(int image, int imgSize, int iconSize, int iconX, int iconY, float width, float height) {
        ImGui.image(
                image,
                width, height,
                (iconX * iconSize) / (float) imgSize,
                (iconY * iconSize) / (float) imgSize,
                (iconX * iconSize + iconSize) / (float) imgSize,
                (iconY * iconSize + iconSize) / (float) imgSize
        );
    }

    public static int getScale() {
        return scale;
    }

    public static void setScale(int scale) {
        ImGuiUtil.scale = scale;
    }

    public static void syncScaleWithMC() {
        setScale((int) MC.getWindow().getGuiScale());
    }
}
