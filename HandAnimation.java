package carb.modules;

import carb.Module;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public class HandAnimation extends Module {
    public HandAnimation() { super("HandAnimation", "Visuals"); }

    /** Wywolywane z mixina przed rysowaniem reki. */
    public void apply(MatrixStack m, float swing) {
        if (swing <= 0) return;
        float s = MathHelper.sin(swing * (float) Math.PI);
        m.translate(0.0f, -0.15f * s, 0.0f);
        m.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-45f * s));
        m.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-25f * s));
    }
}
