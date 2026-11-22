package br.guardians.aurora;

import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Formas temporárias: "small" (pequeno), "giant" (grande) e "bat" (morcego: pequeno e voando). */
public class Forms {
    record Form(String type, long end) {}

    static final Map<UUID, Form> ACTIVE = new HashMap<>();
    static final Identifier MOD_ID = AuroraMod.id("form_scale");
    static final String BAT_TAG = "aurora_bat";

    static void scale(ServerPlayerEntity p, double v) {
        EntityAttributeInstance a = p.getAttributeInstance(EntityAttributes.GENERIC_SCALE);
        if (a == null) return;
        a.removeModifier(MOD_ID);
        a.addTemporaryModifier(new EntityAttributeModifier(MOD_ID, v, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }

    public static void start(ServerPlayerEntity p, String type, int ticks) {
        end(p);
        switch (type) {
            case "small" -> scale(p, -0.65);
            case "giant" -> scale(p, 1.0);
            case "bat" -> {
                scale(p, -0.7);
                if (!p.isCreative() && !p.isSpectator()) {
                    p.getAbilities().allowFlying = true;
                    p.getAbilities().flying = true;
                    p.sendAbilitiesUpdate();
                }
                p.addCommandTag(BAT_TAG);
            }
            default -> { }
        }
        ACTIVE.put(p.getUuid(), new Form(type, p.getServer().getTicks() + (long) ticks));
    }

    public static void end(ServerPlayerEntity p) {
        Form f = ACTIVE.remove(p.getUuid());
        EntityAttributeInstance a = p.getAttributeInstance(EntityAttributes.GENERIC_SCALE);
        if (a != null) a.removeModifier(MOD_ID);
        boolean bat = (f != null && f.type().equals("bat")) || p.getCommandTags().contains(BAT_TAG);
        if (bat) {
            p.removeCommandTag(BAT_TAG);
            if (!p.isCreative() && !p.isSpectator()) {
                p.getAbilities().allowFlying = false;
                p.getAbilities().flying = false;
                p.sendAbilitiesUpdate();
                p.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 100, 0));
            }
        }
    }

    /** Ao entrar ou renascer: garante que nada de forma antiga ficou para trás. */
    public static void cleanup(ServerPlayerEntity p) { end(p); }

    public static void tick(MinecraftServer s) {
        if (ACTIVE.isEmpty()) return;
        long now = s.getTicks();
        for (ServerPlayerEntity p : s.getPlayerManager().getPlayerList()) {
            Form f = ACTIVE.get(p.getUuid());
            if (f != null && (now >= f.end() || !p.isAlive())) end(p);
        }
    }
}
