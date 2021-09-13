package com.shblock.physicscontrol.client.gui;

import com.shblock.physicscontrol.PhysicsControl;
import imgui.*;
import imgui.flag.*;
import imgui.type.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class ImGuiImpl {
    private static final Minecraft MC = Minecraft.getInstance();

    private static final int IMAGE = Minecraft.getInstance().getTextureManager().getTexture(new ResourceLocation(PhysicsControl.MODID, "widgets")).getId();
    private static final int IMG_SIZE_64x = 6;
    private static final int IMG_SIZE = IMG_SIZE_64x * 64;
    private static final int WINDOW_EDGE = 4;

    private static final Map<String, Integer> windowFlagsCache = new HashMap<>();

    public static ImDrawList getDrawListForImpl() {
        return ImGui.getBackgroundDrawList(ImGui.getWindowViewport());
    }

    public static void playClickSound() {
        MC.getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1F));
    }

    public static void pushTransparent(int col) {
        ImGui.pushStyleColor(col, 0, 0, 0, 0);
    }

    public static void pushStylesForImpl() {
        pushTransparent(ImGuiCol.WindowBg);
//        ImGui.pushStyleColor(ImGuiCol.TitleBg, 10, 10, 10, 50);
//        ImGui.pushStyleColor(ImGuiCol.TitleBgActive, 41, 74, 122, 50);
//        ImGui.pushStyleColor(ImGuiCol.TitleBgCollapsed, 0, 0, 0, 50);
//        ImGui.pushStyleColor(ImGuiCol.Border, 110, 110, 128, 0);
        ImGui.pushStyleColor(ImGuiCol.Text, 32, 32, 32, 255);
        pushTransparent(ImGuiCol.FrameBg);
        pushTransparent(ImGuiCol.FrameBgHovered);
        pushTransparent(ImGuiCol.FrameBgActive);
        pushTransparent(ImGuiCol.ScrollbarBg);
//        pushTransparent(ImGuiCol.ScrollbarGrab);
//        pushTransparent(ImGuiCol.ScrollbarGrabActive);
//        pushTransparent(ImGuiCol.ScrollbarGrabHovered);
        pushTransparent(ImGuiCol.CheckMark);
        ImGui.pushStyleColor(ImGuiCol.SliderGrab, 0xFF6E6E6E);
        ImGui.pushStyleColor(ImGuiCol.SliderGrabActive, 0xFF5E5E5E);
        pushTransparent(ImGuiCol.Button);
        pushTransparent(ImGuiCol.ButtonHovered);
        pushTransparent(ImGuiCol.ButtonActive);
        pushTransparent(ImGuiCol.Header);
        pushTransparent(ImGuiCol.HeaderHovered);
        pushTransparent(ImGuiCol.HeaderActive);
        pushTransparent(ImGuiCol.Separator);
//        pushTransparent(ImGuiCol.SeparatorHovered);
//        pushTransparent(ImGuiCol.SeparatorActive);
        pushTransparent(ImGuiCol.ResizeGrip);
        pushTransparent(ImGuiCol.ResizeGripHovered);
        pushTransparent(ImGuiCol.ResizeGripActive);
        pushTransparent(ImGuiCol.Tab);
        pushTransparent(ImGuiCol.TabHovered);
        pushTransparent(ImGuiCol.TabActive);
        pushTransparent(ImGuiCol.TabUnfocused);
        pushTransparent(ImGuiCol.TabUnfocusedActive);
        pushTransparent(ImGuiCol.ModalWindowDimBg);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowBorderSize, 0F);
        ImGui.pushStyleVar(ImGuiStyleVar.ChildBorderSize, 0F);
        ImGui.pushStyleVar(ImGuiStyleVar.PopupBorderSize, 0F);
        ImGui.pushStyleVar(ImGuiStyleVar.FrameBorderSize, 0F);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowRounding, 0F);
        ImGui.pushStyleVar(ImGuiStyleVar.ChildRounding, 0F);
        ImGui.pushStyleVar(ImGuiStyleVar.FrameRounding, 0F);
        ImGui.pushStyleVar(ImGuiStyleVar.PopupRounding, 0F);
        ImGui.pushStyleVar(ImGuiStyleVar.ScrollbarRounding, 0F);
        ImGui.pushStyleVar(ImGuiStyleVar.GrabRounding, 0F);
        ImGui.pushStyleVar(ImGuiStyleVar.TabRounding, 0F);
    }

    public static void popStylesForImpl() {
        ImGui.popStyleVar(11);
        ImGui.popStyleColor(25);
    }

    private static int setupWindowFlagsWithBg(int flags) {
        if (ImGui.getStateStorage().getBool(0)) {
            flags |= ImGuiWindowFlags.NoResize |
                    ImGuiWindowFlags.NoDocking |
                    ImGuiWindowFlags.NoScrollbar |
                    ImGuiWindowFlags.NoScrollWithMouse |
                    ImGuiWindowFlags.NoNav |
                    ImGuiWindowFlags.NoNavInputs |
                    ImGuiWindowFlags.NoNavFocus;
        }
        return flags;
    }

    private static boolean nextWindowCollapsed = false;

    public static void setNextWindowCollapsed() {
        nextWindowCollapsed = true;
    }

    private static boolean iBeginWindowWithBg(String s, @Nullable ImBoolean pOpen, int flags, BiFunction<String, Integer, Boolean> windowBeginner) {
        int orgFlags = flags;
//        flags |= ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoBackground;
        flags |= ImGuiWindowFlags.NoTitleBar;
        pushStylesForImpl();
        boolean result = windowBeginner.apply(s, windowFlagsCache.getOrDefault(s, flags));
        windowFlagsCache.put(s, setupWindowFlagsWithBg(flags));
        return result && setupBeginWithBg(s, orgFlags, pOpen);
    }

    private static boolean pushedClipRect = false;

    private static boolean setupBeginWithBg(String s, int flags, @Nullable ImBoolean pOpen) {
        ImGuiStorage storage = ImGui.getStateStorage();
        if (nextWindowCollapsed) {
            storage.setBool(0, true);
            nextWindowCollapsed = false;
        }
        ImDrawList drawList = getDrawListForImpl();
        float x = ImGui.getWindowPosX();
        float y = ImGui.getWindowPosY();
        float width = ImGui.getWindowSizeX();
        float height = ImGui.getWindowSizeY();
        ImGuiUtil.resizeableImage64x(
                drawList,
                IMAGE,
                IMG_SIZE_64x,
                x, y,
                width, height,
                0,
                ImGuiUtil.isInMainViewport() ? 0 : 1,
                WINDOW_EDGE
        );
//        ImGui.pushClipRect(x, y, x + width, y + height, true);
//        drawList.pushClipRect(x, y, x + width, y + height, true);

        boolean collapsed = false;
        if ((flags & ImGuiWindowFlags.NoTitleBar) == 0) {
            int iconSize = 16 * 2;
            collapsed = storage.getBool(0, false);

            ImGuiUtil.text(drawList, ImGui.getWindowPosX() + iconSize, ImGui.getWindowPosY() + 8, s.split("###")[0]);

            boolean closeButtonHovered;
            boolean titleHovered = false;
            boolean collapseButtonHovered = false;
            if (pOpen != null) {
                float x1 = ImGui.getWindowPosX() + ImGui.getWindowWidth();
                float x0 = x1 - iconSize;
                float y0 = ImGui.getWindowPosY();
                float y1 = y0 + iconSize;
                closeButtonHovered = ImGui.isMouseHoveringRect(x0, y0, x1, y1);
                if (!closeButtonHovered) {
                    titleHovered = ImGui.isMouseHoveringRect(
                            ImGui.getWindowPosX(),
                            ImGui.getWindowPosY(),
                            ImGui.getWindowPosX() + ImGui.getWindowWidth(),
                            ImGui.getWindowPosY() + iconSize
                    );
                    collapseButtonHovered = ImGui.isMouseHoveringRect(
                            ImGui.getWindowPosX(),
                            ImGui.getWindowPosY(),
                            ImGui.getWindowPosX() + iconSize,
                            ImGui.getWindowPosY() + iconSize
                    );
                }
                if (closeButtonHovered) {
                    ImGui.captureMouseFromApp(true);
                    if (ImGui.isMouseReleased(ImGuiMouseButton.Left)) {
                        playClickSound();
                        pOpen.set(false);
                        windowFlagsCache.remove(s);
                    }
                }
                ImGuiUtil.icon(
                        drawList, IMAGE, IMG_SIZE, 16,
                        closeButtonHovered ? (ImGui.isMouseDown(ImGuiMouseButton.Left) ? 2 : 1) : 0,
                        17,
                        x0, y0, x1, y1
                );
            }

            ImGuiUtil.iconWH(
                    drawList, IMAGE, IMG_SIZE, 16, collapsed ? 1 : 3, collapseButtonHovered ? 19 : 18,
                    ImGui.getWindowPosX(),
                    ImGui.getWindowPosY(),
                    iconSize, iconSize
            );

            if ((collapseButtonHovered && ImGui.isMouseReleased(ImGuiMouseButton.Left)) || (titleHovered && ImGui.isMouseDoubleClicked(ImGuiMouseButton.Left))) {
                if ((flags & ImGuiWindowFlags.NoCollapse) == 0){
                    playClickSound();
                    if (collapsed) { // reset size
                        ImGui.setWindowSize(ImGui.getWindowWidth(), storage.getFloat(1));
                    } else { // store size
                        storage.setFloat(1, ImGui.getWindowHeight());
                        ImGui.setWindowSize(ImGui.getWindowWidth(), iconSize);
                    }
                    collapsed = !collapsed;
                    storage.setBool(0, collapsed);
                }
            }

            if (!collapsed) {
                ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 0F, 0F);
                ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 0F, 0F);
                ImGui.invisibleButton("##WINDOW_TITLE_SPACING", 1, iconSize);
                ImGui.popStyleVar(2);
                drawSeparator(drawList, ImGui.getWindowPosY() + iconSize);

                float cx = x;
                float cy = ImGui.getWindowPosY() + iconSize + ImGuiUtil.getScale() * 2;
                float xm = x + width;
                float ym = y + height;
                pushedClipRect = true;
                ImGui.pushClipRect(cx, cy, xm, ym, true);
                getDrawListForImpl().pushClipRect(cx, cy, xm, ym, true);
            }
        }

        return !collapsed;
    }

    public static boolean beginWithBg(String s) {
        return beginWithBg(s, ImGuiWindowFlags.None);
    }
    public static boolean beginWithBg(String s, ImBoolean pOpen) {
        return beginWithBg(s, pOpen, ImGuiWindowFlags.None);
    }
    public static boolean beginWithBg(String s, int flags) {
        return beginWithBg(s, null, flags);
    }
    public static boolean beginWithBg(String s, @Nullable ImBoolean pOpen, int flags) {
        return iBeginWindowWithBg(s, pOpen, flags, ImGui::begin);
    }

