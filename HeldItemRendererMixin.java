package carb.mixin;

import carb.ModuleManager;
import carb.modules.HandAnimation;
import carb.modules.ShaderHand;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public class HeldItemRendererMixin {
    @Inject(method = "renderFirstPersonItem", at = @At("HEAD"))
    private void carb$head(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand,
                           float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices,
                           VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        HandAnimation ha = ModuleManager.get(HandAnimation.class);
        if (ha != null && ha.isEnabled()) ha.apply(matrices, swingProgress);
        ShaderHand sh = ModuleManager.get(ShaderHand.class);
        if (sh != null && sh.isEnabled()) sh.tint();
    }

    @Inject(method = "renderFirstPersonItem", at = @At("TAIL"))
    private void carb$tail(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand,
                           float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices,
                           VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }
}
