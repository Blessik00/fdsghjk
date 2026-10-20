package dev.pulse.pvp;

import net.minecraft.class_2561;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_437;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** PulsePvP menu: sidebar with categories, module cards with switches, settings panel on the right. */
public class ClickGuiScreen extends class_437 {
    // ---- layout
    private static final int PW_MAX = 440, PH_MAX = 270, SIDE = 46, SETW = 164, HEAD = 30, FOOT = 28, CARD_H = 26, GAP = 6;

    // ---- palette
    private static final int BG = 0x0D1120, BG2 = 0x0A0D19, CARD = 0x151B2E, CARD_HOVER = 0x1B2340, EDGE = 0x232A47;
    private static final int TEXT = 0xFFFFFF, MUTED = 0x8D96B5, DIM = 0x3B4366, PINK = 0xFF5C8A;

    // ---- icons
    private static final String[] ICON_EYE = {"  #####  ", " #     # ", "#  ###  #", "# ##### #", "#  ###  #", " #     # ", "  #####  "};
    private static final String[] ICON_HUD = {"    #    ", "    #    ", "    #    ", "         ", "###   ###", "         ", "    #    ", "    #    ", "    #    "};
    private static final String[] ICON_ANIM = {"       ##", "      ###", "     ### ", "#   ###  ", "##  ##   ", " ####    ", "  ##     ", " ####    ", "##  ##   "};
    private static final String[] ICON_HEART = {" ##   ## ", "#### ####", "#########", "#########", " ####### ", "  #####  ", "   ###   ", "    #    "};
    private static final String[] ICON_GEAR = {"    #    ", " ## # ## ", " ####### ", "  ## ##  ", "###   ###", "  ## ##  ", " ####### ", " ## # ## ", "    #    "};
    private static final String[] ICON_CROWN = {"#    #    #", "##   #   ##", "###  #  ###", "####   ####", "###########", "###########", "###########", "###########"};
    private static final String[] ICON_X = {"##   ##", " ## ## ", "  ###  ", "  ###  ", " ## ## ", "##   ##"};
    private static final String[][] TAB_ICONS = {ICON_EYE, ICON_HUD, ICON_ANIM, ICON_HEART, ICON_GEAR};
    private static final String[] TAB_NAMES = {Mods.VISUALS, Mods.HUD, Mods.ANIM, "Favorites", "Settings"};

    // ---- hit regions (rebuilt every frame)
    private static final int K_TAB = 0, K_CARD = 1, K_HEART = 2, K_GEAR = 3, K_CLOSE = 4, K_SLIDER = 5, K_CHIP = 6,
            K_TOG = 7, K_ACCENT = 8, K_UISCALE = 9, K_RESET = 10, K_SEARCH = 11;

    private static final class Hit {
        int kind, x, y, w, h, idx;
        String mod;
        Mods.Setting s;
        Hit(int kind, int x, int y, int w, int h) { this.kind = kind; this.x = x; this.y = y; this.w = w; this.h = h; }
        boolean in(double mx, double my) { return mx >= x && mx < x + w && my >= y && my < y + h; }
    }

    private final List<Hit> hits = new ArrayList<>();
    private final Map<String, Float> anim = new HashMap<>();
    private Hit drag;

    private float open;
    private long lastFrame = System.nanoTime();
    private int tab;
    private String sel;
    private float selAnim;
    private double scroll;
    private double maxScroll;
    private String query = "";
    private boolean searching;

    private int px, py, pw, ph;

    public ClickGuiScreen() {
        super(class_2561.method_43470("PulsePvP"));
    }

    // ------------------------------------------------------------ helpers
    private static int a(int rgb, float alpha) {
        int al = Math.max(0, Math.min(255, Math.round(alpha * 255f)));
        return (al << 24) | (rgb & 0xFFFFFF);
    }

    private static int solid(int rgb) { return 0xFF000000 | (rgb & 0xFFFFFF); }

    private static int mix(int c1, int c2, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int r = (int) (((c1 >> 16) & 255) * (1 - t) + ((c2 >> 16) & 255) * t);
        int g = (int) (((c1 >> 8) & 255) * (1 - t) + ((c2 >> 8) & 255) * t);
        int b = (int) ((c1 & 255) * (1 - t) + (c2 & 255) * t);
        return (r << 16) | (g << 8) | b;
    }

