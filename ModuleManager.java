package carb;

import carb.modules.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;

import java.util.ArrayList;
import java.util.List;

public final class ModuleManager {
    public static final List<Module> MODULES = new ArrayList<>();

    public static void init() {
        // Combat
        MODULES.add(new Aura());
        MODULES.add(new Hitboxes());
        // Movement
        MODULES.add(new Speed());
        MODULES.add(new Timer());
        MODULES.add(new FreeCam());
        // Build
        MODULES.add(new AutoDripstone());
        MODULES.add(new AutoTrap());
        MODULES.add(new AutoWeb());
        // Alerts
        MODULES.add(new ElytraAlert());
        MODULES.add(new TrapAlert());
        // Visuals
        MODULES.add(new ShaderHand());
        MODULES.add(new HandAnimation());
        MODULES.add(new ChinaHat());
        MODULES.add(new Lyrics3D());
    }

    @SuppressWarnings("unchecked")
    public static <T extends Module> T get(Class<T> c) {
        for (Module m : MODULES) if (c.isInstance(m)) return (T) m;
        return null;
    }

    public static void tick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        boolean licensed = License.isActive();
        long window = mc.getWindow().getHandle();

        for (Module m : MODULES) {
            if (!licensed && m.isEnabled()) m.setEnabled(false);

            if (mc.currentScreen == null && m.key != -1) {
                boolean down = InputUtil.isKeyPressed(window, m.key);
                if (down && !m.keyWasDown) m.toggle();
                m.keyWasDown = down;
            } else {
                m.keyWasDown = false;
            }

            if (m.isEnabled()) m.onTick();
        }
    }
}
