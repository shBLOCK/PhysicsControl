package com.shblock.physicscontrol.client.gui.PhysicsSimulator;

import codechicken.lib.render.shader.ShaderProgram;
import codechicken.lib.render.shader.ShaderProgramBuilder;
import codechicken.lib.render.shader.UniformType;
import com.shblock.physicscontrol.PhysicsControl;
import com.shblock.physicscontrol.physics.util.AABBHelper;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;
import org.jbox2d.collision.AABB;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;
import org.jbox2d.particle.ParticleColor;
import org.jbox2d.particle.ParticleType;

import java.util.Arrays;
import java.util.function.Predicate;

import static codechicken.lib.render.shader.ShaderObject.StandardShaderType.FRAGMENT;
import static org.lwjgl.opengl.GL43.*;

public class ParticleRender2D {
    private static final int RGBA_PER_PARTICLE = 3; // How many RGBA pixel it takes to represent the data of one particle
    public static int MAX_COUNT = 65536;
    private static final float FILTER_AABB_SIZE = 1F;

    public static ShaderProgram fluidShader = null;

    private static int tbo;
    private static int tex;

    private static ISelectiveResourceReloadListener shaderReloadHandler =
            (resourceManager, resourcePredicate) -> {
        PhysicsControl.log("Reloading particle shader...");
        loadShader();
    };

    static {
        loadShader();
        ((IReloadableResourceManager) Minecraft.getInstance().getResourceManager()).registerReloadListener(shaderReloadHandler);

        initBuffer();
    }

    public static void loadShader() {
        fluidShader = ShaderProgramBuilder.builder()
                .addShader("frag", shader -> shader
                        .type(FRAGMENT)
                        .source(new ResourceLocation(PhysicsControl.MODID, "shaders/particles.frag"))
                        .uniform("count", UniformType.U_INT)
                        .uniform("size", UniformType.FLOAT)
                        .uniform("translate", UniformType.VEC2)
                        .uniform("scale", UniformType.FLOAT)
                        .uniform("data", UniformType.INT))
                .build();
    }

    private static void initBuffer() {
        tbo = glGenBuffers();
        glBindBuffer(GL_TEXTURE_BUFFER, tbo);
        glBufferData(GL_TEXTURE_BUFFER, MAX_COUNT * 4 * RGBA_PER_PARTICLE * 4, GL_DYNAMIC_DRAW);
        glBindBuffer(GL_TEXTURE_BUFFER, 0);

        tex = glGenTextures();
        glBindTexture(GL_TEXTURE_BUFFER, tex);
        glTexBuffer(GL_TEXTURE_BUFFER, GL_RGBA32F, tbo);
        glBindTexture(GL_TEXTURE_BUFFER, 0);
    }

    public static void render(int screenWidth, int screenHeight, Vec2 translate, float scale, World world, SimulatorConfig config) {
        render(
                screenWidth,
                screenHeight,
                translate,
                scale,
                world.getParticleCount(),
                world.getParticleRadius(),
                world.getParticlePositionBuffer(),
                world.getParticleVelocityBuffer(),
                world.getParticleColorBuffer(),
                world.getParticleFlagsBuffer(),
                config
        );
    }

    private static float[] genRenderData(int count, float particleSize, Vec2[] position, Vec2[] velocity, ParticleColor[] color, int[] flags, Predicate<Vec2> filter) {
        int dataPerP = 4 * RGBA_PER_PARTICLE;
        float[] data = new float[dataPerP * count];
        int cnt = 0;
        int i = 0;
        while (i<Math.min(count, MAX_COUNT)) {
            if (filter.test(position[i]) && ((flags[i] & ParticleType.b2_zombieParticle) == 0)) {
                data[dataPerP * cnt] = position[i].x;
                data[dataPerP * cnt + 1] = position[i].y;

                data[dataPerP * cnt + 2] = (color[i].r + 128) / 255F;
                data[dataPerP * cnt + 3] = (color[i].g + 128) / 255F;
                data[dataPerP * cnt + 4] = (color[i].b + 128) / 255F;
                data[dataPerP * cnt + 5] = (color[i].a + 128) / 255F;

                data[dataPerP * cnt + 6] = velocity[i].x;
                data[dataPerP * cnt + 7] = velocity[i].y;

                data[dataPerP * cnt + 8] = flags[i];

                cnt++;
            }
            i++;
        }
        return Arrays.copyOfRange(data, 0, cnt * dataPerP);
    }

    public static void render(int screenWidth, int screenHeight, Vec2 translate, float scale, int count, float particleSize, Vec2[] position, Vec2[] velocity, ParticleColor[] color, int[] flags, SimulatorConfig config) {
        Vec2 lower = new Vec2(
                (-translate.x) / scale,
                (-translate.y) / scale
        );
        Vec2 upper = new Vec2(
                (screenWidth - translate.x) / scale,
                (screenHeight - translate.y) / scale
        );

        glEnable(GL_BLEND);
//        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);

        int shader = fluidShader.getProgramId();
        if (shader == -1) {
            fluidShader.use();
            shader = fluidShader.getProgramId();
        }
        glUseProgram(shader);

        glUniform1f(glGetUniformLocation(shader, "size"), particleSize);
        MainWindow window = Minecraft.getInstance().getWindow();
        glUniform2f(glGetUniformLocation(shader, "screenSize"), window.getScreenWidth(), window.getScreenHeight()   );
        float guiScale = (float) window.getGuiScale();
        glUniform2f(glGetUniformLocation(shader, "translate"), translate.x * guiScale, translate.y * guiScale);
        glUniform1f(glGetUniformLocation(shader, "scale"), scale * guiScale);
        glBindBuffer(GL_TEXTURE_BUFFER, tbo);
        AABB screenBB = new AABB(lower, upper);
        float[] data = genRenderData(count, particleSize, position, velocity, color, flags, pos -> {
            Vec2 p = pos.clone();
            p.y = -p.y;
            return AABBHelper.isOverlapping2D(screenBB, new AABB(p.sub(new Vec2(FILTER_AABB_SIZE, FILTER_AABB_SIZE)), p.add(new Vec2(FILTER_AABB_SIZE, FILTER_AABB_SIZE))));
        });
        glBufferSubData(
                GL_TEXTURE_BUFFER,
                0,
                data
        );
        glBindTexture(GL_TEXTURE_BUFFER, tex);
        glUniform1i(glGetUniformLocation(shader, "data"), 0);
        glUniform1ui(glGetUniformLocation(shader, "count"), data.length / (4 * RGBA_PER_PARTICLE)); //TODO: improve this way to get the render count

        glUniform1f(glGetUniformLocation(shader, "smoothLower"), config.particleRenderSmoothLowerBound);
        glUniform1f(glGetUniformLocation(shader, "smoothUpper"), config.particleRenderSmoothUpperBound);
        glUniform1f(glGetUniformLocation(shader, "borderLower"), config.particleRenderBorderLowerBound);
        glUniform1f(glGetUniformLocation(shader, "borderUpper"), config.particleRenderBorderUpperBound);

        glBegin(GL_QUADS);
        glColor4f(1F, 1F, 1F, 1F);
        glVertex3f(0F, 0F, 0);
        glVertex3f(0F, screenHeight, 0);
        glVertex3f(screenWidth, screenHeight, 0);
        glVertex3f(screenWidth, 0F, 0);
        glEnd();

        glUseProgram(0);
    }
}
