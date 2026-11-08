package carb.gui;

import carb.License;
import carb.Module;
import carb.ModuleManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CarbScreen extends Screen {
    private static final String[] LEFT = {"Combat", "Movement", "Build", "Alerts"};
    private static final String[] RIGHT = {"Visuals"};

    private TextFieldWidget keyField;
    private Module binding;
    private String message = "";
    private final Map<Module, ButtonWidget> bindButtons = new HashMap<>();
    private final List<Object[]> headers = new ArrayList<>();

    public CarbScreen() {
        super(Text.literal("Carb Client"));
    }

    @Override
    protected void init() {
        bindButtons.clear();
        headers.clear();

        int lx = 20;
        int rx = Math.max(lx + 340, width - 330);

        keyField = new TextFieldWidget(textRenderer, lx, 44, 240, 20, Text.literal("Klucz"));
        keyField.setMaxLength(400);
        keyField.setPlaceholder(Text.literal("Wklej klucz CARB-..."));
        addDrawableChild(keyField);
        addDrawableChild(ButtonWidget.builder(Text.literal("Aktywuj"), b -> {
            License.Status s = License.activate(keyField.getText());
            message = switch (s) {
                case VALID_MONTH -> "Klucz miesieczny OK";
                case VALID_LIFE -> "Klucz lifetime OK";
                case EXPIRED -> "Klucz wygasl";
                case INVALID -> "Nieprawidlowy klucz";
            };
        }).dimensions(lx + 245, 44, 70, 20).build());

        int y = 80;
        for (String cat : LEFT) y = column(cat, lx, y);
        y = 80;
        for (String cat : RIGHT) y = column(cat, rx, y);
    }

    private int column(String category, int x, int y) {
        headers.add(new Object[]{category, x, y});
        y += 14;
        for (Module m : ModuleManager.MODULES) {
            if (!m.category.equals(category)) continue;
            ButtonWidget t = ButtonWidget.builder(label(m), b -> {
                m.toggle();
                b.setMessage(label(m));
            }).dimensions(x, y, 150, 20).build();
            ButtonWidget k = ButtonWidget.builder(bindLabel(m), b -> {
                binding = m;
                b.setMessage(Text.literal("[ nacisnij klawisz ]"));
            }).dimensions(x + 155, y, 150, 20).build();
            addDrawableChild(t);
            addDrawableChild(k);
            bindButtons.put(m, k);
            y += 22;
        }
        return y + 6;
    }

    private Text label(Module m) {
        return Text.literal(m.name + ": " + (m.isEnabled() ? "ON" : "OFF"));
    }

    private Text bindLabel(Module m) {
        String k = m.key == -1 ? "brak" : InputUtil.fromKeyCode(m.key, 0).getLocalizedText().getString();
        return Text.literal("Bind: " + k);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (binding != null) {
            binding.key = keyCode == 256 ? -1 : keyCode; // ESC = usun bind
            bindButtons.get(binding).setMessage(bindLabel(binding));
            binding = null;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        ctx.drawTextWithShadow(textRenderer, "Carb Client", 20, 12, 0xFFFFFF);
        String lic = License.isActive() ? "Licencja: " + License.status() : "Brak licencji - wpisz klucz";
        ctx.drawTextWithShadow(textRenderer, lic, 20, 26, License.isActive() ? 0x55FF55 : 0xFF5555);
        if (!message.isEmpty()) ctx.drawTextWithShadow(textRenderer, message, 340, 50, 0xFFFFFF);
        for (Object[] h : headers) {
            ctx.drawTextWithShadow(textRenderer, "-- " + h[0] + " --", (int) h[1], (int) h[2], 0xFFAA00);
        }
    }

    @Override
    public boolean shouldPause() { return false; }
}
