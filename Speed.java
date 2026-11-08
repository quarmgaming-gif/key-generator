package carb.modules;

import carb.Module;
import net.minecraft.util.math.Vec3d;

public class Speed extends Module {
    public Speed() { super("Speed", "Movement"); }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        boolean moving = mc.player.input.movementForward != 0 || mc.player.input.movementSideways != 0;
        if (!moving || !mc.player.isOnGround()) return;
        Vec3d v = mc.player.getVelocity();
        double nx = v.x * 1.15, nz = v.z * 1.15;
        double h = Math.sqrt(nx * nx + nz * nz);
        if (h > 0.5) { nx *= 0.5 / h; nz *= 0.5 / h; }
        mc.player.setVelocity(nx, v.y, nz);
    }
}
