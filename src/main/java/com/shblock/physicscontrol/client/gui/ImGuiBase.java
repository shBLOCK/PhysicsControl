package com.shblock.physicscontrol.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
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
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.Level;
import org.lwjgl.opengl.GL30;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwSetMonitorCallback;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;

@Mod.EventBusSubscriber
public class ImGuiBase extends Screen {
    private static Minecraft mc;
    private static long windowPtr;
    private static ImGuiImplGlfw imGuiGlfw;
    private static ImGuiImplGl3 imGuiGl3;
    private static ImGuiIO io;
    private static final String GLSL_VERSION = "#version 110";
    private static final Path FONTS_PATH = ModList.get()
            .getModFileById(PhysicsControl.MODID)
            .getFile()
            .getFilePath()
            .resolve("assets")
            .resolve(PhysicsControl.MODID)
            .resolve("fonts");
    private static final String DEFAULT_FONT_NAME = "unifont.ttf";

    private boolean initialized = false;

    protected ImGuiBase(ITextComponent p_i51108_1_) {
        super(p_i51108_1_);
        MinecraftForge.EVENT_BUS.register(this);
//        if (!initialized) {
//            initImGui();
//            initialized = true;
//        } else {
//            ImGui.createContext();
//        }
        initImGui();
    }

    private void initImGui() {
        if (this.initialized) { //just to make sure
            return;
        }

        mc = Minecraft.getInstance();
        windowPtr = mc.getWindow().getWindow();
        imGuiGlfw = new ImGuiImplGlfw();
        imGuiGl3 = new ImGuiImplGl3();

        ImGui.createContext();

        io = ImGui.getIO();
        io.setIniFilename(null); // We don't want to save .ini file
        io.addConfigFlags(ImGuiConfigFlags.DockingEnable);
        io.addConfigFlags(ImGuiConfigFlags.ViewportsEnable);
        io.setBackendPlatformName("imgui_java_impl_glfw");

        // ------------------------------------------------------------
        // GLFW callbacks to handle user input

        glfwSetKeyCallback(windowPtr, (w, key, scancode, action, mods) -> {
            if (action == GLFW_PRESS) {
                io.setKeysDown(key, true);
            } else if (action == GLFW_RELEASE) {
                io.setKeysDown(key, false);
            }

            io.setKeyCtrl(io.getKeysDown(GLFW_KEY_LEFT_CONTROL) || io.getKeysDown(GLFW_KEY_RIGHT_CONTROL));
            io.setKeyShift(io.getKeysDown(GLFW_KEY_LEFT_SHIFT) || io.getKeysDown(GLFW_KEY_RIGHT_SHIFT));
            io.setKeyAlt(io.getKeysDown(GLFW_KEY_LEFT_ALT) || io.getKeysDown(GLFW_KEY_RIGHT_ALT));
            io.setKeySuper(io.getKeysDown(GLFW_KEY_LEFT_SUPER) || io.getKeysDown(GLFW_KEY_RIGHT_SUPER));

            if (!io.getWantCaptureKeyboard()) {
                mc.execute(() -> mc.keyboardHandler.keyPress(w, key, scancode, action, mods)); // if ImGui didn't use the event, call mc's key handler
            }
        });

        // isn't used in mc
        glfwSetCharCallback(windowPtr, (w, c) -> {
            if (c != GLFW_KEY_DELETE) {
                io.addInputCharacter(c);
            }
        });

        glfwSetMonitorCallback((windowId, event) -> {
            imGuiGlfw.monitorCallback(windowId, event);
            //TODO: add mc's MonitorHandler.onMonitorChange() callback function back
        });

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
        final ImFontConfig fontConfig = new ImFontConfig();
        // Glyphs could be added per-font as well as per config used globally like here
        fontConfig.setGlyphRanges(fontAtlas.getGlyphRangesChineseSimplifiedCommon());
        fontConfig.setPixelSnapH(true);

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

        this.initialized = true;
    }

