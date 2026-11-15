package br.guardians.aurora;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

/** Temporada 1: pérolas com poderes únicos (clique direito). */
public enum Pearl {
    IGNIS("ignis", "Pérola de Ignis", 300, (p, w) -> {
        for (LivingEntity e : Fx.near(p, 6)) {
            e.setOnFireFor(5);
            e.damage(w.getDamageSources().playerAttack(p), 4);
        }
    }),
    ZEFIRO("zefiro", "Pérola de Zéfiro", 120, (p, w) -> Fx.dash(p, 2.2)),
    MARE("mare", "Pérola da Maré", 400, (p, w) -> {
        p.heal(8);
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.WATER_BREATHING, 600, 0));
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 100, 1));
    }),
    TERRA("terra", "Pérola de Terra", 500, (p, w) -> {
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 200, 1));
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 400, 1));
    }),
    TROVAO("trovao", "Pérola do Trovão", 400, (p, w) -> Fx.bolt(w, Fx.target(p, 30), p)),
    SOMBRA("sombra", "Pérola da Sombra", 600, (p, w) -> {
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, 200, 0));
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 200, 1));
    }),
    TEMPO("tempo", "Pérola do Tempo", 500, (p, w) -> {
        for (LivingEntity e : Fx.near(p, 8)) {
            e.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 100, 3));
            e.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 100, 1));
        }
    });

    public interface Ability { void cast(ServerPlayerEntity p, ServerWorld w); }

    public final String key, label;
    public final int cooldown;
    public final Ability ability;

    Pearl(String key, String label, int cooldown, Ability ability) {
        this.key = key; this.label = label; this.cooldown = cooldown; this.ability = ability;
    }

    public static Pearl byKey(String k) {
        for (Pearl p : values()) if (p.key.equals(k)) return p;
        return null;
    }
}
