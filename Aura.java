package carb.modules;

import carb.Module;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;

public class Aura extends Module {
    private static final double RANGE = 4.2;

    public Aura() { super("Aura", "Combat"); }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (mc.player.getAttackCooldownProgress(0.5f) < 1.0f) return;

        PlayerEntity best = null;
        double bestDist = RANGE * RANGE;
        for (PlayerEntity p : mc.world.getPlayers()) {
            if (p == mc.player || !p.isAlive()) continue;
            double d = mc.player.squaredDistanceTo(p);
            if (d < bestDist) { bestDist = d; best = p; }
        }
        if (best != null) {
            mc.interactionManager.attackEntity(mc.player, best);
            mc.player.swingHand(Hand.MAIN_HAND);
        }
    }
}