    //glfwMouseButtonCallback
    //call ImGui's handler, have to do it this way because mc's handler method has private access
    @SubscribeEvent
    public void onRawMouseEvent(final InputEvent.RawMouseEvent event) {
        if (!this.initialized) {
            return;
        }

        int button = event.getButton();
        int action = event.getAction();

//        final boolean[] mouseDown = new boolean[5];
//
//        mouseDown[0] = button == GLFW_MOUSE_BUTTON_1 && action != GLFW_RELEASE;
//        mouseDown[1] = button == GLFW_MOUSE_BUTTON_2 && action != GLFW_RELEASE;
//        mouseDown[2] = button == GLFW_MOUSE_BUTTON_3 && action != GLFW_RELEASE;
//        mouseDown[3] = button == GLFW_MOUSE_BUTTON_4 && action != GLFW_RELEASE;
//        mouseDown[4] = button == GLFW_MOUSE_BUTTON_5 && action != GLFW_RELEASE;
//
//        io.setMouseDown(mouseDown);
//
//        if (!io.getWantCaptureMouse() && mouseDown[1]) {
//            ImGui.setWindowFocus(null);
//        }

        imGuiGlfw.mouseButtonCallback(windowPtr, button, action, event.getMods());

        if (io.getWantCaptureMouse()) {
            event.setCanceled(true);
        }
    }

    //glfwScrollCallback
    //call ImGui's handler, have to do it this way because mc's handler method has private access
    @SubscribeEvent
    public void onMouseScrollEvent(final GuiScreenEvent.MouseScrollEvent.Pre event) {
//        io.setMouseWheelH(io.getMouseWheelH() + (float) event.getScrollDelta());
//        io.setMouseWheel(io.getMouseWheel() + (float) event.getScrollDelta());

        if (!this.initialized) {
            return;
        }

        imGuiGlfw.scrollCallback(windowPtr, event.getScrollDelta(), event.getScrollDelta());
        if (io.getWantCaptureMouse()) {
            event.setCanceled(true);
        }
    }

    @Override
    public void render(MatrixStack matrixStack, int combinedLight, int combinedOverlay, float particleTick) {
        if (!this.initialized) { //just to make sure
            return;
        }

        super.render(matrixStack, combinedLight, combinedOverlay, particleTick);

        // Any Dear ImGui code SHOULD go between ImGui.newFrame()/ImGui.render() methods
        startFrame();
//        setupDockspace();
        ImGui.showDemoWindow();
        endFrame();
    }

    @Override
    public void onClose() {
        super.onClose();
        destroyImGui();
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    private void startFrame() {
        imGuiGlfw.newFrame();
        ImGui.newFrame();
    }

    private void endFrame() {
        GL30.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        GL30.glViewport(0, 0, mc.getWindow().getScreenWidth(), mc.getWindow().getScreenHeight());
        GL30.glClearColor(0, 0, 0, 1);
        GL30.glClear(GL30.GL_COLOR_BUFFER_BIT);

        ImGui.render();
        imGuiGl3.renderDrawData(ImGui.getDrawData());


        if (io.hasConfigFlags(ImGuiConfigFlags.ViewportsEnable)) {
            final long backupWindowPtr = glfwGetCurrentContext();
            ImGui.updatePlatformWindows();
            ImGui.renderPlatformWindowsDefault();
            glfwMakeContextCurrent(backupWindowPtr);
        }
    }

    private void setupDockspace() {
        MainWindow window = mc.getWindow();
        int windowFlags = ImGuiWindowFlags.MenuBar | ImGuiWindowFlags.NoDocking;

        ImGuiViewport mainViewport = ImGui.getMainViewport();
        ImGui.setNextWindowPos(mainViewport.getWorkPosX(), mainViewport.getWorkPosY());
        ImGui.setNextWindowSize(mainViewport.getWorkSizeX(), mainViewport.getWorkSizeY());
        ImGui.setNextWindowViewport(mainViewport.getID());
        ImGui.setNextWindowPos(0.0f, 0.0f);
        ImGui.setNextWindowSize(window.getScreenWidth(), window.getScreenHeight());
        ImGui.pushStyleVar(ImGuiStyleVar.WindowRounding, 0.0f);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowBorderSize, 0.0f);
        windowFlags |= ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoCollapse |
                ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoMove |
                ImGuiWindowFlags.NoBringToFrontOnFocus | ImGuiWindowFlags.NoNavFocus;

        ImGui.begin("Dockspace Demo", new ImBoolean(true), windowFlags);
        ImGui.popStyleVar(2);

        // Dockspace
        ImGui.dockSpace(ImGui.getID("Dockspace"));

//        menuBar.imgui();

        ImGui.end();
    }

    // If you want to clean a room after yourself - do it by yourself
    private void destroyImGui() {
        if (!this.initialized) { //just to make sure
            return;
        }
        this.initialized = false;
        imGuiGlfw.dispose();
        imGuiGl3.dispose();
        ImGui.destroyContext();
    }
}
