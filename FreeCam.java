package carb.modules;

import carb.Module;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.math.Vec3d;

public class FreeCam extends Module {
    private OtherClientPlayerEntity cam;
    private Vec3d pos;

    public FreeCam() { super("FreeCam", "Movement"); }

    private boolean down(KeyBinding kb) {
        int code = InputUtil.fromTranslationKey(kb.getBoundKeyTranslationKey()).getCode();
        return InputUtil.isKeyPressed(mc.getWindow().getHandle(), code);
    }

    @Override
    public void onEnable() { cam = null; }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;
        if (cam == null || cam.getWorld() != mc.world) {
            GameProfile gp = mc.player.getGameProfile();
            cam = new OtherClientPlayerEntity(mc.world, gp);
            pos = mc.player.getEyePos();
            mc.setCameraEntity(cam);
        }
        float yaw = mc.player.getYaw(), pitch = mc.player.getPitch();
        Vec3d fwd = Vec3d.fromPolar(pitch, yaw);
        Vec3d right = Vec3d.fromPolar(0, yaw + 90);
        Vec3d move = Vec3d.ZERO;
        if (down(mc.options.forwardKey)) move = move.add(fwd);
        if (down(mc.options.backKey)) move = move.subtract(fwd);
        if (down(mc.options.rightKey)) move = move.add(right);
        if (down(mc.options.leftKey)) move = move.subtract(right);
        if (down(mc.options.jumpKey)) move = move.add(0, 1, 0);
        if (down(mc.options.sneakKey)) move = move.add(0, -1, 0);
        if (move.lengthSquared() > 0) pos = pos.add(move.normalize().multiply(0.8));
        cam.refreshPositionAndAngles(pos.x, pos.y - 1.62, pos.z, yaw, pitch);

        // prawdziwy gracz stoi w miejscu
        mc.options.forwardKey.setPressed(false);
        mc.options.backKey.setPressed(false);
        mc.options.leftKey.setPressed(false);
        mc.options.rightKey.setPressed(false);
        mc.options.jumpKey.setPressed(false);
        mc.options.sneakKey.setPressed(false);
    }

    @Override
    public void onDisable() {
        if (mc.player != null) mc.setCameraEntity(mc.player);
        cam = null;
    }
}
