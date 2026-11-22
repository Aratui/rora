package br.guardians.aurora;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
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

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            tick++;
            if (client.player == null) { CDS.clear(); SEL.clear(); return; }
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
        if (mc.options.hudHidden || (CDS.isEmpty() && SEL.isEmpty())) return;
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
}