//    public static boolean beginChildWithBg(String s) {
//        return iBeginWindowWithBg(s, null, ImGuiWindowFlags.None, (text, flgs) -> ImGui.beginChild(text));
//    }
//
//    public static boolean beginChildWithBg(String s, ImBoolean pOpen) {
//        return iBeginWindowWithBg(s, pOpen, ImGuiWindowFlags.None, (text, flgs) -> ImGui.beginChild(text));
//    }
//
//    public static boolean beginChildWithBg(String s, int flags) {
//        return iBeginWindowWithBg(s, null, flags, (text, flgs) -> ImGui.beginChild(text, 0, 0, false, flgs));
//    }
//
//    public static boolean beginChildWithBg(String s, @Nullable ImBoolean pOpen, int flags) {
//        return iBeginWindowWithBg(s, pOpen, flags, (text, flgs) -> ImGui.beginChild(text, 0, 0, false, flgs));
//    }

//    public static boolean beginChildWithBg(String s, float width, float height) {
//        return iBeginWindowWithBg(s, null, ImGuiWindowFlags.None, (text, flgs) -> ImGui.beginChild(text, width, height));
//    }

//    public static boolean beginChildWithBg(String s, float width, float height, int flags) {
//        return iBeginWindowWithBg(s, null, flags, (text, flgs) -> ImGui.beginChild(text, width, height, false, flgs));
//    }
//
//    public static boolean beginChildWithBg(String s, float width, float height, @Nullable ImBoolean pOpen, int flags) {
//        return iBeginWindowWithBg(s, pOpen, flags, (text, flgs) -> ImGui.beginChild(text, width, height, false, flgs));
//    }

    private static void iEndWindowWithBg(Runnable endFunction) {
        if (pushedClipRect) {
            getDrawListForImpl().popClipRect();
            ImGui.popClipRect();
            pushedClipRect = false;
        }
        endFunction.run();
        popStylesForImpl();
    }

    public static void endWithBg() {
        iEndWindowWithBg(ImGui::end);
    }

