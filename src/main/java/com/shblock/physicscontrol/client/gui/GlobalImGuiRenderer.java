package com.shblock.physicscontrol.client.gui;

import com.shblock.physicscontrol.PhysicsControl;
import imgui.*;
import imgui.callback.ImStrConsumer;
import imgui.callback.ImStrSupplier;
import imgui.flag.*;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import imgui.type.ImBoolean;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.nio.file.Path;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwGetClipboardString;

public class GlobalImGuiRenderer {
    private static Minecraft mc;
    private static long windowPtr;
    private static ImGuiImplGlfw imGuiGlfw;
    private static ImGuiImplGl3 imGuiGl3;
    public static ImGuiIO io;
    private static final String GLSL_VERSION = "#version 110";
    private static final Path FONTS_PATH = ModList.get()
            .getModFileById(PhysicsControl.MODID)
            .getFile()
            .getFilePath()
            .resolve("assets")
            .resolve(PhysicsControl.MODID)
            .resolve("fonts");
    private static final String DEFAULT_FONT_NAME = "unifont.ttf";

    private boolean needClose = false;

    private static GlobalImGuiRenderer instance;

    //new a instance if it hasn't been initialized
    public static void tryInitInstance() {
        if (instance == null) {
            instance = new GlobalImGuiRenderer();
        }
    }

