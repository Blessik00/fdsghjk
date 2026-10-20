package dev.pulse.pvp;

import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_2398;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_638;
import net.minecraft.class_746;

/** Additional PvP modules: Hit Marker, Low Health Warning, Jump Circle, Kill Effect, Speed HUD, Cooldown Bar. */
public final class Extras {
    private static long lastComboTime = Long.MIN_VALUE;
    private static int lastCombo;
    private static long markerAt;

    private static boolean wasOnGround = true;
    private static double lastX, lastZ;
    private static boolean haveLast;
    private static float speed;

    private static class_1309 watched;
    private static boolean killDone;

    private Extras() {}

    private static int a(int rgb, int alpha) {
        return (Math.max(0, Math.min(255, alpha)) << 24) | (rgb & 0xFFFFFF);
    }

    // ------------------------------------------------------------------ tick
    public static void tick(class_310 mc) {
        class_746 p = mc.field_1724;
        class_638 w = mc.field_1687;
        if (p == null || w == null) {
            haveLast = false;
            watched = null;
            return;
        }
        try {
            // hit marker trigger
            if (Tracker.comboTime != lastComboTime || Tracker.combo > lastCombo) {
                if (Tracker.combo > 0 && Tracker.comboTime != lastComboTime) markerAt = System.currentTimeMillis();
                lastComboTime = Tracker.comboTime;
            }
            lastCombo = Tracker.combo;

            // speed
            double x = p.method_23317(), z = p.method_23321();
            if (haveLast) {
                double d = Math.sqrt((x - lastX) * (x - lastX) + (z - lastZ) * (z - lastZ)) * 20.0;
                speed += ((float) d - speed) * 0.25f;
            }
            lastX = x;
            lastZ = z;
            haveLast = true;

            // jump circle
            boolean ground = p.method_24828();
            if (wasOnGround && !ground && Config.on(Mods.JUMP)) jumpRing(w, p);
            wasOnGround = ground;

            // kill effect
            class_1309 t = Tracker.target;
            if (t != null && t != watched) {
                watched = t;
                killDone = false;
            }
            if (watched != null && !killDone && Config.on(Mods.KILL)) {
                if (watched.method_6032() <= 0f || watched.method_31481()) {
                    killDone = true;
                    if (watched != p) killBurst(w, watched);
                }
            }
        } catch (Throwable ignored) {
        }
    }

    private static void jumpRing(class_638 w, class_746 p) {
        float size = Mods.v(Mods.JUMP, "size");
        double x = p.method_23317(), y = p.method_23318() + 0.05, z = p.method_23321();
        int n = 28;
        for (int i = 0; i < n; i++) {
            double ang = i * (Math.PI * 2 / n);
            double c = Math.cos(ang), s = Math.sin(ang);
            w.method_8406(class_2398.field_11205, x + c * 0.25, y, z + s * 0.25, c * 0.12 * size, 0.01, s * 0.12 * size);
        }
    }

    private static void killBurst(class_638 w, class_1309 e) {
        int amount = Math.round(Mods.v(Mods.KILL, "amount"));
        double x = e.method_23317(), y = e.method_23318() + e.method_17682() * 0.5, z = e.method_23321();
        for (int i = 0; i < amount; i++) {
            double u = Math.random() * Math.PI * 2, v = Math.random() * Math.PI - Math.PI / 2;
            double sp = 0.15 + Math.random() * 0.25;
            double dx = Math.cos(u) * Math.cos(v) * sp, dy = Math.sin(v) * sp + 0.1, dz = Math.sin(u) * Math.cos(v) * sp;
            w.method_8406(i % 2 == 0 ? class_2398.field_11205 : class_2398.field_11208, x, y, z, dx, dy, dz);
        }
    }