    private float ease(String key, boolean target, float dt) {
        float v = anim.getOrDefault(key, target ? 1f : 0f);
        float goal = target ? 1f : 0f;
        v += (goal - v) * Math.min(1f, dt * 14f);
        if (Math.abs(goal - v) < 0.004f) v = goal;
        anim.put(key, v);
        return v;
    }

    private static void rr(class_332 c, int x, int y, int w, int h, int r, int col) {
        Ui.rrect(c, x, y, w, h, r, col);
    }

    private static void icon(class_332 c, String[] rows, int x, int y, int col) {
        for (int j = 0; j < rows.length; j++) {
            String row = rows[j];
            for (int i = 0; i < row.length(); i++) {
                if (row.charAt(i) == '#') c.method_25294(x + i, y + j, x + i + 1, y + j + 1, col);
            }
        }
    }

    private static int iconW(String[] rows) { return rows[0].length(); }

    private int accent() { return Config.accent & 0xFFFFFF; }

    private List<Mods.Mod> visibleMods() {
        List<Mods.Mod> out = new ArrayList<>();
        String q = query.trim().toLowerCase();
        for (Mods.Mod m : Mods.ALL) {
            if (!q.isEmpty()) {
                if (m.name.toLowerCase().contains(q) || m.desc.toLowerCase().contains(q)) out.add(m);
            } else if (tab < 3) {
                if (m.category.equals(Mods.CATEGORIES[tab])) out.add(m);
            } else if (tab == 3) {
                if (Config.FAVORITES.contains(m.name)) out.add(m);
            }
        }
        return out;
    }

    // ------------------------------------------------------------ screen hooks
    @Override
    public boolean method_25421() { return false; }

    @Override
    public void method_25420(class_332 ctx, int mx, int my, float delta) { /* custom background in render */ }

    @Override
    public void method_25394(class_332 ctx, int mx, int my, float delta) {
        long now = System.nanoTime();
        float dt = Math.min(0.1f, (now - lastFrame) / 1_000_000_000f);
        lastFrame = now;
        open = Math.min(1f, open + dt * 6f);
        float o = 1f - (1f - open) * (1f - open);
        selAnim += ((sel != null ? 1f : 0f) - selAnim) * Math.min(1f, dt * 14f);
        if (Math.abs((sel != null ? 1f : 0f) - selAnim) < 0.004f) selAnim = sel != null ? 1f : 0f;

        class_327 font = this.field_22793;
        pw = Math.min(PW_MAX, this.field_22789 - 12);
        ph = Math.min(PH_MAX, this.field_22790 - 12);
        px = (this.field_22789 - pw) / 2;
        py = (this.field_22790 - ph) / 2 + Math.round((1f - o) * 10f);
        hits.clear();

        ctx.method_25294(0, 0, this.field_22789, this.field_22790, a(0x000000, 0.52f * o));

        // window
        int acc = accent();
        rr(ctx, px - 3, py - 3, pw + 6, ph + 6, 12, a(acc, 0.10f * o));
        rr(ctx, px - 1, py - 1, pw + 2, ph + 2, 10, a(0x2A3260, o));
        rr(ctx, px, py, pw, ph, 9, a(BG, 0.97f * o));

        drawSidebar(ctx, font, mx, my, o, dt);
        drawMain(ctx, font, mx, my, o, dt);
        drawSettingsPanel(ctx, font, mx, my, o, dt);
    }