    private GlobalImGuiRenderer() {
        initImGui();
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void initImGui() {
        mc = Minecraft.getInstance();
        windowPtr = mc.getWindow().getWindow();
        imGuiGlfw = new ImGuiImplGlfw();
        imGuiGl3 = new ImGuiImplGl3();

        ImGui.createContext();

        io = ImGui.getIO();
        io.setIniFilename(null); // We don't want to save .ini file
//        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard);
//        io.addConfigFlags(ImGuiConfigFlags.NavEnableSetMousePos);
        io.addConfigFlags(ImGuiConfigFlags.DockingEnable);
        io.addConfigFlags(ImGuiConfigFlags.ViewportsEnable);
        io.setBackendPlatformName("imgui_java_impl_glfw");

        // ------------------------------------------------------------
        // GLFW callbacks to handle user input

        glfwSetKeyCallback(windowPtr, (w, key, scancode, action, mods) -> {
            if (isImGuiOpened()) {
                if (action == GLFW_PRESS) {
                    io.setKeysDown(key, true);
                } else if (action == GLFW_RELEASE) {
                    io.setKeysDown(key, false);
                }

                io.setKeyCtrl(io.getKeysDown(GLFW_KEY_LEFT_CONTROL) || io.getKeysDown(GLFW_KEY_RIGHT_CONTROL));
                io.setKeyShift(io.getKeysDown(GLFW_KEY_LEFT_SHIFT) || io.getKeysDown(GLFW_KEY_RIGHT_SHIFT));
                io.setKeyAlt(io.getKeysDown(GLFW_KEY_LEFT_ALT) || io.getKeysDown(GLFW_KEY_RIGHT_ALT));
                io.setKeySuper(io.getKeysDown(GLFW_KEY_LEFT_SUPER) || io.getKeysDown(GLFW_KEY_RIGHT_SUPER));
            }
            if (!isImGuiOpened() || !io.getWantCaptureKeyboard()) {
                mc.execute(() -> mc.keyboardHandler.keyPress(w, key, scancode, action, mods)); // if ImGui didn't use the event, call mc's key handler
            }
        });

        // isn't used in mc
        glfwSetCharCallback(windowPtr, (w, c) -> {
            if (isImGuiOpened()) {
                if (c != GLFW_KEY_DELETE) {
                    io.addInputCharacter(c);
                }
            }
        });

//        glfwSetMonitorCallback((windowId, event) -> {
//            if (isImGuiOpened()) {
//                imGuiGlfw.monitorCallback(windowId, event);
//                //TODO: add mc's MonitorHandler.onMonitorChange() callback function back?
//            }
//        });

        io.setSetClipboardTextFn(new ImStrConsumer() {
            @Override
            public void accept(final String s) {
                glfwSetClipboardString(windowPtr, s);
            }
        });

        io.setGetClipboardTextFn(new ImStrSupplier() {
            @Override
            public String get() {
                final String clipboardString = glfwGetClipboardString(windowPtr);
                if (clipboardString != null) {
                    return clipboardString;
                } else {
                    return "";
                }
            }
        });

        final ImFontAtlas fontAtlas = io.getFonts();

        short[] glyphRanges = {0x1, Short.MAX_VALUE, 0};

        ImFontConfig fontConfig = new ImFontConfig();
        fontConfig.setPixelSnapH(true);
        fontConfig.setGlyphRanges(glyphRanges);
        String default_font_path = FONTS_PATH.resolve(DEFAULT_FONT_NAME).toString();
        fontAtlas.addFontFromFileTTF(default_font_path, 16, fontConfig);
        fontAtlas.addFontFromFileTTF(default_font_path, 8, fontConfig);
        fontAtlas.addFontFromFileTTF(default_font_path, 24, fontConfig);
        fontAtlas.addFontFromFileTTF(default_font_path, 32, fontConfig);
        PhysicsControl.log(Level.DEBUG, "Loaded default font file: " + DEFAULT_FONT_NAME);

        File[] files = FONTS_PATH.toFile().listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.exists()) {
                    if (file.getName().equals(DEFAULT_FONT_NAME)) {
                        continue;
                    }
                    fontAtlas.addFontFromFileTTF(file.toString(), 16, fontConfig);
                    fontAtlas.addFontFromFileTTF(file.toString(), 8, fontConfig);
                    fontAtlas.addFontFromFileTTF(file.toString(), 24, fontConfig);
                    fontAtlas.addFontFromFileTTF(file.toString(), 32, fontConfig);
                    PhysicsControl.log(Level.DEBUG, "Loaded font file: " + file.getName());
                }
            }
        }
        fontConfig.destroy();

        imGuiGlfw.init(windowPtr, false);
        imGuiGl3.init(GLSL_VERSION);
    }

    private ImGuiBase getCurrentImGuiScreen() {
        return mc.screen instanceof ImGuiBase ? (ImGuiBase) mc.screen : null;
    }

    private boolean isImGuiOpened() {
        return getCurrentImGuiScreen() != null;
    }

    //glfwMouseButtonCallback
    //call ImGui's handler, have to do it this way because mc's handler method has private access
    @SubscribeEvent
    public void onRawMouseEvent(final InputEvent.RawMouseEvent event) {
        if (!isImGuiOpened()) {
            return;
        }

        int button = event.getButton();
        int action = event.getAction();

        final boolean[] mouseDown = new boolean[5];

        mouseDown[0] = button == GLFW_MOUSE_BUTTON_1 && action != GLFW_RELEASE;
        mouseDown[1] = button == GLFW_MOUSE_BUTTON_2 && action != GLFW_RELEASE;
        mouseDown[2] = button == GLFW_MOUSE_BUTTON_3 && action != GLFW_RELEASE;
        mouseDown[3] = button == GLFW_MOUSE_BUTTON_4 && action != GLFW_RELEASE;
        mouseDown[4] = button == GLFW_MOUSE_BUTTON_5 && action != GLFW_RELEASE;

        io.setMouseDown(mouseDown);

        if (!io.getWantCaptureMouse() && mouseDown[1]) {
            ImGui.setWindowFocus(null);
        }

//        System.out.println(io.getWantCaptureMouse());
        if (io.getWantCaptureMouse()) {
            event.setCanceled(true);
        }
    }

    //glfwScrollCallback
    //call ImGui's handler, have to do it this way because mc's handler method has private access
    @SubscribeEvent
    public void onMouseScrollEvent(final GuiScreenEvent.MouseScrollEvent.Pre event) {
        if (!isImGuiOpened()) {
            return;
        }

        imGuiGlfw.scrollCallback(windowPtr, event.getScrollDelta(), event.getScrollDelta());
        if (io.getWantCaptureMouse()) {
            event.setCanceled(true);
        }
    }

    //onGuiInputEvent1 ~ onGuiInputEvent7 are to cancel the Gui event when ImGui want to capture mouse or keyboard

    @SubscribeEvent
    public void onGuiInputEvent1(final GuiScreenEvent.MouseClickedEvent.Pre event) {
        if (isImGuiOpened() && io.getWantCaptureMouse()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onGuiInputEvent2(final GuiScreenEvent.MouseReleasedEvent.Pre event) {
        if (isImGuiOpened() && io.getWantCaptureMouse()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onGuiInputEvent3(final GuiScreenEvent.MouseDragEvent.Pre event) {
        if (isImGuiOpened() && io.getWantCaptureMouse()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onGuiInputEvent4(final GuiScreenEvent.MouseScrollEvent.Pre event) {
        if (isImGuiOpened() && io.getWantCaptureMouse()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onGuiInputEvent5(final GuiScreenEvent.KeyboardKeyPressedEvent.Pre event) {
        if (isImGuiOpened() && io.getWantCaptureKeyboard()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onGuiInputEvent6(final GuiScreenEvent.KeyboardKeyReleasedEvent.Pre event) {
        if (isImGuiOpened() && io.getWantCaptureKeyboard()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onGuiInputEvent7(final GuiScreenEvent.KeyboardCharTypedEvent.Pre event) {
        if (isImGuiOpened() && io.getWantCaptureKeyboard()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onWorldRender(RenderGameOverlayEvent event) { //try to close the Gui by render a empty frame
        if (this.needClose && !isImGuiOpened()) {
            startFrame();
            endFrame();
            this.needClose = false;
        }
    }

    @SubscribeEvent
    public void render(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (isImGuiOpened()) {
            this.needClose = true;
            startFrame();
//            setupDockSpace();
            ImGui.pushStyleColor(ImGuiCol.WindowBg, 15, 15, 15, 240);
            getCurrentImGuiScreen().buildImGui();
            ImGui.popStyleColor(1);
//            ImGui.popStyleColor(1); //pop ImGuiCol.ChildBg in setupDockspace()
            endFrame();
        }
    }

    private void startFrame() {
        imGuiGlfw.newFrame();
        ImGui.newFrame();
    }

    private void endFrame() {
        ImGui.render();
        imGuiGl3.renderDrawData(ImGui.getDrawData());

        if (io.hasConfigFlags(ImGuiConfigFlags.ViewportsEnable)) {
            final long backupWindowPtr = glfwGetCurrentContext();
            ImGui.updatePlatformWindows();
            ImGui.renderPlatformWindowsDefault();
            glfwMakeContextCurrent(backupWindowPtr);
        }
    }

    private void setupDockSpace() { //TODO: fix dock space always want to capture everything!!!
        MainWindow mc_window = mc.getWindow();

        int windowFlags = ImGuiWindowFlags.NoDocking;

        ImGuiViewport mainViewPort = ImGui.getMainViewport();
        ImGui.setNextWindowPos(mc_window.getX(), mc_window.getY());
        ImGui.setNextWindowSize(mc_window.getWidth(), mc_window.getHeight());
        ImGui.setNextWindowViewport(mainViewPort.getID());

        ImGui.pushStyleVar(ImGuiStyleVar.WindowRounding, 0f);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowBorderSize, 0f);
        ImGui.pushStyleColor(ImGuiCol.ChildBg, 15,15, 15, 240);
        ImGui.pushStyleColor(ImGuiCol.DockingEmptyBg, 0, 0, 0, 0);
        ImGui.pushStyleColor(ImGuiCol.WindowBg, 0, 0, 0, 0);

        windowFlags |= ImGuiWindowFlags.NoTitleBar |
                ImGuiWindowFlags.NoCollapse |
                ImGuiWindowFlags.NoResize |
                ImGuiWindowFlags.NoMove |
                ImGuiWindowFlags.NoBringToFrontOnFocus |
                ImGuiWindowFlags.NoNavFocus;

        ImGui.begin("Dockspace", new ImBoolean(true), windowFlags);

        ImGui.dockSpace(ImGui.getID("Dockspace"), 0F, 0F, ImGuiDockNodeFlags.None);

        ImGui.popStyleVar(2);
        ImGui.popStyleColor(2); //Don't pop ImGuiCol.ChildBg

        ImGui.end();
    }
}
