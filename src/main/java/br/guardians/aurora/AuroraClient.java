package br.guardians.aurora;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class AuroraClient implements ClientModInitializer {
    record CdEntry(String label, int total, long end) {}

    static KeyBinding essenceKey, essenceNext, pearlKey, pearlNext;
    static final Map<String, CdEntry> CDS = new LinkedHashMap<>();
    static final Map<String, String> SEL = new LinkedHashMap<>();
    static StatusPayload status;
    static int lastLevel = -1;
    static long flashUntil;
    static long tick;

    static KeyBinding key(String name, int code) {
        return KeyBindingHelper.registerKeyBinding(new KeyBinding(name, InputUtil.Type.KEYSYM, code, "category.aurora"));
    }

    @Override
    public void onInitializeClient() {
        essenceKey = key("key.aurora.essence", GLFW.GLFW_KEY_R);
        essenceNext = key("key.aurora.essence_next", GLFW.GLFW_KEY_V);
        pearlKey = key("key.aurora.pearl", GLFW.GLFW_KEY_G);
        pearlNext = key("key.aurora.pearl_next", GLFW.GLFW_KEY_B);

        ClientPlayNetworking.registerGlobalReceiver(CdPayload.ID, (payload, ctx) ->
                CDS.put(payload.key(), new CdEntry(payload.label(), payload.ticks(), tick + payload.ticks())));
        ClientPlayNetworking.registerGlobalReceiver(SelPayload.ID, (payload, ctx) -> {
            if (payload.text().isEmpty()) SEL.remove(payload.key());
            else SEL.put(payload.key(), payload.text());
        });

        ClientPlayNetworking.registerGlobalReceiver(StatusPayload.ID, (payload, ctx) -> {
            if (lastLevel != -1 && payload.level() > lastLevel) flashUntil = tick + 40;
            lastLevel = payload.level();
            status = payload;
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            tick++;
            if (client.player == null) { CDS.clear(); SEL.clear(); status = null; lastLevel = -1; return; }
            while (essenceKey.wasPressed()) ClientPlayNetworking.send(new CastPayload(0));
            while (pearlKey.wasPressed()) ClientPlayNetworking.send(new CastPayload(1));
            while (essenceNext.wasPressed()) ClientPlayNetworking.send(new CastPayload(2));
            while (pearlNext.wasPressed()) ClientPlayNetworking.send(new CastPayload(3));
        });

        HudRenderCallback.EVENT.register(AuroraClient::render);
    }

    static String k(KeyBinding b) { return b.getBoundKeyLocalizedText().getString(); }

    /** Canto inferior direito: habilidade selecionada + barras de recarga. */
    static void render(DrawContext g, RenderTickCounter tc) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.options.hudHidden) return;
        panel(g, mc);
        if (CDS.isEmpty() && SEL.isEmpty()) return;
        int right = g.getScaledWindowWidth() - 6;
        int y = g.getScaledWindowHeight() - 14;

        String e = SEL.get("e"), p = SEL.get("p");
        if (p != null) {
            String t = "[" + k(pearlKey) + "/" + k(pearlNext) + "] " + p;
            g.drawTextWithShadow(mc.textRenderer, t, right - mc.textRenderer.getWidth(t), y, 0xFFD27F);
            y -= 12;
        }
        if (e != null) {
            String t = "[" + k(essenceKey) + "/" + k(essenceNext) + "] " + e;
            g.drawTextWithShadow(mc.textRenderer, t, right - mc.textRenderer.getWidth(t), y, 0x8CD2FF);
            y -= 12;
        }
        y -= 14;
        int x = right - 150;
        Iterator<CdEntry> it = CDS.values().iterator();
        while (it.hasNext()) {
            CdEntry c = it.next();
            long rem = c.end() - tick;
            if (rem < -40) { it.remove(); continue; }
            boolean ready = rem <= 0;
            String txt = c.label() + (ready ? " - PRONTA!" : " - " + String.format("%.1fs", rem / 20.0));
            g.drawTextWithShadow(mc.textRenderer, txt, right - mc.textRenderer.getWidth(txt), y, ready ? 0x55FF55 : 0xFFFFFF);
            g.fill(x, y + 10, x + 150, y + 13, 0x90000000);
            int w = ready ? 150 : (int) (150 * (1.0 - (double) rem / c.total()));
            g.fill(x, y + 10, x + w, y + 13, ready ? 0xFF55FF55 : 0xFFFFAA00);
            y -= 18;
        }
    }

    static int teamColor(String t) {
        return switch (t) {
            case "spy" -> 0x7FD4FF;
            case "olympus" -> 0xFFD34D;
            case "gdc" -> 0xFF6B6B;
            default -> 0xFFFFFF;
        };
    }

    static int factionColor(String f) {
        return switch (f) {
            case "arcana" -> 0xC08CFF;
            case "aurora" -> 0x5CF0C8;
            case "hidra" -> 0x7CDB5A;
            default -> 0xFFFFFF;
        };
    }

    /** Painel de status no canto superior esquerdo: deus, nível, time e facção. */
    static void panel(DrawContext g, MinecraftClient mc) {
        StatusPayload s = status;
        if (s == null) return;
        boolean hasGod = !s.god().isEmpty(), hasTeam = !s.team().isEmpty(), hasFac = !s.faction().isEmpty();
        if (!hasGod && !hasTeam && !hasFac && s.level() <= 1) return;

        God god = God.byKey(s.god());
        int rows = (hasTeam ? 1 : 0) + (hasFac ? 1 : 0);
        int x0 = 6, y0 = 6, w = 122, h = 36 + rows * 11;
        int accent = hasTeam ? teamColor(s.team()) : (god != null ? Palette.color("essence_" + god.key) : 0xFFD34D);
        boolean flash = tick < flashUntil && (tick / 4) % 2 == 0;
        int border = flash ? 0xFFFFFFFF : (0xFF000000 | accent);

        g.fill(x0, y0, x0 + w, y0 + h, 0xB0101018);
        g.fill(x0, y0, x0 + w, y0 + 1, border);
        g.fill(x0, y0 + h - 1, x0 + w, y0 + h, border);
        g.fill(x0, y0, x0 + 1, y0 + h, border);
        g.fill(x0 + w - 1, y0, x0 + w, y0 + h, border);

        // ícone (essência do deus) + nome + nível
        if (god != null) g.drawItem(new ItemStack(AuroraItems.ESSENCES.get(god)), x0 + 4, y0 + 4);
        String name = god != null ? god.label : "Sem deus";
        g.drawTextWithShadow(mc.textRenderer, Text.literal(name).setStyle(Style.EMPTY.withFont(Palette.TITLE)),
                x0 + 24, y0 + 5, god != null ? (0xFF000000 | Palette.color("essence_" + god.key)) : 0xAAAAAA);
        boolean max = s.level() >= God.MAX_LEVEL;
        String lv = max ? "Nível MÁX" : "Nível " + s.level();
        g.drawTextWithShadow(mc.textRenderer, lv, x0 + 24, y0 + 17, max ? 0xFFD34D : 0xFFFFFF);

        // barra de nível: 10 segmentos
        for (int i = 0; i < God.MAX_LEVEL; i++) {
            int sx = x0 + 4 + i * 11;
            boolean on = i < s.level();
            g.fill(sx, y0 + 28, sx + 10, y0 + 32, on ? (0xFF000000 | accent) : 0xFF3A3A48);
            if (on) g.fill(sx, y0 + 28, sx + 10, y0 + 29, 0x66FFFFFF);
        }

        int y = y0 + 36;
        if (hasTeam) {
            g.fill(x0 + 4, y + 1, x0 + 8, y + 8, 0xFF000000 | teamColor(s.team()));
            g.drawTextWithShadow(mc.textRenderer, s.team().toUpperCase() + " · " + s.teamPts() + " pts",
                    x0 + 12, y, teamColor(s.team()));
            y += 11;
        }
        if (hasFac) {
            g.fill(x0 + 4, y + 1, x0 + 8, y + 8, 0xFF000000 | factionColor(s.faction()));
            String f = s.faction().substring(0, 1).toUpperCase() + s.faction().substring(1);
            g.drawTextWithShadow(mc.textRenderer, f + " · " + s.facPts() + " pts",
                    x0 + 12, y, factionColor(s.faction()));
        }
    }
}
