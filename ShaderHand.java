package carb.modules;

import carb.Module;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.util.math.MathHelper;

public class ShaderHand extends Module {
    public ShaderHand() { super("ShaderHand", "Visuals"); }

    /** Wywolywane z mixina przed rysowaniem reki. */
    public void tint() {
        float h = (System.currentTimeMillis() % 3000) / 3000f;
        int rgb = MathHelper.hsvToRgb(h, 0.7f, 1f);
        RenderSystem.setShaderColor(((rgb >> 16) & 255) / 255f, ((rgb >> 8) & 255) / 255f, (rgb & 255) / 255f, 1f);
    }
}
