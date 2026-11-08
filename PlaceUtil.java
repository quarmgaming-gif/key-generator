package carb.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.function.Predicate;

public final class PlaceUtil {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static int findHotbar(Predicate<ItemStack> p) {
        for (int i = 0; i < 9; i++) {
            if (p.test(mc.player.getInventory().getStack(i))) return i;
        }
        return -1;
    }

    /** Stawia blok na pozycji pos, opierajac sie o dowolnego solidnego sasiada. */
    public static boolean place(BlockPos pos, int slot) {
        if (!mc.world.getBlockState(pos).isReplaceable()) return false;
        for (Direction d : Direction.values()) {
            BlockPos n = pos.offset(d);
            if (mc.world.getBlockState(n).isReplaceable()) continue;
            return click(n, d.getOpposite(), slot);
        }
        return false;
    }

    /** Klika w sciane 'face' bloku 'support' (blok pojawia sie na support.offset(face)). */
    public static boolean placeAgainst(BlockPos support, Direction face, int slot) {
        if (!mc.world.getBlockState(support.offset(face)).isReplaceable()) return false;
        return click(support, face, slot);
    }

    private static boolean click(BlockPos support, Direction face, int slot) {
        Vec3d hit = Vec3d.ofCenter(support).add(face.getOffsetX() * 0.5, face.getOffsetY() * 0.5, face.getOffsetZ() * 0.5);
        int prev = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = slot;
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, new BlockHitResult(hit, face, support, false));
        mc.player.swingHand(Hand.MAIN_HAND);
        mc.player.getInventory().selectedSlot = prev;
        return true;
    }

    public static net.minecraft.entity.player.PlayerEntity nearestPlayer(double range) {
        net.minecraft.entity.player.PlayerEntity best = null;
        double bd = range * range;
        for (net.minecraft.entity.player.PlayerEntity p : mc.world.getPlayers()) {
            if (p == mc.player || !p.isAlive()) continue;
            double d = mc.player.squaredDistanceTo(p);
            if (d < bd) { bd = d; best = p; }
        }
        return best;
    }
}
