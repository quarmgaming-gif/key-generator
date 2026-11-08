package carb;

import carb.gui.CarbScreen;
import carb.modules.ChinaHat;
import carb.modules.Lyrics3D;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class CarbClient implements ClientModInitializer {
    private static KeyBinding openGui;

    @Override
    public void onInitializeClient() {
        License.loadFromDisk();
        ModuleManager.init();

        openGui = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.carb.gui", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K, "category.carb"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openGui.wasPressed()) client.setScreen(new CarbScreen());
            ModuleManager.tick();
        });

        WorldRenderEvents.AFTER_ENTITIES.register(ctx -> {
            ChinaHat hat = ModuleManager.get(ChinaHat.class);
            if (hat != null && hat.isEnabled()) hat.render(ctx);
            Lyrics3D ly = ModuleManager.get(Lyrics3D.class);
            if (ly != null && ly.isEnabled()) ly.render(ctx);
        });
    }
}
