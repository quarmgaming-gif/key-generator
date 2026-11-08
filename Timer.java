package carb.modules;

import carb.Module;

public class Timer extends Module {
    private static final float MULTIPLIER = 1.6f;

    public Timer() { super("Timer", "Movement"); }

    @Override
    public void onTick() {
        if (mc.world != null) mc.world.getTickManager().setTickRate(20f * MULTIPLIER);
    }

    @Override
    public void onDisable() {
        if (mc.world != null) mc.world.getTickManager().setTickRate(20f);
    }
}