    // ------------------------------------------------------------ sidebar
    private void drawSidebar(class_332 ctx, class_327 font, int mx, int my, float o, float dt) {
        int acc = accent();
        rr(ctx, px + 5, py + 5, SIDE - 8, ph - 10, 7, a(BG2, o));
        icon(ctx, ICON_CROWN, px + 5 + (SIDE - 8 - 11) / 2, py + 14, a(acc, o));
        ctx.method_25294(px + 12, py + 29, px + SIDE - 7, py + 30, a(EDGE, o));

        int bx = px + 9, bw = SIDE - 16;
        for (int i = 0; i < 5; i++) {
            int by = i < 4 ? py + 38 + i * 30 : py + ph - 38;
            if (i == 4) ctx.method_25294(px + 12, by - 7, px + SIDE - 7, by - 6, a(EDGE, o));
            boolean selected = (tab == i) && query.isEmpty();
            boolean hover = mx >= bx && mx < bx + bw && my >= by && my < by + 24;
            float t = ease("tab" + i, selected, dt);
            if (t > 0.01f) {
                rr(ctx, bx - 1, by - 1, bw + 2, 26, 7, a(acc, 0.55f * t * o));
                rr(ctx, bx, by, bw, 24, 6, a(mix(BG2, acc, 0.18f), o));
            } else if (hover) {
                rr(ctx, bx, by, bw, 24, 6, a(CARD, o));
            }
            String[] ic = TAB_ICONS[i];
            int col = selected ? solid(mix(0xFFFFFF, acc, 0.35f)) : (hover ? solid(0xC5CCE6) : solid(0x6C759A));
            icon(ctx, ic, bx + (bw - iconW(ic)) / 2, by + (24 - ic.length) / 2, a(col, o));
            Hit h = new Hit(K_TAB, bx, by, bw, 24);
            h.idx = i;
            hits.add(h);
        }
    }

    // ------------------------------------------------------------ main area
    private void drawMain(class_332 ctx, class_327 font, int mx, int my, float o, float dt) {
        int acc = accent();
        int mainX = px + SIDE + 8;
        int mainW = pw - SIDE - 16 - Math.round(SETW * selAnim);
        int top = py + HEAD;
        int bottom = py + ph - FOOT;

        String title = !query.isEmpty() ? "Search" : TAB_NAMES[tab];
        ctx.method_51433(font, title, mainX + 2, py + 11, a(MUTED, o), false);

        if (tab == 4 && query.isEmpty()) {
            drawInterfacePage(ctx, font, mainX, top, mainW, mx, my, o);
        } else {
            List<Mods.Mod> list = visibleMods();
            int colW = (mainW - GAP) / 2;
            int rows = (list.size() + 1) / 2;
            int content = rows * (CARD_H + GAP);
            maxScroll = Math.max(0, content - (bottom - top) + 4);
            scroll = Math.max(0, Math.min(maxScroll, scroll));

            if (list.isEmpty()) {
                String msg = tab == 3 && query.isEmpty() ? "Нажми на сердечко у модуля, чтобы добавить его сюда" : "Ничего не найдено";
                ctx.method_25300(font, msg, mainX + mainW / 2, top + 40, a(MUTED, o));
            }

            ctx.method_44379(mainX - 4, top, mainX + mainW + 4, bottom);
            for (int i = 0; i < list.size(); i++) {
                Mods.Mod m = list.get(i);
                int cx = mainX + (i % 2) * (colW + GAP);
                int cy = top + (i / 2) * (CARD_H + GAP) - (int) scroll;
                if (cy + CARD_H < top - 4 || cy > bottom + 4) continue;
                drawCard(ctx, font, m, cx, cy, colW, mx, my, o, dt, top, bottom);
            }
            ctx.method_44380();

            // scrollbar
            if (maxScroll > 0) {
                int trackH = bottom - top;
                int barH = Math.max(18, (int) (trackH * (trackH / (double) (trackH + maxScroll))));
                int barY = top + (int) ((trackH - barH) * (scroll / maxScroll));
                rr(ctx, mainX + mainW + 3, barY, 2, barH, 1, a(acc, 0.7f * o));
            }
        }

        // search box
        int sx = mainX, sy = py + ph - FOOT + 6, sw = 138, sh = 15;
        rr(ctx, sx - 1, sy - 1, sw + 2, sh + 2, 5, a(searching ? acc : EDGE, o));
        rr(ctx, sx, sy, sw, sh, 4, a(BG2, o));
        icon(ctx, new String[]{" ###  ", "#   # ", "#   # ", "#   # ", " ###  ", "    ##"}, sx + 5, sy + 4, a(MUTED, o));
        if (query.isEmpty() && !searching) {
            ctx.method_51433(font, "Search  CTRL+F", sx + 16, sy + 4, a(DIM + 0x1A1A2A, o), false);
        } else {
            String shown = query + ((System.currentTimeMillis() / 500) % 2 == 0 && searching ? "_" : "");
            ctx.method_51433(font, shown, sx + 16, sy + 4, a(TEXT, o), false);
        }
        hits.add(new Hit(K_SEARCH, sx, sy, sw, sh));
    }

