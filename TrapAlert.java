package carb.modules;

import carb.Module;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class TrapAlert extends Module {
    private static final int R = 10, RY = 5;
    private int ticks, cooldown;

    public TrapAlert() { super("TrapAlert", "Alerts"); }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;
        if (cooldown > 0) cooldown--;
        if (++ticks % 20 != 0 || cooldown > 0) return;

        BlockPos me = mc.player.getBlockPos();
        BlockPos.Mutable p = new BlockPos.Mutable();
        int webs = 0, wires = 0, tnt = 0;
        double nearest = Double.MAX_VALUE;

        for (int x = -R; x <= R; x++) for (int y = -RY; y <= RY; y++) for (int z = -R; z <= R; z++) {
            p.set(me.getX() + x, me.getY() + y, me.getZ() + z);
            BlockState s = mc.world.getBlockState(p);
            boolean hit = false;
            if (s.isOf(Blocks.COBWEB)) { webs++; hit = true; }
            else if (s.isOf(Blocks.TRIPWIRE) || s.isOf(Blocks.TRIPWIRE_HOOK)) { wires++; hit = true; }
            else if (s.isOf(Blocks.TNT)) {
                for (int dy = 1; dy <= 2; dy++) {
                    if (mc.world.getBlockState(p.up(dy)).isIn(BlockTags.PRESSURE_PLATES)) { tnt++; hit = true; break; }
                }
            }
            if (hit) nearest = Math.min(nearest, Math.sqrt(me.getSquaredDistance(p)));
        }

        if (webs + wires + tnt > 0) {
            mc.player.sendMessage(Text.literal("[Carb] TRAP w poblizu! pajeczyny: " + webs + ", tripwire: " + wires
                    + ", TNT pod plyta: " + tnt + " (najblizszy " + (int) nearest + " m)"), true);
            cooldown = 5; // x co 20 tickow = ok. 5 s
        }
    }
}
