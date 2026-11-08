package carb.modules;

import carb.Module;
import carb.util.PlaceUtil;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class AutoDripstone extends Module {
    private BlockPos pending;

    public AutoDripstone() {
        super("AutoDripstone", "Build");
        UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
            if (isEnabled() && world.isClient && hand == Hand.MAIN_HAND
                    && world.getBlockState(hit.getBlockPos()).getBlock() instanceof TrapdoorBlock) {
                pending = hit.getBlockPos();
            }
            return ActionResult.PASS;
        });
    }

    @Override
    public void onTick() {
        if (pending == null || mc.player == null) return;
        BlockPos trap = pending;
        pending = null;
        int slot = PlaceUtil.findHotbar(s -> s.isOf(Items.POINTED_DRIPSTONE));
        if (slot < 0) {
            mc.player.sendMessage(Text.literal("[Carb] Brak dripstone w hotbarze"), true);
            return;
        }
        PlaceUtil.placeAgainst(trap, Direction.DOWN, slot);
    }
}
