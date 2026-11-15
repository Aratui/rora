package br.guardians.aurora;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

/** Temporada 2: deuses gregos. O poder escala com o nível (1 a 10). */
public enum God {
    ZEUS("zeus", "Zeus", (p, w, l) -> {
        int n = 1 + l / 3;
        for (LivingEntity e : Fx.near(p, 12)) {
            if (n-- <= 0) break;
            Fx.bolt(w, e.getPos(), p);
        }
    }),
    POSEIDON("poseidon", "Poseidon", (p, w, l) -> {
        for (LivingEntity e : Fx.near(p, 6)) {
            Vec3d d = e.getPos().subtract(p.getPos()).normalize().multiply(1.2 + l * 0.1);
            e.addVelocity(d.x, 0.4, d.z);
            e.velocityModified = true;
        }
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.WATER_BREATHING, 200 + l * 40, 0));
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.DOLPHINS_GRACE, 200 + l * 40, 0));
    }),
    HADES("hades", "Hades", (p, w, l) -> {
        for (LivingEntity e : Fx.near(p, 8))
            e.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 60 + l * 10, l / 4));
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 100 + l * 20, 0));
    }),
    ATHENA("athena", "Atena", (p, w, l) -> {
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 120 + l * 20, l / 4));
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 300, l / 3));
    }),
    APOLLO("apollo", "Apolo", (p, w, l) -> {
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 100 + l * 20, l / 4));
        for (LivingEntity e : Fx.near(p, 15))
            e.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 200, 0));
    }),
    ARES("ares", "Ares", (p, w, l) ->
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 120 + l * 20, l / 3))),
    HERMES("hermes", "Hermes", (p, w, l) -> {
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 120 + l * 20, 1 + l / 4));
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 120 + l * 20, 1));
    }),
    ARTEMIS("artemis", "Ártemis", (p, w, l) -> {
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 400, 0));
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 120 + l * 20, 0));
        for (LivingEntity e : Fx.near(p, 12 + l))
            e.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 200, 0));
    });

    public interface Ability { void cast(ServerPlayerEntity p, ServerWorld w, int level); }

    public static final int MAX_LEVEL = 10;
    public final String key, label;
    public final Ability ability;

    God(String key, String label, Ability ability) {
        this.key = key; this.label = label; this.ability = ability;
    }

    public static God byKey(String k) {
        for (God g : values()) if (g.key.equals(k)) return g;
        return null;
    }
}
