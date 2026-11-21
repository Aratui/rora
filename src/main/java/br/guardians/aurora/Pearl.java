package br.guardians.aurora;

import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import static net.minecraft.entity.effect.StatusEffects.*;

/** Temporada 1: pérolas. Habilidades desbloqueiam conforme o nível do jogador. */
public enum Pearl {
    IGNIS("ignis", "Pérola de Ignis", 300, "Pérola do fogo. Queima e fere quem estiver perto de você.", ParticleTypes.FLAME,
        Skill.s(1, "Labareda", "Incendeia e causa 4 de dano a quem está a 6 blocos.", (p, w, l) -> {
            for (LivingEntity e : Fx.near(p, 6)) { e.setOnFireFor(5); Fx.hurt(w, p, e, 4); } }),
        Skill.s(4, "Explosão Ígnea", "Empurra os inimigos e causa +3 de dano.", (p, w, l) -> {
            for (LivingEntity e : Fx.near(p, 6)) { Fx.push(p, e, 1.0); Fx.hurt(w, p, e, 3); } }),
        Skill.s(8, "Pele de Magma", "Você ganha resistência ao fogo e força.", (p, w, l) -> {
            Fx.eff(p, FIRE_RESISTANCE, 300, 0); Fx.eff(p, STRENGTH, 200, 0); })),

    ZEFIRO("zefiro", "Pérola de Zéfiro", 120, "Pérola do vento. Impulsiona você pelo ar.", ParticleTypes.CLOUD,
        Skill.s(1, "Rajada", "Lança você para a frente.", (p, w, l) -> Fx.dash(p, 2.2)),
        Skill.s(4, "Passos de Vento", "Velocidade, pulo alto e queda suave.", (p, w, l) -> {
            Fx.eff(p, SPEED, 200, 1); Fx.eff(p, JUMP_BOOST, 200, 1); Fx.eff(p, SLOW_FALLING, 200, 0); }),
        Skill.s(8, "Furacão", "Joga inimigos longe e os faz levitar.", (p, w, l) -> {
            for (LivingEntity e : Fx.near(p, 6)) { Fx.push(p, e, 1.8); Fx.eff(e, LEVITATION, 40, 0); } })),

    MARE("mare", "Pérola da Maré", 400, "Pérola da água. Cura e protege quem a usa.", ParticleTypes.SPLASH,
        Skill.s(1, "Cura da Maré", "Cura 8 de vida e dá respiração aquática.", (p, w, l) -> {
            p.heal(8); Fx.eff(p, WATER_BREATHING, 600, 0); }),
        Skill.s(4, "Correnteza", "Regeneração, apaga o fogo e dá nado de golfinho.", (p, w, l) -> {
            Fx.eff(p, REGENERATION, 100, 1); p.extinguish(); Fx.eff(p, DOLPHINS_GRACE, 300, 0); }),
        Skill.s(8, "Onda Gigante", "Empurra e deixa os inimigos lentos.", (p, w, l) -> {
            for (LivingEntity e : Fx.near(p, 7)) { Fx.push(p, e, 1.5); Fx.eff(e, SLOWNESS, 100, 1); } })),

    TERRA("terra", "Pérola de Terra", 500, "Pérola da rocha. Torna você uma muralha.", ParticleTypes.CRIT,
        Skill.s(1, "Pele de Pedra", "Resistência e absorção por alguns segundos.", (p, w, l) -> {
            Fx.eff(p, RESISTANCE, 200, 1); Fx.eff(p, ABSORPTION, 400, 1); }),
        Skill.s(4, "Tremor", "Fere e atrasa inimigos a 5 blocos.", (p, w, l) -> {
            for (LivingEntity e : Fx.near(p, 5)) { Fx.hurt(w, p, e, 3); Fx.eff(e, SLOWNESS, 80, 2); } }),
        Skill.s(8, "Fortaleza", "Resistência e absorção muito maiores.", (p, w, l) -> {
            Fx.eff(p, RESISTANCE, 300, 2); Fx.eff(p, ABSORPTION, 400, 3); })),

    TROVAO("trovao", "Pérola do Trovão", 400, "Pérola da tempestade. Chama raios onde você mira.", ParticleTypes.ELECTRIC_SPARK,
        Skill.s(1, "Raio", "Cai um raio onde você está olhando (até 30 blocos).", (p, w, l) ->
            Fx.bolt(w, Fx.target(p, 30), p)),
        Skill.s(4, "Tempestade", "Raios caem nos 2 inimigos mais próximos.", (p, w, l) -> Fx.bolts(w, p, 0, 2)),
        Skill.s(8, "Fúria do Trovão", "Velocidade, pressa e resistência.", (p, w, l) -> {
            Fx.eff(p, SPEED, 200, 1); Fx.eff(p, HASTE, 200, 1); Fx.eff(p, RESISTANCE, 200, 0); })),

    SOMBRA("sombra", "Pérola da Sombra", 600, "Pérola da escuridão. Esconde e acelera você.", ParticleTypes.SMOKE,
        Skill.s(1, "Véu", "Invisibilidade e velocidade.", (p, w, l) -> {
            Fx.eff(p, INVISIBILITY, 200, 0); Fx.eff(p, SPEED, 200, 1); }),
        Skill.s(4, "Passo Sombrio", "Avanço rápido para a frente.", (p, w, l) -> Fx.dash(p, 1.8)),
        Skill.s(8, "Eclipse", "Cega e enfraquece inimigos a 8 blocos.", (p, w, l) -> {
            for (LivingEntity e : Fx.near(p, 8)) { Fx.eff(e, BLINDNESS, 100, 0); Fx.eff(e, WEAKNESS, 100, 1); } })),

    TEMPO("tempo", "Pérola do Tempo", 500, "Pérola temporal. Distorce o tempo ao seu redor.", ParticleTypes.ENCHANT,
        Skill.s(1, "Lentidão", "Deixa inimigos lentos e fracos.", (p, w, l) -> {
            for (LivingEntity e : Fx.near(p, 8)) { Fx.eff(e, SLOWNESS, 100, 3); Fx.eff(e, WEAKNESS, 100, 1); } }),
        Skill.s(4, "Aceleração", "Você fica muito mais rápido.", (p, w, l) -> {
            Fx.eff(p, SPEED, 200, 2); Fx.eff(p, HASTE, 200, 1); }),
        Skill.s(8, "Tempo Parado", "Quase congela inimigos a 10 blocos.", (p, w, l) -> {
            for (LivingEntity e : Fx.near(p, 10)) { Fx.eff(e, SLOWNESS, 60, 6); Fx.eff(e, WEAKNESS, 100, 2); } }));

    public final String key, label, desc;
    public final int cooldown;
    public final ParticleEffect fx;
    public final Skill[] skills;

    Pearl(String key, String label, int cooldown, String desc, ParticleEffect fx, Skill... skills) {
        this.key = key; this.label = label; this.cooldown = cooldown;
        this.desc = desc; this.fx = fx; this.skills = skills;
    }

    public void cast(ServerPlayerEntity p, ServerWorld w, int level) {
        Skill.castAll(skills, p, w, level, fx);
    }

    public static Pearl byKey(String k) {
        for (Pearl p : values()) if (p.key.equals(k)) return p;
        return null;
    }
}