    private void drawCard(class_332 ctx, class_327 font, Mods.Mod m, int x, int y, int w, int mx, int my, float o, float dt, int top, int bottom) {
        int acc = accent();
        boolean on = Config.on(m.name);
        float t = ease("m" + m.name, on, dt);
        boolean hover = mx >= x && mx < x + w && my >= y && my < y + CARD_H && my >= top && my < bottom;

        if (t > 0.01f) {
            rr(ctx, x - 3, y - 3, w + 6, CARD_H + 6, 9, a(acc, 0.13f * t * o));
            rr(ctx, x - 1, y - 1, w + 2, CARD_H + 2, 7, a(mix(EDGE, acc, 0.85f * t), o));
        } else {
            rr(ctx, x - 1, y - 1, w + 2, CARD_H + 2, 7, a(EDGE, o));
        }
        rr(ctx, x, y, w, CARD_H, 6, a(hover ? CARD_HOVER : CARD, o));

        // heart
        boolean fav = Config.FAVORITES.contains(m.name);
        int hx = x + 7, hy = y + (CARD_H - 8) / 2;
        icon(ctx, ICON_HEART, hx, hy, a(fav ? PINK : DIM, o));
        if (!fav && !hover) { /* keep dim */ }

        // name
        int nameCol = mix(MUTED, TEXT, t);
        ctx.method_51433(font, m.name, x + 22, y + (CARD_H - 8) / 2, a(nameCol, o), false);

        // toggle switch
        int tw = 24, th = 12, tx = x + w - tw - 7, ty = y + (CARD_H - th) / 2;
        rr(ctx, tx, ty, tw, th, 6, a(mix(0x2A3148, acc, t), o));
        int kx = tx + 2 + Math.round((tw - th) * t);
        rr(ctx, kx, ty + 2, 8, 8, 4, a(mix(0x9099B8, 0xFFFFFF, t), o));

        // gear
        int gx = tx - 16, gy = y + (CARD_H - 9) / 2;
        boolean hasSettings = !m.settings.isEmpty();
        if (hasSettings) {
            boolean selected = m.name.equals(sel);
            boolean gh = mx >= gx - 3 && mx < gx + 12 && my >= y && my < y + CARD_H && my >= top && my < bottom;
            icon(ctx, ICON_GEAR, gx, gy, a(selected || gh ? acc : MUTED, o));
        }

        if (my >= top && my < bottom && y + CARD_H > top && y < bottom) {
            Hit card = new Hit(K_CARD, x, Math.max(y, top), w, Math.min(y + CARD_H, bottom) - Math.max(y, top));
            card.mod = m.name;
            Hit heart = new Hit(K_HEART, x + 2, y, 18, CARD_H);
            heart.mod = m.name;
            hits.add(card);
            // later hits take priority in click handling
            if (hasSettings) {
                Hit gear = new Hit(K_GEAR, gx - 4, y, 16, CARD_H);
                gear.mod = m.name;
                hits.add(gear);
            }
            hits.add(heart);
        }
    }

