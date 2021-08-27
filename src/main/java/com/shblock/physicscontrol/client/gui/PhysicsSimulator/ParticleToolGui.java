package com.shblock.physicscontrol.client.gui.PhysicsSimulator;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.shblock.physicscontrol.client.I18nHelper;
import com.shblock.physicscontrol.client.InteractivePhysicsSimulator2D;
import com.shblock.physicscontrol.client.gui.RenderHelper;
import com.shblock.physicscontrol.command.CommandCreateParticles;
import com.shblock.physicscontrol.command.CommandDeleteParticles;
import com.shblock.physicscontrol.command.CommandEditParticles;
import com.shblock.physicscontrol.physics.util.ParticleHelper;
import imgui.ImGui;
import imgui.flag.ImGuiColorEditFlags;
import imgui.flag.ImGuiDataType;
import imgui.flag.ImGuiSliderFlags;
import imgui.flag.ImGuiTabBarFlags;
import imgui.type.ImFloat;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.vector.Matrix4f;
import org.jbox2d.collision.AABB;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Rot;
import org.jbox2d.common.Settings;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;
import org.jbox2d.particle.ParticleColor;
import org.jbox2d.particle.ParticleDef;
import org.jbox2d.particle.ParticleType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ParticleToolGui {
    private static final String PREFIX = "physicscontrol.gui.sim.particle_tool.";

    public void buildImGui(SimulatorConfig config) {
        if (ImGui.begin(I18n.get(PREFIX + "window"))) {
            if (ImGui.beginTabBar("##tab_bar", ImGuiTabBarFlags.NoCloseWithMiddleMouseButton)) {
                if (ImGui.beginTabItem(I18n.get(PREFIX + "tab.tools"))) {
                    // Tool Size
                    ImGui.text(I18n.get(PREFIX + "tab.tools.size"));
                    ImGui.sameLine();
                    ImFloat size = new ImFloat(config.particleToolSize);
                    if (ImGui.sliderScalar("##tool_size", ImGuiDataType.Float, size, 0.1F, 10F, I18nHelper.localizeNumFormat(PREFIX + "tab.tools.size.num"))) {
                        config.particleToolSize = size.get();
                    }

                    // Particle Color
                    if (ImGui.checkbox(I18n.get(PREFIX + "tab.tools.set_color"), config.particleToolSetColor)) {
                        config.particleToolSetColor = !config.particleToolSetColor;
                    }
                    if (config.particleToolSetColor) {
                        ImGui.colorPicker4(I18n.get(PREFIX + "tab.tools.color"), config.particleToolColor, ImGuiColorEditFlags.AlphaBar | ImGuiColorEditFlags.AlphaPreviewHalf | ImGuiColorEditFlags.PickerHueBar | ImGuiColorEditFlags.Uint8);
                    }

                    // Particle Flags
                    if (ImGui.checkbox(I18n.get(PREFIX + "tab.tools.set_flags"), config.particleToolSetFlags))  {
                        config.particleToolSetFlags = !config.particleToolSetFlags;
                    }
                    if (config.particleToolSetFlags) {
                        if (ImGui.beginListBox("##flag_checkboxes")) {
                            addFlagSelector(config, ParticleType.b2_powderParticle, I18n.get(PREFIX + "flags.powder"));
                            addFlagSelector(config, ParticleType.b2_tensileParticle, I18n.get(PREFIX + "flags.tensile"));
                            addFlagSelector(config, ParticleType.b2_viscousParticle, I18n.get(PREFIX + "flags.viscous"));
                            addFlagSelector(config, ParticleType.b2_colorMixingParticle, I18n.get(PREFIX + "flags.color_mixing"));
                            addFlagSelector(config, ParticleType.b2_wallParticle, I18n.get(PREFIX + "flags.wall"));
                            addFlagSelector(config, ParticleType.b2_springParticle, I18n.get(PREFIX + "flags.spring"));
                            addFlagSelector(config, ParticleType.b2_elasticParticle, I18n.get(PREFIX + "flags.elastic"));
                            ImGui.endListBox();
                        }
                    }

                    ImGui.endTabItem();
                }

                if (ImGui.beginTabItem(I18n.get(PREFIX + "tab.rendering"))) {
                    ImGui.text(I18n.get(PREFIX + "tab.rendering.slb"));
                    ImGui.sameLine();
                    ImFloat slb = new ImFloat(config.particleRenderSmoothLowerBound);
                    if (ImGui.sliderScalar("##smooth_lower", ImGuiDataType.Float, slb, 0F, config.particleRenderSmoothUpperBound, "%.2F", ImGuiSliderFlags.AlwaysClamp)) {
                        config.particleRenderSmoothLowerBound = slb.get();
                    }

                    ImGui.text(I18n.get(PREFIX + "tab.rendering.sub"));
                    ImGui.sameLine();
                    ImFloat sub = new ImFloat(config.particleRenderSmoothUpperBound);
                    if (ImGui.sliderScalar("##smooth_upper", ImGuiDataType.Float, sub, config.particleRenderSmoothLowerBound, 10F, "%.2F", ImGuiSliderFlags.AlwaysClamp)) {
                        config.particleRenderSmoothUpperBound = sub.get();
                    }

                    ImGui.text(I18n.get(PREFIX + "tab.rendering.blb"));
                    ImGui.sameLine();
                    ImFloat blb = new ImFloat(config.particleRenderBorderLowerBound);
                    if (ImGui.sliderScalar("##border_lower", ImGuiDataType.Float, blb, 0F, config.particleRenderBorderUpperBound, "%.2F", ImGuiSliderFlags.AlwaysClamp)) {
                        config.particleRenderBorderLowerBound = blb.get();
                    }

                    ImGui.text(I18n.get(PREFIX + "tab.rendering.bub"));
                    ImGui.sameLine();
                    ImFloat bub = new ImFloat(config.particleRenderBorderUpperBound);
                    if (ImGui.sliderScalar("##border_upper", ImGuiDataType.Float, bub, config.particleRenderBorderLowerBound, 1F, "%.2F", ImGuiSliderFlags.AlwaysClamp)) {
                        config.particleRenderBorderUpperBound = bub.get();
                    }

                    if (ImGui.button(I18n.get(PREFIX + "tab.rendering.reset"))) { // Probably can be improved
                        config.resetParticleRender();
                    }

                    ImGui.endTabItem();
                }

                ImGui.endTabBar();
            }
        }

        ImGui.end();
    }

    private void addFlagSelector(SimulatorConfig config, int flag, String name) {
        boolean haveFlag = (config.particleToolFlags & flag) != 0;
        if (ImGui.checkbox(name, haveFlag)) {
            if (haveFlag) {
                config.particleToolFlags -= flag;
            } else {
                config.particleToolFlags += flag;
            }
        }
    }

    private float[] getParticleColorArray(SimulatorConfig config) {
        return config.particleToolSetColor ? config.particleToolColor : SimulatorConfig.defaultParticleToolColor;
    }

    private ParticleColor getParticleColor(SimulatorConfig config) {
        float[] c = getParticleColorArray(config);
        return new ParticleColor(
                (byte) (c[0] * 255F - 128F),
                (byte) (c[1] * 255F - 128F),
                (byte) (c[2] * 255F - 128F),
                (byte) (c[3] * 255F - 128F)
        );
    }

    private int getParticleFlags(SimulatorConfig config) {
        return config.particleToolSetFlags ? config.particleToolFlags : SimulatorConfig.defaultParticleToolFlags;
    }

    public void render(MatrixStack matrixStack, Vec2 pos, SimulatorConfig config) {
        matrixStack.pushPose();

        matrixStack.translate(pos.x, pos.y, 0F);
        Matrix4f matrix = matrixStack.last().pose();
        float[] color = getParticleColorArray(config);
        RenderHelper.drawCircleFrame(matrix, config.particleToolSize, 1F, color[0], color[1], color[2], 1F);

        matrixStack.popPose();
    }

    public static final int CREATE = 0;
    public static final int EDIT = 1;
    public static final int DELETE = 2;

    public void execute(GuiPhysicsSimulator gui, InteractivePhysicsSimulator2D simulator, Vec2 pos, int operation, SimulatorConfig config) {
        World world = simulator.getSpace();

        Shape shape = new CircleShape();
        Transform transform = new Transform(pos, new Rot(0F));
        shape.setRadius(config.particleToolSize);
        Set<Integer> particlesInShape = ParticleHelper.getParticlesInShape(simulator.getSpace(), shape, transform);

        switch (operation) {
            case CREATE:
                AABB aabb = new AABB();
                shape.computeAABB(aabb, transform, 0);

                List<Vec2> positions = new ArrayList<>();
                float stride = Settings.particleStride * world.getParticleRadius() * 2F;
                for (float x = aabb.lowerBound.x; x < aabb.upperBound.x; x += stride) {
                    for (float y = aabb.lowerBound.y; y < aabb.upperBound.y; y += stride) {
                        Vec2 point = new Vec2(x, y);
                        if (shape.testPoint(transform, point)) {
                            positions.add(point);
                        }
                    }
                }
                Vec2[] posBuf = world.getParticlePositionBuffer();
                float fac = world.getParticleRadius() * 2F;
                Vec2[] positionArray = positions.stream().filter(point -> {
                    for (int index : particlesInShape) {
                        float dist = posBuf[index].sub(point).length();
                        if (dist < fac) {
                            return false;
                        }
                    }
                    return true;
                }).toArray(Vec2[]::new);

                ParticleDef def = new ParticleDef();
                def.color = getParticleColor(config);
                def.flags = getParticleFlags(config);
                simulator.executeCommand(new CommandCreateParticles(def, positionArray));
                break;
            case EDIT:
                CommandEditParticles cmd;
                if (config.particleToolSetColor && config.particleToolSetFlags) {
                    cmd = new CommandEditParticles(particlesInShape, getParticleColor(config), config.particleToolFlags);
                } else if (config.particleToolSetColor) {
                    cmd = new CommandEditParticles(particlesInShape, getParticleColor(config));
                } else if (config.particleToolSetFlags) {
                    cmd = new CommandEditParticles(particlesInShape, config.particleToolFlags);
                } else {
                    return;
                }
                simulator.executeCommand(cmd);
                break;
            case DELETE:
                simulator.executeCommand(new CommandDeleteParticles(particlesInShape));
                break;
        }
    }
}
