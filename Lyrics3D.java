package carb.modules;

import carb.Module;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Napisy ze sluchanej piosenki w 3D. Tylko Windows + Spotify (aplikacja desktop):
 * tytul bierzemy z okna Spotify, tekst z lrclib.net. Synchronizacja jest przyblizona
 * (liczymy czas od zmiany tytulu).
 */
public class Lyrics3D extends Module {
    private static final Pattern LRC = Pattern.compile("\\[(\\d+):(\\d+)(?:\\.(\\d+))?](.*)");

    private static class Floating {
        final String text; final Vec3d pos; final long born;
        Floating(String t, Vec3d p, long b) { text = t; pos = p; born = b; }
    }

    private volatile boolean running;
    private volatile String title = "";
    private volatile long songStart;
    private volatile List<long[]> times = new ArrayList<>();
    private volatile List<String> lines = new ArrayList<>();
    private int lastIndex = -1;
    private final List<Floating> floating = new CopyOnWriteArrayList<>();

    public Lyrics3D() { super("Lyrics3D", "Visuals"); }

    @Override
    public void onEnable() {
        running = true;
        title = "";
        lastIndex = -1;
        Thread t = new Thread(this::poll, "carb-lyrics");
        t.setDaemon(true);
        t.start();
    }

    @Override
    public void onDisable() {
        running = false;
        floating.clear();
    }

    // ---------- watek w tle ----------
    private void poll() {
        HttpClient http = HttpClient.newHttpClient();
        while (running) {
            try {
                String now = spotifyTitle();
                if (now != null && !now.equals(title)) {
                    title = now;
                    songStart = System.currentTimeMillis();
                    times = new ArrayList<>();
                    lines = new ArrayList<>();
                    lastIndex = -1;
                    fetch(http, now);
                }
            } catch (Exception ignored) {}
            try { Thread.sleep(2000); } catch (InterruptedException e) { return; }
        }
    }

    private String spotifyTitle() throws Exception {
        if (!System.getProperty("os.name").toLowerCase().contains("win")) return null;
        Process pr = new ProcessBuilder("tasklist", "/v", "/fo", "csv", "/nh", "/fi", "IMAGENAME eq Spotify.exe").start();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(pr.getInputStream()))) {
            String line;
            while ((line = r.readLine()) != null) {
                int i = line.lastIndexOf("\",\"");
                if (i < 0) continue;
                String t = line.substring(i + 3).replace("\"", "").trim();
                if (t.contains(" - ") && !t.startsWith("Spotify")) return t;
            }
        }
        return null;
    }

    private void fetch(HttpClient http, String t) throws Exception {
        int i = t.indexOf(" - ");
        String artist = URLEncoder.encode(t.substring(0, i), StandardCharsets.UTF_8);
        String track = URLEncoder.encode(t.substring(i + 3), StandardCharsets.UTF_8);
        HttpRequest req = HttpRequest.newBuilder(URI.create(
                "https://lrclib.net/api/get?artist_name=" + artist + "&track_name=" + track)).build();
        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200) return;
        JsonObject o = JsonParser.parseString(res.body()).getAsJsonObject();
        if (!o.has("syncedLyrics") || o.get("syncedLyrics").isJsonNull()) return;

        List<long[]> ts = new ArrayList<>();
        List<String> ls = new ArrayList<>();
        for (String l : o.get("syncedLyrics").getAsString().split("\n")) {
            Matcher m = LRC.matcher(l.trim());
            if (!m.matches() || m.group(4).trim().isEmpty()) continue;
            long ms = Long.parseLong(m.group(1)) * 60000 + Long.parseLong(m.group(2)) * 1000;
            if (m.group(3) != null) ms += Long.parseLong((m.group(3) + "00").substring(0, 3));
            ts.add(new long[]{ms});
            ls.add(m.group(4).trim());
        }
        times = ts;
        lines = ls;
    }

    // ---------- render ----------
    public void render(WorldRenderContext ctx) {
        if (mc.world == null || mc.player == null) return;
        MatrixStack m = ctx.matrixStack();
        VertexConsumerProvider vcp = ctx.consumers();
        if (m == null || vcp == null) return;

        long now = System.currentTimeMillis();
        List<long[]> ts = times;
        List<String> ls = lines;
        long elapsed = now - songStart;
        int idx = -1;
        for (int i = 0; i < ts.size(); i++) if (ts.get(i)[0] <= elapsed) idx = i;
        Vec3d cam = ctx.camera().getPos();

        if (idx >= 0 && idx != lastIndex && idx < ls.size()) {
            lastIndex = idx;
            Vec3d look = Vec3d.fromPolar(mc.player.getPitch(), mc.player.getYaw()).multiply(4.0);
            floating.add(new Floating(ls.get(idx), cam.add(look).add(0, 0.5, 0), now));
            while (floating.size() > 4) floating.remove(0);
        }

        TextRenderer tr = mc.textRenderer;
        for (Floating f : floating) {
            float age = (now - f.born) / 1000f;
            if (age > 7f) { floating.remove(f); continue; }
            int alpha = (int) (255 * Math.min(1f, (7f - age) / 2f));
            if (alpha < 8) continue;
            Vec3d p = f.pos.add(0, age * 0.25, 0);
            m.push();
            m.translate(p.x - cam.x, p.y - cam.y, p.z - cam.z);
            m.multiply(ctx.camera().getRotation());
            m.scale(-0.04f, -0.04f, 0.04f);
            float x = -tr.getWidth(f.text) / 2f;
            tr.draw(f.text, x, 0, (alpha << 24) | 0xFFFFFF, true, m.peek().getPositionMatrix(), vcp,
                    TextRenderer.TextLayerType.SEE_THROUGH, 0, 0xF000F0);
            m.pop();
        }
        if (vcp instanceof VertexConsumerProvider.Immediate imm) imm.draw();
    }
}