    // ------------------------------------------------------------ interface page
    private void drawInterfacePage(class_332 ctx, class_327 font, int x, int y, int w, int mx, int my, float o) {
        int acc = accent();
        ctx.method_51433(font, "Цвет акцента", x + 2, y + 4, a(TEXT, o), false);
        for (int i = 0; i < Config.ACCENTS.length; i++) {
            int c = Config.ACCENTS[i] & 0xFFFFFF;
            int sx = x + 2 + i * 30, sy = y + 18;
            boolean cur = (Config.accent & 0xFFFFFF) == c;
            if (cur) rr(ctx, sx - 2, sy - 2, 28, 28, 8, a(0xFFFFFF, o));
            rr(ctx, sx, sy, 24, 24, 6, a(c, o));
            Hit h = new Hit(K_ACCENT, sx, sy, 24, 24);
            h.idx = i;
            hits.add(h);
        }
        int sy2 = y + 62;
        ctx.method_51433(font, "Размер HUD", x + 2, sy2, a(TEXT, o), false);
        String val = String.format("%.2f", Config.scale);
        ctx.method_51433(font, val, x + w - font.method_1727(val) - 2, sy2, a(MUTED, o), false);
        int tx = x + 2, tw = w - 4, ty = sy2 + 14;
        float t = (Config.scale - 0.7f) / 0.8f;
        drawTrack(ctx, tx, ty, tw, t, o);
        hits.add(new Hit(K_UISCALE, tx, ty - 5, tw, 14));

        ctx.method_51433(font, "Открыть меню: Правый Shift (меняется в настройках управления)", x + 2, sy2 + 40, a(MUTED, o * 0.8f), false);
        ctx.method_51433(font, "PulsePvP 1.1.0", x + 2, sy2 + 54, a(DIM + 0x303040, o), false);
    }

    private void drawTrack(class_332 ctx, int x, int y, int w, float t, float o) {
        int acc = accent();
        t = Math.max(0f, Math.min(1f, t));
        rr(ctx, x, y, w, 4, 2, a(0x2A3148, o));
        int fw = Math.max(4, Math.round(w * t));
        rr(ctx, x, y, fw, 4, 2, a(acc, o));
        int kx = x + Math.round((w - 8) * t);
        rr(ctx, kx, y - 2, 8, 8, 4, a(0xFFFFFF, o));
    }

    // ------------------------------------------------------------ settings panel
    private void drawSettingsPanel(class_332 ctx, class_327 font, int mx, int my, float o, float dt) {
        if (sel == null && selAnim <= 0.01f) return;
        Mods.Mod m = Mods.get(sel != null ? sel : lastSel);
        if (m == null) return;
        if (sel != null) lastSel = sel;
        int acc = accent();

        int w = Math.round(SETW * selAnim);
        int x = px + pw - w - 5, y = py + 5, h = ph - 10;
        if (w < 40) return;
        float po = o * Math.min(1f, selAnim * 1.4f);

        ctx.method_44379(x, y, x + w, y + h);
        rr(ctx, x, y, SETW - 6, h, 7, a(BG2, po));
        int ix = x + 10, iw = SETW - 6 - 20;

        // breadcrumb
        icon(ctx, TAB_ICONS[Math.max(0, Math.min(2, categoryIndex(m)))], ix, y + 9, a(MUTED, po));
        ctx.method_51433(font, m.category + " / ", ix + 13, y + 9, a(MUTED, po), false);
        ctx.method_51433(font, m.name, ix + 13 + font.method_1727(m.category + " / "), y + 9, a(TEXT, po), false);
        boolean xh = mx >= x + SETW - 28 && mx < x + SETW - 12 && my >= y + 5 && my < y + 21;
        icon(ctx, ICON_X, x + SETW - 24, y + 10, a(xh ? TEXT : MUTED, po));
        Hit close = new Hit(K_CLOSE, x + SETW - 28, y + 5, 18, 18);
        hits.add(close);
        ctx.method_25294(x + 8, y + 24, x + SETW - 14, y + 25, a(EDGE, po));

        int cy = y + 32;
        // description
        for (String line : wrap(font, m.desc, iw)) {
            ctx.method_51433(font, line, ix, cy, a(MUTED, po * 0.9f), false);
            cy += 10;
        }
        cy += 8;

        for (Mods.Setting s : m.settings) {
            float val = Mods.v(m.name, s.key);
            if (s.type == Mods.SLIDER) {
                ctx.method_51433(font, s.label, ix, cy, a(TEXT, po), false);
                String txt = fmt(s, val);
                ctx.method_51433(font, txt, ix + iw - font.method_1727(txt), cy, a(MUTED, po), false);
                float t = (val - s.min) / (s.max - s.min);
                drawTrack(ctx, ix, cy + 14, iw, t, po);
                Hit hh = new Hit(K_SLIDER, ix, cy + 9, iw, 14);
                hh.mod = m.name; hh.s = s;
                hits.add(hh);
                cy += 27;
            } else if (s.type == Mods.TOGGLE) {
                ctx.method_51433(font, s.label, ix, cy + 1, a(TEXT, po), false);
                boolean on = val > 0.5f;
                float t = ease("t" + m.name + s.key, on, dt);
                int tw = 24, th = 12, tx = ix + iw - tw;
                rr(ctx, tx, cy - 1, tw, th, 6, a(mix(0x2A3148, acc, t), po));
                rr(ctx, tx + 2 + Math.round((tw - th) * t), cy + 1, 8, 8, 4, a(mix(0x9099B8, 0xFFFFFF, t), po));
                Hit hh = new Hit(K_TOG, ix, cy - 2, iw, 16);
                hh.mod = m.name; hh.s = s;
                hits.add(hh);
                cy += 22;
            } else {
                ctx.method_51433(font, s.label, ix, cy, a(TEXT, po), false);
                cy += 13;
                int cxp = ix;
                for (int i = 0; i < s.options.length; i++) {
                    int cw = font.method_1727(s.options[i]) + 10;
                    if (cxp + cw > ix + iw) { cxp = ix; cy += 16; }
                    boolean cur = Math.round(val) == i;
                    boolean hv = mx >= cxp && mx < cxp + cw && my >= cy && my < cy + 14;
                    if (cur) rr(ctx, cxp - 1, cy - 1, cw + 2, 16, 6, a(acc, 0.9f * po));
                    rr(ctx, cxp, cy, cw, 14, 5, a(cur ? mix(BG2, acc, 0.3f) : (hv ? CARD_HOVER : CARD), po));
                    ctx.method_51433(font, s.options[i], cxp + 5, cy + 3, a(cur ? TEXT : MUTED, po), false);
                    Hit hh = new Hit(K_CHIP, cxp, cy, cw, 14);
                    hh.mod = m.name; hh.s = s; hh.idx = i;
                    hits.add(hh);
                    cxp += cw + 4;
                }
                cy += 22;
            }
        }

        // reset button
        int by = y + h - 24;
        boolean rh = mx >= ix && mx < ix + 64 && my >= by && my < by + 15;
        rr(ctx, ix - 1, by - 1, 66, 17, 6, a(rh ? acc : EDGE, po));
        rr(ctx, ix, by, 64, 15, 5, a(CARD, po));
        ctx.method_25300(font, "Сброс", ix + 32, by + 4, a(rh ? TEXT : MUTED, po));
        hits.add(new Hit(K_RESET, ix, by, 64, 15));
        ctx.method_44380();
    }

