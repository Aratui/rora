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

    static KeyBinding essenceKey, pearlKey;
    static final Map<String, CdEntry> CDS = new LinkedHashMap<>();
    static long tick;

    @Override
    public void onInitializeClient() {
        essenceKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("key.aurora.essence", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R, "category.aurora"));
        pearlKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("key.aurora.pearl", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G, "category.aurora"));

        ClientPlayNetworking.registerGlobalReceiver(CdPayload.ID, (payload, ctx) ->
                CDS.put(payload.key(), new CdEntry(payload.label(), payload.ticks(), tick + payload.ticks())));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            tick++;
            if (client.player == null) { CDS.clear(); return; }
            while (essenceKey.wasPressed()) ClientPlayNetworking.send(new CastPayload(0));
            while (pearlKey.wasPressed()) ClientPlayNetworking.send(new CastPayload(1));
        });

        HudRenderCallback.EVENT.register(AuroraClient::render);
    }

    /** Barras de recarga no canto inferior direito da tela. */
    static void render(DrawContext g, RenderTickCounter tc) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.options.hudHidden || CDS.isEmpty()) return;
        int x = g.getScaledWindowWidth() - 160;
        int y = g.getScaledWindowHeight() - 40;
        Iterator<CdEntry> it = CDS.values().iterator();
        while (it.hasNext()) {
            CdEntry e = it.next();
            long rem = e.end() - tick;
            if (rem < -40) { it.remove(); continue; }
            boolean ready = rem <= 0;
            String txt = e.label() + (ready ? " - PRONTA!" : " - " + String.format("%.1fs", rem / 20.0));
            g.drawTextWithShadow(mc.textRenderer, txt, x, y, ready ? 0x55FF55 : 0xFFFFFF);
            g.fill(x, y + 10, x + 150, y + 13, 0x90000000);
            int w = ready ? 150 : (int) (150 * (1.0 - (double) rem / e.total()));
            g.fill(x, y + 10, x + w, y + 13, ready ? 0xFF55FF55 : 0xFFFFAA00);
            y -= 18;
        }
    }
}