    // ---------------------------------------------------------------- render
    public static void render(class_332 ctx) {
        class_310 mc = class_310.method_1551();
        if (mc == null || mc.field_1724 == null || mc.field_1687 == null) return;
        int w = ctx.method_51421(), h = ctx.method_51443();
        class_327 font = mc.field_1772;
        try { if (Config.on(Mods.LOWHP)) lowHealth(ctx, mc, w, h); } catch (Throwable ignored) {}
        try { if (Config.on(Mods.HITMARK)) hitMarker(ctx, w, h); } catch (Throwable ignored) {}
        try { if (Config.on(Mods.COOLDOWN)) cooldown(ctx, mc, w, h); } catch (Throwable ignored) {}
        try { if (Config.on(Mods.SPEED)) speedHud(ctx, font, h); } catch (Throwable ignored) {}
    }

    private static void lowHealth(class_332 ctx, class_310 mc, int w, int h) {
        float hp = mc.field_1724.method_6032();
        float limit = Mods.v(Mods.LOWHP, "hearts") * 2f;
        if (hp <= 0f || hp > limit) return;
        float sev = 1f - hp / limit;
        float pulse = 0.65f + 0.35f * (float) Math.sin(System.currentTimeMillis() / 180.0);
        int layers = 14;
        for (int i = 0; i < layers; i++) {
            float f = 1f - i / (float) layers;
            int alpha = (int) (95 * sev * pulse * f * f);
            if (alpha <= 0) continue;
            int c = a(0xFF1B1B, alpha);
            ctx.method_25294(i * 3, i * 3, w - i * 3, i * 3 + 3, c);
            ctx.method_25294(i * 3, h - i * 3 - 3, w - i * 3, h - i * 3, c);
            ctx.method_25294(i * 3, i * 3 + 3, i * 3 + 3, h - i * 3 - 3, c);
            ctx.method_25294(w - i * 3 - 3, i * 3 + 3, w - i * 3, h - i * 3 - 3, c);
        }
    }

    private static void hitMarker(class_332 ctx, int w, int h) {
        long dt = System.currentTimeMillis() - markerAt;
        if (markerAt == 0 || dt > 320) return;
        float t = dt / 320f;
        int alpha = (int) (255 * (1f - t));
        int size = Math.round(Mods.v(Mods.HITMARK, "size"));
        int gap = 3 + (int) (t * 3);
        int cx = w / 2, cy = h / 2;
        int col = a(0xFFFFFF, alpha);
        int sh = a(0x000000, alpha / 2);
        for (int i = gap; i < gap + size; i++) {
            for (int sx = -1; sx <= 1; sx += 2) {
                for (int sy = -1; sy <= 1; sy += 2) {
                    int x = cx + sx * i, y = cy + sy * i;
                    ctx.method_25294(x + 1, y + 1, x + 2, y + 2, sh);
                    ctx.method_25294(x, y, x + 1, y + 1, col);
                }
            }
        }
    }

    private static void cooldown(class_332 ctx, class_310 mc, int w, int h) {
        float prog = mc.field_1724.method_7261(0f);
        if (prog >= 0.999f) return;
        int bw = 36, x = w / 2 - bw / 2, y = h / 2 + 14;
        ctx.method_25294(x - 1, y - 1, x + bw + 1, y + 4, a(0x000000, 150));
        ctx.method_25294(x, y, x + bw, y + 3, a(0x2A3148, 255));
        int fill = Math.round(bw * Math.max(0f, prog));
        int col = prog > 0.9f ? 0x55FF77 : Config.accent;
        ctx.method_25294(x, y, x + fill, y + 3, a(col, 255));
    }

    private static void speedHud(class_332 ctx, class_327 font, int h) {
        String s = String.format("%.2f b/s", speed);
        float sc = Config.scale;
        var m = ctx.method_51448();
        m.method_22903();
        m.method_22905(sc, sc, 1f);
        int ph = (int) (h / sc);
        int tw = font.method_1727(s);
        int x = 6, y = ph - 22;
        ctx.method_25294(x, y, x + tw + 10, y + 14, a(0x0B0F1C, 190));
        ctx.method_25294(x, y, x + 2, y + 14, a(Config.accent, 255));
        ctx.method_51433(font, s, x + 6, y + 3, 0xFFFFFFFF, true);
        m.method_22909();
    }
}
