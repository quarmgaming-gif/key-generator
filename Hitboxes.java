package carb.modules;

import carb.Module;
import net.minecraft.entity.player.PlayerEntity;

public class Hitboxes extends Module {
    private static final double EXPAND = 0.35;

    public Hitboxes() { super("Hitboxes", "Combat"); }

    @Override
    public void onTick() {
        if (mc.world == null) return;
        for (PlayerEntity p : mc.world.getPlayers()) {
            if (p == mc.player) continue;
            p.setBoundingBox(p.getDimensions(p.getPose()).getBoxAt(p.getPos()).expand(EXPAND, 0, EXPAND));
        }
    }

    @Override
    public void onDisable() {
        if (mc.world == null) return;
        for (PlayerEntity p : mc.world.getPlayers()) {
            p.setBoundingBox(p.getDimensions(p.getPose()).getBoxAt(p.getPos()));
        }
    }
}