//    public static void endChildWithBg() {
//        iEndWindowWithBg(ImGui::endChild);
//    }

    private static void iDrawButton(ImDrawList drawList, float x, float y, float width, float height, boolean hovered, boolean active, int color) {
        int chunkY = 2;
        if (active) {
            chunkY = 4;
        } else if (hovered) {
            chunkY = 3;
        }

        if (chunkY != 4) {
            ImGuiUtil.resizeableImage64x(
                    drawList, IMAGE, IMG_SIZE_64x,
                    x,
                    y,
                    width,
                    height,
                    1, chunkY,
                    2, 3, 2, 2,
                    color
            );
        } else {
            ImGuiUtil.resizeableImage64x(
                    drawList, IMAGE, IMG_SIZE_64x,
                    x,
                    y,
                    width,
                    height,
                    1, chunkY,
                    1,
                    color
            );
        }
    }
    private static void iDrawButton(ImDrawList drawList, float x, float y, float width, float height, boolean hovered, boolean active) {
        iDrawButton(drawList, x, y, width, height, hovered, active, 0xFFFFFFFF);
    }

    private static boolean iButton(ImDrawList drawList, Supplier<Boolean> button, int color) {
        boolean result = button.get();

        if (result)
            playClickSound();

        if (ImGui.isItemVisible()) {
            iDrawButton(
                    drawList,
                    ImGui.getItemRectMinX(),
                    ImGui.getItemRectMinY(),
                    ImGui.getItemRectSizeX(),
                    ImGui.getItemRectSizeY(),
                    ImGui.isItemHovered(),
                    ImGui.isItemActive(),
                    color
            );
        }

        return result;
    }
    private static boolean iButton(ImDrawList drawList, Supplier<Boolean> button) {
        return iButton(drawList, button, 0xFFFFFFFF);
    }

    public static boolean button(ImDrawList drawList, String s) {
        return iButton(drawList, () -> ImGui.button(s));
    }
    public static boolean button(ImDrawList drawList, String s, float width, float height) {
        return iButton(drawList, () -> ImGui.button(s, width, height));
    }

    public static boolean imageButton(ImDrawList drawList, int image, float width, float height) {
        return iButton(drawList, () -> ImGui.imageButton(image, width, height));
    }
    public static boolean imageButton(ImDrawList drawList, int image, float width, float height, float u, float v) {
        return iButton(drawList, () -> ImGui.imageButton(image, width, height, u, v));
    }
    public static boolean imageButton(ImDrawList drawList, int image, float width, float height, float u0, float v0, float u1, float v1) {
        return iButton(drawList, () -> ImGui.imageButton(image, width, height, u0, v0, u1, v1));
    }
    public static boolean imageButton(ImDrawList drawList, int image, float width, float height, float u0, float v0, float u1, float v1, int framePadding) {
        return iButton(drawList, () -> ImGui.imageButton(image, width, height, u0, v0, u1, v1, framePadding));
    }
    public static boolean imageButton(ImDrawList drawList, int image, float width, float height, float u0, float v0, float u1, float v1, int framePadding, float bgR, float bgG, float bgB, float bgA) {
        return iButton(drawList, () -> ImGui.imageButton(image, width, height, u0, v0, u1, v1, framePadding, bgR, bgG, bgB, bgA), ImColor.floatToColor(bgR, bgG, bgB, bgA));
    }
    public static boolean imageButton(ImDrawList drawList, int image, float width, float height, float u0, float v0, float u1, float v1, int framePadding, float bgR, float bgG, float bgB, float bgA, float tintR, float tintG, float tintB, float tintA) {
        return iButton(drawList, () -> ImGui.imageButton(image, width, height, u0, v0, u1, v1, framePadding, bgR, bgG, bgB, bgA, tintR, tintG, tintB, tintA), ImColor.floatToColor(bgR, bgG, bgB, bgA));
    }
    public static boolean imageButton(ImDrawList drawList, int image, float width, float height, float u0, float v0, float u1, float v1, int framePadding, ImVec4 bgColor) {
        return imageButton(drawList, image, width, height, u0, v0, u1, v1, framePadding, bgColor.x, bgColor.y, bgColor.z, bgColor.w);
    }
    public static boolean imageButton(ImDrawList drawList, int image, float width, float height, float u0, float v0, float u1, float v1, int framePadding, ImVec4 bgColor, ImVec4 tintColor) {
        return imageButton(drawList, image, width, height, u0, v0, u1, v1, framePadding, bgColor.x, bgColor.y, bgColor.z, bgColor.w, tintColor.x, tintColor.y, tintColor.z, tintColor.w);
    }

    private static boolean iSelector(ImDrawList drawList, boolean selected, Supplier<Boolean> button) {
        boolean result = button.get();

        float x = ImGui.getItemRectMinX();
        float y = ImGui.getItemRectMinY();
        float width = ImGui.getItemRectSizeX();
        float height = ImGui.getItemRectSizeY();

        if (ImGui.isItemVisible()) {
            ImGuiUtil.resizeableImage64x(
                    drawList, IMAGE, IMG_SIZE_64x,
                    x, y, width, height,
                    0,
                    ImGui.isItemHovered() ? 3 : 2,
                    4
            );
        }
        if (selected) {
            ImGuiUtil.resizeableImage64x(
                    drawList, IMAGE, IMG_SIZE_64x,
                    x, y, width, height,
                    1,
                    ImGui.isItemHovered() ? 1 : 0,
                    4
            );
        }

        return result;
    }

    public static boolean selector(ImDrawList drawList, boolean selected, int image, float width, float height) {
        return iSelector(drawList, selected, () -> ImGui.imageButton(image, width, height));
    }
    public static boolean selector(ImDrawList drawList, boolean selected, int image, float width, float height, float u0, float v0) {
        return iSelector(drawList, selected, () -> ImGui.imageButton(image, width, height, u0, v0));
    }
    public static boolean selector(ImDrawList drawList, boolean selected, int image, float width, float height, float u0, float v0, float u1, float v1) {
        return iSelector(drawList, selected, () -> ImGui.imageButton(image, width, height, u0, v0, u1, v1));
    }
    public static boolean selector(ImDrawList drawList, boolean selected, int image, float width, float height, float u0, float v0, float u1, float v1, int framePadding) {
        return iSelector(drawList, selected, () -> ImGui.imageButton(image, width, height, u0, v0, u1, v1, framePadding));
    }
    public static boolean selector(ImDrawList drawList, boolean selected, int image, float width, float height, float u0, float v0, float u1, float v1, int framePadding, float tintR, float tintG, float tintB, float tintA) {
        return iSelector(drawList, selected, () -> ImGui.imageButton(image, width, height, u0, v0, u1, v1, framePadding, 1F, 1F, 1F, 1F, tintR, tintG, tintB, tintA));
    }
    public static boolean selector(ImDrawList drawList, boolean selected, int image, float width, float height, float u0, float v0, float u1, float v1, int framePadding, ImVec4 tint) {
        return iSelector(drawList, selected, () -> ImGui.imageButton(image, width, height, u0, v0, u1, v1, framePadding, 1F, 1F, 1F, 1F, tint.x, tint.y, tint.z, tint.w));
    }

    public static boolean checkbox(ImDrawList drawList, String s, boolean b) {
        boolean result = ImGui.checkbox(s, b);

        if (result)
            playClickSound();

        if (ImGui.isItemVisible()) {
            int chunkY = 2;
            if (ImGui.isItemActive()) {
                chunkY = 4;
            } else if (ImGui.isItemHovered()) {
                chunkY = 3;
            }

            float size = ImGui.getItemRectSizeY();
            float x0 = ImGui.getItemRectMinX();
            float y0 = ImGui.getItemRectMinY();
            float x1 = ImGui.getItemRectMinX() + size;
            float y1 = ImGui.getItemRectMaxY();
            if (chunkY != 4) {
                ImGuiUtil.resizeableImage64x(
                        drawList, IMAGE, IMG_SIZE_64x,
                        x0, y0, size, size,
                        4, chunkY,
                        2, 3, 2, 2
                );
            } else {
                ImGuiUtil.resizeableImage64x(
                        drawList, IMAGE, IMG_SIZE_64x,
                        x0, y0, size, size,
                        4, chunkY,
                        1
                );
            }

            if (result ? !b : b) {
                ImGuiUtil.icon(
                        drawList, IMAGE, IMG_SIZE, 16,
                        0, 16,
                        x0, y0, x1, y1
                );
            }
        }

        return result;
    }
    public static boolean checkbox(ImDrawList drawList, String s, ImBoolean b) {
        boolean result = checkbox(drawList, s, b.get());
        if (result) {
            b.set(!b.get());
        }

        return result;
    }

    private static boolean iSelectable(ImDrawList drawList, Supplier<Boolean> selectable, boolean state) {
        ImGui.pushStyleColor(ImGuiCol.Text, 200, 200, 200, 255);
        boolean result = selectable.get();
        ImGui.popStyleColor(1);

        if (result) {
            state = !state;
            playClickSound();
        }

        if (ImGui.isItemVisible()) {
            boolean hovered = ImGui.isItemHovered();
            int chunkY = state ? 0 : 1;
            if (hovered) {
                chunkY += 2;
            }
            if (ImGui.isItemActive()) {
                chunkY = 4;
            }

            ImGuiUtil.resizeableImage64x(
                    drawList, IMAGE, IMG_SIZE_64x,
                    ImGui.getItemRectMinX(),
                    ImGui.getItemRectMinY(),
                    ImGui.getItemRectSizeX(),
                    ImGui.getItemRectSizeY(),
                    2, chunkY,
                    1
            );
        }

        return result;
    }

    public static boolean selectable(ImDrawList drawList, String s) {
        return iSelectable(drawList, () -> ImGui.selectable(s), true);
    }
    public static boolean selectable(ImDrawList drawList, String s, boolean b) {
        return iSelectable(drawList, () -> ImGui.selectable(s, b), b);
    }
    public static boolean selectable(ImDrawList drawList, String s, boolean b, int flags) {
        return iSelectable(drawList, () -> ImGui.selectable(s, b, flags), b);
    }
    public static boolean selectable(ImDrawList drawList, String s, boolean b, int flags, float width, float height) {
        return iSelectable(drawList, () -> ImGui.selectable(s, b, flags, width, height), b);
    }
    public static boolean selectable(ImDrawList drawList, String s, ImBoolean b) {
        return iSelectable(drawList, () -> ImGui.selectable(s, b), b.get());
    }
    public static boolean selectable(ImDrawList drawList, String s, ImBoolean b, int flags) {
        return iSelectable(drawList, () -> ImGui.selectable(s, b, flags), b.get());
    }
    public static boolean selectable(ImDrawList drawList, String s, ImBoolean b, int flags, float width, float height) {
        return iSelectable(drawList, () -> ImGui.selectable(s, b, flags, width, height), b.get());
    }

    public static void separator(ImDrawList drawList) {
        ImGui.separator();
        ImGuiUtil.resizeableImage64x(
                drawList, IMAGE, IMG_SIZE_64x,
                ImGui.getItemRectMinX() + ImGuiUtil.getScale(),
                ImGui.getItemRectMinY(),
                ImGui.getItemRectSizeX() - ImGuiUtil.getScale() * 2,
                ImGuiUtil.getScale() * 2,
                5, 2,
                1
        );
    }

    private static void drawSeparator(ImDrawList drawList, float x, float y, float width, float height) {
        ImGuiUtil.resizeableImage64x(
                drawList, IMAGE, IMG_SIZE_64x,
                x + ImGuiUtil.getScale(), y, width - ImGuiUtil.getScale() * 2, height,
                5, 2,
                1
        );
    }
    private static void drawSeparator(ImDrawList drawList, float x, float y, float width) {
        drawSeparator(drawList, x, y, width, ImGuiUtil.getScale() * 2);
    }
    private static void drawSeparator(ImDrawList drawList, float y) {
        drawSeparator(drawList, ImGui.getWindowPosX(), y, ImGui.getWindowWidth());
    }

    public static boolean collapsingHeader(ImDrawList drawList, String s) {
        ImGui.pushStyleVar(ImGuiStyleVar.FramePadding, ImGui.getStyle().getFramePaddingX(), 8F);
        ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, ImGui.getStyle().getItemSpacingX(), ImGuiUtil.getScale());
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 0F, ImGui.getStyle().getWindowPaddingY());
        boolean result = ImGui.collapsingHeader(s, ImGuiTreeNodeFlags.SpanFullWidth);
        ImGui.popStyleVar(3);

        if (ImGui.isItemToggledOpen())
            playClickSound();

        ImGuiUtil.resizeableImage64x(
                drawList, IMAGE, IMG_SIZE_64x,
                ImGui.getWindowPosX(),
                ImGui.getItemRectMinY(),
                ImGui.getWindowSizeX(),
                ImGui.getItemRectSizeY(),
                result ? 5 : 0,
                result ? 3 : 0,
                WINDOW_EDGE
        );

        if (result) {
            drawSeparator(drawList, ImGui.getItemRectMaxY() - ImGuiUtil.getScale() * 2);
        }

        return result;
    }

    private static boolean iSlider(ImDrawList drawList, Supplier<Boolean> slider) {
        ImGui.pushStyleColor(ImGuiCol.Text, 200, 200, 200, 255);
        boolean result = slider.get();
        ImGui.popStyleColor(1);

        ImGuiUtil.resizeableImage64x(
                drawList, IMAGE, IMG_SIZE_64x,
                ImGui.getItemRectMinX(),
                ImGui.getItemRectMinY(),
                ImGui.getItemRectSizeX(),
                ImGui.getItemRectSizeY(),
                4, 2,
                1
        );

        if (ImGui.isItemDeactivatedAfterEdit())
            playClickSound();

        return result;
    }

    public static boolean sliderScalar(ImDrawList drawList, String label, int dataType, ImInt v, int vMin, int vMax) {
        return iSlider(drawList, () -> ImGui.sliderScalar(label, dataType, v, vMin, vMax));
    }
    public static boolean sliderScalar(ImDrawList drawList, String label, int dataType, ImInt v, int vMin, int vMax, String format) {
        return iSlider(drawList, () -> ImGui.sliderScalar(label, dataType, v, vMin, vMax, format));
    }
    public static boolean sliderScalar(ImDrawList drawList, String label, int dataType, ImInt v, int vMin, int vMax, String format, int imGuiSliderFlags) {
        return iSlider(drawList, () -> ImGui.sliderScalar(label, dataType, v, vMin, vMax, format, imGuiSliderFlags));
    }
    public static boolean sliderScalar(ImDrawList drawList, String label, int dataType, ImFloat v, float vMin, float vMax) {
        return iSlider(drawList, () -> ImGui.sliderScalar(label, dataType, v, vMin, vMax));
    }
    public static boolean sliderScalar(ImDrawList drawList, String label, int dataType, ImFloat v, float vMin, float vMax, String format) {
        return iSlider(drawList, () -> ImGui.sliderScalar(label, dataType, v, vMin, vMax, format));
    }
    public static boolean sliderScalar(ImDrawList drawList, String label, int dataType, ImFloat v, float vMin, float vMax, String format, int imGuiSliderFlags) {
        return iSlider(drawList, () -> ImGui.sliderScalar(label, dataType, v, vMin, vMax, format, imGuiSliderFlags));
    }
    public static boolean sliderScalar(ImDrawList drawList, String label, int dataType, ImLong v, long vMin, long vMax) {
        return iSlider(drawList, () -> ImGui.sliderScalar(label, dataType, v, vMin, vMax));
    }
    public static boolean sliderScalar(ImDrawList drawList, String label, int dataType, ImLong v, long vMin, long vMax, String format) {
        return iSlider(drawList, () -> ImGui.sliderScalar(label, dataType, v, vMin, vMax, format));
    }
    public static boolean sliderScalar(ImDrawList drawList, String label, int dataType, ImLong v, long vMin, long vMax, String format, int imGuiSliderFlags) {
        return iSlider(drawList, () -> ImGui.sliderScalar(label, dataType, v, vMin, vMax, format, imGuiSliderFlags));
    }
    public static boolean sliderScalar(ImDrawList drawList, String label, int dataType, ImDouble v, double vMin, double vMax) {
        return iSlider(drawList, () -> ImGui.sliderScalar(label, dataType, v, vMin, vMax));
    }
    public static boolean sliderScalar(ImDrawList drawList, String label, int dataType, ImDouble v, double vMin, double vMax, String format) {
        return iSlider(drawList, () -> ImGui.sliderScalar(label, dataType, v, vMin, vMax, format));
    }
    public static boolean sliderScalar(ImDrawList drawList, String label, int dataType, ImDouble v, double vMin, double vMax, String format, int imGuiSliderFlags) {
        return iSlider(drawList, () -> ImGui.sliderScalar(label, dataType, v, vMin, vMax, format, imGuiSliderFlags));
    }
    public static boolean sliderScalar(ImDrawList drawList, String label, int dataType, ImShort v, short vMin, short vMax) {
        return iSlider(drawList, () -> ImGui.sliderScalar(label, dataType, v, vMin, vMax));
    }
    public static boolean sliderScalar(ImDrawList drawList, String label, int dataType, ImShort v, short vMin, short vMax, String format) {
        return iSlider(drawList, () -> ImGui.sliderScalar(label, dataType, v, vMin, vMax, format));
    }
    public static boolean sliderScalar(ImDrawList drawList, String label, int dataType, ImShort v, short vMin, short vMax, String format, int imGuiSliderFlags) {
        return iSlider(drawList, () -> ImGui.sliderScalar(label, dataType, v, vMin, vMax, format, imGuiSliderFlags));
    }

    private static void iDrawMultipartWidget(ImDrawList drawList, float x, float y, float width, float height, int parts, int chunkX, int chunkY, int edge) {
        float innerSpacing = ImGui.getStyle().getItemInnerSpacingX();
        float prePartWidth = (width - innerSpacing * parts) / parts;
        float currentX = x;
        for (int i=0; i<parts; i++) {
            ImGuiUtil.resizeableImage64x(
                    drawList, IMAGE, IMG_SIZE_64x,
                    currentX,
                    y,
                    prePartWidth,
                    height,
                    chunkX, chunkY,
                    edge
            );
            currentX += prePartWidth + innerSpacing;
        }
    }

    private static boolean iInput(ImDrawList drawList, Supplier<Boolean> widget, int parts, boolean hasButton, int flags) {
        ImGui.pushStyleColor(ImGuiCol.Text, 200, 200, 200, 255);
        boolean result = widget.get();
        ImGui.popStyleColor(1);

        if (!hasButton) {
            if (result)
                playClickSound();
        } else {
            if (ImGui.isItemDeactivatedAfterEdit())
                playClickSound();
        }

        float x = ImGui.getItemRectMinX();
        float y = ImGui.getItemRectMinY();
        float width = ImGui.getItemRectSizeX();
        float height = ImGui.getItemRectSizeY();

        if (parts == 1) {
            if (hasButton) {
                float innerSpacing = ImGui.getStyle().getItemInnerSpacingX();
                float boxWidth = width - (height + innerSpacing) * 2;
                ImGuiUtil.resizeableImage64x(
                        drawList, IMAGE, IMG_SIZE_64x,
                        x,
                        y,
                        boxWidth,
                        height,
                        4, 3,
                        1
                );

                float currentX = x + boxWidth;
                for (int i=0; i<2; i++) {
                    currentX += innerSpacing;
                    boolean hovered = ImGui.isMouseHoveringRect(currentX, y, currentX + height, y + height);
                    boolean active = hovered && ImGui.isItemActive();
                    iDrawButton(
                            drawList,
                            currentX,
                            y,
                            height,
                            height,
                            hovered,
                            active
                    );
                    currentX += height;
                }
            } else {
                ImGuiUtil.resizeableImage64x(
                        drawList, IMAGE, IMG_SIZE_64x,
                        x,
                        y,
                        width,
                        height,
                        4, 3,
                        1
                );
            }
        } else {
            iDrawMultipartWidget(drawList, x, y, width, height, parts, 4, 3, 1);
        }

        return result;
    }

    private static boolean iInput(ImDrawList drawList, Supplier<Boolean> widget, int flags) {
        return iInput(drawList, widget, 1, false, flags);
    }

    private static boolean iInput(ImDrawList drawList, Supplier<Boolean> widget, int parts, int flags) {
        return iInput(drawList, widget, parts, false, flags);
    }

    public static boolean inputText(ImDrawList drawList, String label, ImString text) {
        return iInput(drawList, () -> ImGui.inputText(label, text), ImGuiInputTextFlags.None);
    }
    public static boolean inputText(ImDrawList drawList, String label, ImString text, int flags) {
        return iInput(drawList, () -> ImGui.inputText(label, text, flags), flags);
    }

    public static boolean inputFloat(ImDrawList drawList, String label, ImFloat value) {
        return iInput(drawList, () -> ImGui.inputFloat(label, value), 1, true, ImGuiInputTextFlags.None);
    }
    public static boolean inputFloat(ImDrawList drawList, String label, ImFloat value, float step) {
        return iInput(drawList, () -> ImGui.inputFloat(label, value, step), 1, true, ImGuiInputTextFlags.None);
    }
    public static boolean inputFloat(ImDrawList drawList, String label, ImFloat value, float step, float stepFast) {
        return iInput(drawList, () -> ImGui.inputFloat(label, value, step, stepFast), 1, true, ImGuiInputTextFlags.None);
    }
    public static boolean inputFloat(ImDrawList drawList, String label, ImFloat value, float step, float stepFast, String format) {
        return iInput(drawList, () -> ImGui.inputFloat(label, value, step, stepFast, format), 1, true, ImGuiInputTextFlags.None);
    }
    public static boolean inputFloat(ImDrawList drawList, String label, ImFloat value, float step, float stepFast, String format, int flags) {
        return iInput(drawList, () -> ImGui.inputFloat(label, value, step, stepFast, format, flags), 1, true, flags);
    }

    public static boolean inputFloat2(ImDrawList drawList, String s, float[] values) {
        return iInput(drawList, () -> ImGui.inputFloat2(s, values), 2, ImGuiInputTextFlags.None);
    }
    public static boolean inputFloat2(ImDrawList drawList, String s, float[] values, String format) {
        return iInput(drawList, () -> ImGui.inputFloat2(s, values, format), 2, ImGuiInputTextFlags.None);
    }
    public static boolean inputFloat2(ImDrawList drawList, String s, float[] values, String format, int flags) {
        return iInput(drawList, () -> ImGui.inputFloat2(s, values, format, flags), 2, flags);
    }

    private static boolean iDrag(ImDrawList drawList, Supplier<Boolean> widget, int parts) {
        ImGui.pushStyleColor(ImGuiCol.Text, 200, 200, 200, 255);
        boolean result = widget.get();
        ImGui.popStyleColor(1);

        if (ImGui.isItemActive())
            ImGui.setMouseCursor(ImGuiMouseCursor.ResizeEW);

        iDrawMultipartWidget(
                drawList,
                ImGui.getItemRectMinX(),
                ImGui.getItemRectMinY(),
                ImGui.getItemRectSizeX(),
                ImGui.getItemRectSizeY(),
                parts,
                ImGui.isItemHovered() ? 4 : 3,
                4,
                1
        );

        if (ImGui.isItemDeactivatedAfterEdit())
            playClickSound();

        return result;
    }

    public static boolean dragFloat(ImDrawList drawList, String s, float[] floats) {
        return iDrag(drawList, () -> ImGui.dragFloat(s, floats), 1);
    }
    public static boolean dragFloat(ImDrawList drawList, String s, float[] floats, float speed) {
        return iDrag(drawList, () -> ImGui.dragFloat(s, floats, speed), 1);
    }
    public static boolean dragFloat(ImDrawList drawList, String s, float[] floats, float speed, float min, float max) {
        return iDrag(drawList, () -> ImGui.dragFloat(s, floats, speed, min, max), 1);
    }
    public static boolean dragFloat(ImDrawList drawList, String s, float[] floats, float speed, float min, float max, String format) {
        return iDrag(drawList, () -> ImGui.dragFloat(s, floats, speed, min, max, format), 1);
    }
    public static boolean dragFloat(ImDrawList drawList, String s, float[] floats, float speed, float min, float max, String format, int flags) {
        return iDrag(drawList, () -> ImGui.dragFloat(s, floats, speed, min, max, format, flags), 1);
    }

    public static boolean dragFloat2(ImDrawList drawList, String s, float[] floats) {
        return iDrag(drawList, () -> ImGui.dragFloat2(s, floats), 2);
    }
    public static boolean dragFloat2(ImDrawList drawList, String s, float[] floats, float speed) {
        return iDrag(drawList, () -> ImGui.dragFloat2(s, floats, speed), 2);
    }
    public static boolean dragFloat2(ImDrawList drawList, String s, float[] floats, float speed, float min, float max) {
        return iDrag(drawList, () -> ImGui.dragFloat2(s, floats, speed, min, max), 2);
    }
    public static boolean dragFloat2(ImDrawList drawList, String s, float[] floats, float speed, float min, float max, String format) {
        return iDrag(drawList, () -> ImGui.dragFloat2(s, floats, speed, min, max, format), 2);
    }
    public static boolean dragFloat2(ImDrawList drawList, String s, float[] floats, float speed, float min, float max, String format, int flags) {
        return iDrag(drawList, () -> ImGui.dragFloat2(s, floats, speed, min, max, format, flags), 2);
    }
}