    private String lastSel;

    private int categoryIndex(Mods.Mod m) {
        for (int i = 0; i < Mods.CATEGORIES.length; i++) if (Mods.CATEGORIES[i].equals(m.category)) return i;
        return 0;
    }

    private static String fmt(Mods.Setting s, float v) {
        if (s.step >= 1f) return String.valueOf(Math.round(v));
        if (s.step >= 0.1f) return String.format("%.1f", v);
        return String.format("%.2f", v);
    }

    private static List<String> wrap(class_327 font, String text, int width) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        for (String word : text.split(" ")) {
            String t = cur.length() == 0 ? word : cur + " " + word;
            if (font.method_1727(t) > width && cur.length() > 0) {
                out.add(cur.toString());
                cur = new StringBuilder(word);
            } else {
                cur = new StringBuilder(t);
            }
        }
        if (cur.length() > 0) out.add(cur.toString());
        return out;
    }

    // ------------------------------------------------------------ input
    private void setSlider(Hit h, double mx) {
        float t = (float) Math.max(0, Math.min(1, (mx - h.x - 4) / (double) (h.w - 8)));
        if (h.kind == K_UISCALE) {
            float v = 0.7f + t * 0.8f;
            Config.scale = Math.round(v * 20f) / 20f;
            return;
        }
        Mods.Setting s = h.s;
        float v = s.min + t * (s.max - s.min);
        if (s.step > 0) v = Math.round(v / s.step) * s.step;
        v = Math.max(s.min, Math.min(s.max, v));
        v = Math.round(v * 1000f) / 1000f;
        Config.VALUES.put(h.mod + "." + s.key, v);
    }

    @Override
    public boolean method_25402(double mx, double my, int button) {
        if (button == 0 || button == 1) {
            for (int i = hits.size() - 1; i >= 0; i--) {
                Hit h = hits.get(i);
                if (!h.in(mx, my)) continue;
                boolean left = button == 0;
                switch (h.kind) {
                    case K_TAB:
                        if (!left) break;
                        tab = h.idx; scroll = 0; query = ""; searching = false;
                        if (tab == 4) sel = null;
                        return true;
                    case K_HEART:
                        if (!left) break;
                        if (!Config.FAVORITES.remove(h.mod)) Config.FAVORITES.add(h.mod);
                        return true;
                    case K_GEAR:
                        sel = h.mod.equals(sel) ? null : h.mod;
                        return true;
                    case K_CARD:
                        if (left) {
                            Config.MODULES.put(h.mod, !Config.on(h.mod));
                        } else {
                            Mods.Mod m = Mods.get(h.mod);
                            if (m != null && !m.settings.isEmpty()) sel = h.mod.equals(sel) ? null : h.mod;
                        }
                        return true;
                    case K_CLOSE:
                        sel = null;
                        return true;
                    case K_SLIDER:
                    case K_UISCALE:
                        if (!left) break;
                        drag = h;
                        setSlider(h, mx);
                        return true;
                    case K_CHIP:
                        if (!left) break;
                        Config.VALUES.put(h.mod + "." + h.s.key, (float) h.idx);
                        return true;
                    case K_TOG:
                        if (!left) break;
                        Config.VALUES.put(h.mod + "." + h.s.key, Mods.v(h.mod, h.s.key) > 0.5f ? 0f : 1f);
                        return true;
                    case K_ACCENT:
                        if (!left) break;
                        Config.accent = Config.ACCENTS[h.idx];
                        return true;
                    case K_RESET: {
                        if (!left) break;
                        Mods.Mod m = Mods.get(sel != null ? sel : lastSel);
                        if (m != null) for (Mods.Setting s : m.settings) Config.VALUES.remove(m.name + "." + s.key);
                        return true;
                    }
                    case K_SEARCH:
                        searching = true;
                        return true;
                    default:
                        break;
                }
            }
            searching = false;
        }
        return super.method_25402(mx, my, button);
    }

    @Override
    public boolean method_25403(double mx, double my, int button, double dx, double dy) {
        if (drag != null) {
            setSlider(drag, mx);
            return true;
        }
        return super.method_25403(mx, my, button, dx, dy);
    }

    @Override
    public boolean method_25406(double mx, double my, int button) {
        if (drag != null) {
            drag = null;
            Config.save();
            return true;
        }
        return super.method_25406(mx, my, button);
    }

    @Override
    public boolean method_25401(double mx, double my, double h, double v) {
        scroll -= v * 16;
        scroll = Math.max(0, Math.min(maxScroll, scroll));
        return true;
    }

    @Override
    public boolean method_25404(int key, int scan, int mods) {
        boolean ctrl = (mods & 2) != 0;
        if (ctrl && key == 70) { // Ctrl+F
            searching = true;
            return true;
        }
        if (searching) {
            if (key == 256) { // esc
                if (!query.isEmpty()) query = ""; else searching = false;
                return true;
            }
            if (key == 259) { // backspace
                if (!query.isEmpty()) query = query.substring(0, query.length() - 1);
                scroll = 0;
                return true;
            }
            if (key == 257 || key == 335) { searching = false; return true; }
            char c = 0;
            if (key >= 65 && key <= 90) c = (char) ('a' + key - 65);
            else if (key >= 48 && key <= 57) c = (char) key;
            else if (key == 32) c = ' ';
            else if (key == 45) c = '-';
            if (c != 0 && query.length() < 20) {
                query += c;
                scroll = 0;
            }
            return true;
        }
        if (key == 47) { // '/'
            searching = true;
            return true;
        }
        if (key == 344) { // right shift closes
            method_25419();
            return true;
        }
        return super.method_25404(key, scan, mods);
    }

    @Override
    public void method_25432() {
        Config.save();
        super.method_25432();
    }
}
