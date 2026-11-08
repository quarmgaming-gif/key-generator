package carb;

import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

public abstract class Module {
    public final String name;
    public final String category;
    public int key = GLFW.GLFW_KEY_UNKNOWN;
    private boolean enabled;
    boolean keyWasDown;

    protected final MinecraftClient mc = MinecraftClient.getInstance();

    protected Module(String name, String category) {
        this.name = name;
        this.category = category;
    }

    public boolean isEnabled() { return enabled; }

    public void toggle() { setEnabled(!enabled); }

    public void setEnabled(boolean on) {
        if (on && !License.isActive()) return; // bez licencji nic sie nie wlaczy
        if (on == enabled) return;
        enabled = on;
        if (on) onEnable(); else onDisable();
    }

    public void onEnable() {}
    public void onDisable() {}
    public void onTick() {}
}
