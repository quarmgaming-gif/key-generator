package carb.modules;

import carb.Module;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ElytraAlert extends Module {
    private final Set<UUID> alerted = new HashSet<>();

    public ElytraAlert() { super("ElytraAlert", "Alerts"); }

    @Override
    public void onDisable() { alerted.clear(); }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;
        Set<UUID> flying = new HashSet<>();
        for (PlayerEntity p : mc.world.getPlayers()) {
            if (p == mc.player || !p.isGliding()) continue;
            flying.add(p.getUuid());
            if (alerted.add(p.getUuid())) {
                int dist = (int) mc.player.distanceTo(p);
                mc.player.sendMessage(Text.literal("[Carb] " + p.getName().getString()
                        + " leci na elytrze (" + dist + " m)"), true);
            }
        }
        alerted.retainAll(flying);
    }
}
