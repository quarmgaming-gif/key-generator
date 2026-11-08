package carb.modules;

import carb.Module;
import carb.util.PlaceUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;

public class AutoWeb extends Module {
    public AutoWeb() { super("AutoWeb", "Build"); }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;
        PlayerEntity t = PlaceUtil.nearestPlayer(5.0);
        if (t == null) return;
        int slot = PlaceUtil.findHotbar(s -> s.isOf(Items.COBWEB));
        if (slot < 0) return;

        BlockPos feet = t.getBlockPos();
        if (!PlaceUtil.place(feet, slot)) PlaceUtil.place(feet.up(), slot);
    }
}
