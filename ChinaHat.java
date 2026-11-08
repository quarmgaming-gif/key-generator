package carb.modules;

import carb.Module;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class ChinaHat extends Module {
    private static final int SEG = 32;
    private static final float RADIUS = 0.55f, HEIGHT = 0.3f;

    public ChinaHat() { super("ChinaHat", "Visuals"); }

    public void render(WorldRenderContext ctx) {
        if (mc.world == null || mc.player == null) return;
        MatrixStack m = ctx.matrixStack();
        VertexConsumerProvider vcp = ctx.consumers();
        if (m == null || vcp == null) return;

        float td = ctx.tickCounter().getTickProgress(false);
        Vec3d cam = ctx.camera().getPos();
        boolean firstPerson = mc.options.getPerspective().isFirstPerson() && mc.getCameraEntity() == mc.player;
        VertexConsumer vc = vcp.getBuffer(RenderLayer.getDebugQuads());

        for (PlayerEntity p : mc.world.getPlayers()) {
            if (p == mc.player && firstPerson) continue;
            Vec3d pos = p.getLerpedPos(td);
            m.push();
            m.translate(pos.x - cam.x, pos.y - cam.y + p.getHeight() - 0.05, pos.z - cam.z);
            Matrix4f mat = m.peek().getPositionMatrix();
            float time = (System.currentTimeMillis() % 4000) / 4000f;
            for (int i = 0; i < SEG; i++) {
                double a0 = Math.PI * 2 * i / SEG, a1 = Math.PI * 2 * (i + 1) / SEG;
                int rgb = MathHelper.hsvToRgb((time + i / (float) SEG) % 1f, 0.8f, 1f);
                int r = (rgb >> 16) & 255, g = (rgb >> 8) & 255, b = rgb & 255;
                float x0 = (float) Math.cos(a0) * RADIUS, z0 = (float) Math.sin(a0) * RADIUS;
                float x1 = (float) Math.cos(a1) * RADIUS, z1 = (float) Math.sin(a1) * RADIUS;
                vc.vertex(mat, 0, HEIGHT, 0).color(r, g, b, 170);
                vc.vertex(mat, 0, HEIGHT, 0).color(r, g, b, 170);
                vc.vertex(mat, x1, 0, z1).color(r, g, b, 170);
                vc.vertex(mat, x0, 0, z0).color(r, g, b, 170);
            }
            m.pop();
        }
        if (vcp instanceof VertexConsumerProvider.Immediate imm) imm.draw(RenderLayer.getDebugQuads());
    }
}
