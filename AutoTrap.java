package carb.modules;

import carb.Module;
import carb.util.PlaceUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.List;

public class AutoTrap extends Module {
    public AutoTrap() { super("AutoTrap", "Build"); }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;
        PlayerEntity t = PlaceUtil.nearestPlayer(5.0);
        if (t == null) return;

        int slot = PlaceUtil.findHotbar(s -> s.isOf(Items.OBSIDIAN));
        if (slot < 0) slot = PlaceUtil.findHotbar(s -> s.getItem() instanceof BlockItem);
        if (slot < 0) return;

        BlockPos base = t.getBlockPos();
        List<BlockPos> order = new ArrayList<>();
        for (Direction d : Direction.Type.HORIZONTAL) order.add(base.offset(d));        // nogi
        for (Direction d : Direction.Type.HORIZONTAL) order.add(base.up().offset(d));   // glowa
        order.add(base.up(2));                                                          // gora

        int placed = 0;
        for (BlockPos p : order) {
            if (p.equals(mc.player.getBlockPos()) || p.equals(mc.player.getBlockPos().up())) continue;
            if (PlaceUtil.place(p, slot) && ++placed >= 2) break;
        }
    }
}
