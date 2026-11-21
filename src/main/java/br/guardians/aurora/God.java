package br.guardians.aurora;

import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import static net.minecraft.entity.effect.StatusEffects.*;

/** Temporada 2: deuses. Habilidades nos níveis 1, 3, 5, 7 e 10. */
public enum God {
    ZEUS("zeus", "Zeus", "Rei dos deuses e senhor do trovão.", ParticleTypes.ELECTRIC_SPARK,
        Skill.s(1, "Raio Divino", "Um raio cai no inimigo mais próximo.", (p, w, l) -> Fx.bolts(w, p, 0, 1)),
        Skill.s(3, "Tempestade", "Um segundo raio cai no próximo inimigo.", (p, w, l) -> Fx.bolts(w, p, 1, 1)),
        Skill.s(5, "Armadura de Tempestade", "Resistência e velocidade.", (p, w, l) -> {
            Fx.eff(p, RESISTANCE, 160, 1); Fx.eff(p, SPEED, 160, 0); }),
        Skill.s(7, "Chuva de Raios", "Mais 3 inimigos são atingidos por raios.", (p, w, l) -> Fx.bolts(w, p, 2, 3)),
        Skill.s(10, "Ira do Olimpo", "Dano em todos a 12 blocos e força para você.", (p, w, l) -> {
            for (LivingEntity e : Fx.near(p, 12)) { Fx.hurt(w, p, e, 6); Fx.eff(e, GLOWING, 200, 0); }
            Fx.eff(p, STRENGTH, 200, 1); })),

    POSEIDON("poseidon", "Poseidon", "Deus dos mares, dos terremotos e dos cavalos.", ParticleTypes.SPLASH,
        Skill.s(1, "Maré Sagrada", "Empurra inimigos e dá fôlego e velocidade na água.", (p, w, l) -> {
            for (LivingEntity e : Fx.near(p, 6)) Fx.push(p, e, 1.2);
            Fx.eff(p, WATER_BREATHING, 240, 0); Fx.eff(p, DOLPHINS_GRACE, 240, 0); }),
        Skill.s(3, "Cura das Águas", "Cura 6 de vida e dá regeneração.", (p, w, l) -> {
            p.heal(6); Fx.eff(p, REGENERATION, 100, 0); }),
        Skill.s(5, "Tridente", "Fere os 2 inimigos mais próximos (6 de dano).", (p, w, l) -> {
            for (LivingEntity e : Fx.nearest(p, 10, 2)) Fx.hurt(w, p, e, 6); }),
        Skill.s(7, "Tsunami", "Empurra com força e deixa inimigos lentos.", (p, w, l) -> {
            for (LivingEntity e : Fx.near(p, 9)) { Fx.push(p, e, 2.0); Fx.eff(e, SLOWNESS, 100, 2); } }),
        Skill.s(10, "Fúria dos Mares", "Resistência, força e cura grande.", (p, w, l) -> {
            Fx.eff(p, RESISTANCE, 200, 2); Fx.eff(p, STRENGTH, 200, 1); p.heal(20); })),

    HADES("hades", "Hades", "Senhor do submundo e dos mortos.", ParticleTypes.SOUL,
        Skill.s(1, "Toque da Morte", "Aplica Wither nos inimigos a 8 blocos.", (p, w, l) -> {
            for (LivingEntity e : Fx.near(p, 8)) Fx.eff(e, WITHER, 80, 0); }),
        Skill.s(3, "Pele Sombria", "Resistência e imunidade ao fogo.", (p, w, l) -> {
            Fx.eff(p, RESISTANCE, 120, 0); Fx.eff(p, FIRE_RESISTANCE, 200, 0); }),
        Skill.s(5, "Drenar Vida", "Fere inimigos a 6 blocos e você recupera vida.", (p, w, l) -> {
            for (LivingEntity e : Fx.near(p, 6)) { Fx.hurt(w, p, e, 3); p.heal(2); } }),
        Skill.s(7, "Capuz da Invisibilidade", "Invisibilidade e velocidade.", (p, w, l) -> {
            Fx.eff(p, INVISIBILITY, 160, 0); Fx.eff(p, SPEED, 160, 0); }),
        Skill.s(10, "Reino dos Mortos", "Wither, fraqueza e cegueira em todos a 12 blocos.", (p, w, l) -> {
            for (LivingEntity e : Fx.near(p, 12)) {
                Fx.eff(e, WITHER, 100, 1); Fx.eff(e, WEAKNESS, 200, 1); Fx.eff(e, BLINDNESS, 60, 0); } })),

    ATHENA("athena", "Atena", "Deusa da sabedoria e da estratégia de guerra.", ParticleTypes.END_ROD,
        Skill.s(1, "Égide", "Resistência e absorção.", (p, w, l) -> {
            Fx.eff(p, RESISTANCE, 160, 1); Fx.eff(p, ABSORPTION, 300, 1); }),
        Skill.s(3, "Sabedoria", "Pressa e recupera fome.", (p, w, l) -> {
            Fx.eff(p, HASTE, 200, 1); p.getHungerManager().add(6, 1f); }),
        Skill.s(5, "Lança de Atena", "Fere e empurra os 2 inimigos mais próximos.", (p, w, l) -> {
            for (LivingEntity e : Fx.nearest(p, 10, 2)) { Fx.hurt(w, p, e, 6); Fx.push(p, e, 0.8); } }),
        Skill.s(7, "Estratégia", "Velocidade e força.", (p, w, l) -> {
            Fx.eff(p, SPEED, 200, 1); Fx.eff(p, STRENGTH, 200, 0); }),
        Skill.s(10, "Escudo Divino", "Absorção enorme e resistência alta.", (p, w, l) -> {
            Fx.eff(p, ABSORPTION, 400, 4); Fx.eff(p, RESISTANCE, 100, 3); })),

    APOLLO("apollo", "Apolo", "Deus do sol, da música e da cura.", ParticleTypes.FLAME,
        Skill.s(1, "Luz Solar", "Regeneração e revela inimigos a 15 blocos.", (p, w, l) -> {
            Fx.eff(p, REGENERATION, 100, 1);
            for (LivingEntity e : Fx.near(p, 15)) Fx.eff(e, GLOWING, 200, 0); }),
        Skill.s(3, "Raio Solar", "Queima e fere inimigos a 6 blocos.", (p, w, l) -> {
            for (LivingEntity e : Fx.near(p, 6)) { e.setOnFireFor(4); Fx.hurt(w, p, e, 4); } }),
        Skill.s(5, "Música Curativa", "Cura 10 de vida e recupera fome.", (p, w, l) -> {
            p.heal(10); p.getHungerManager().add(4, 1f); }),
        Skill.s(7, "Flechas de Luz", "Fere os 3 inimigos mais próximos (5 de dano).", (p, w, l) -> {
            for (LivingEntity e : Fx.nearest(p, 16, 3)) Fx.hurt(w, p, e, 5); }),
        Skill.s(10, "Alvorecer", "Regeneração forte e cega inimigos a 10 blocos.", (p, w, l) -> {
            Fx.eff(p, REGENERATION, 200, 2); Fx.eff(p, ABSORPTION, 300, 2);
            for (LivingEntity e : Fx.near(p, 10)) { Fx.eff(e, BLINDNESS, 80, 0); Fx.hurt(w, p, e, 6); } })),

    ARES("ares", "Ares", "Deus da guerra e da fúria.", ParticleTypes.CRIT,
        Skill.s(1, "Fúria", "Força aumentada.", (p, w, l) -> Fx.eff(p, STRENGTH, 160, 1)),
        Skill.s(3, "Grito de Guerra", "Enfraquece e empurra inimigos a 8 blocos.", (p, w, l) -> {
            for (LivingEntity e : Fx.near(p, 8)) { Fx.eff(e, WEAKNESS, 120, 0); Fx.push(p, e, 0.8); } }),
        Skill.s(5, "Sede de Sangue", "Avança e fere quem estiver perto.", (p, w, l) -> {
            Fx.dash(p, 1.8); for (LivingEntity e : Fx.near(p, 4)) Fx.hurt(w, p, e, 6); }),
        Skill.s(7, "Armadura de Guerra", "Resistência e absorção.", (p, w, l) -> {
            Fx.eff(p, RESISTANCE, 200, 1); Fx.eff(p, ABSORPTION, 300, 2); }),
        Skill.s(10, "Deus da Guerra", "Força enorme, velocidade e 8 de dano em área.", (p, w, l) -> {
            Fx.eff(p, STRENGTH, 200, 3); Fx.eff(p, SPEED, 200, 1);
            for (LivingEntity e : Fx.near(p, 8)) Fx.hurt(w, p, e, 8); })),

    HERMES("hermes", "Hermes", "Mensageiro dos deuses, mestre da velocidade.", ParticleTypes.CLOUD,
        Skill.s(1, "Pés Alados", "Velocidade e pulo alto.", (p, w, l) -> {
            Fx.eff(p, SPEED, 160, 2); Fx.eff(p, JUMP_BOOST, 160, 1); }),
        Skill.s(3, "Rajada Mensageira", "Avanço veloz para a frente.", (p, w, l) -> Fx.dash(p, 2.4)),
        Skill.s(5, "Queda Suave", "Cai devagar e apaga o fogo.", (p, w, l) -> {
            Fx.eff(p, SLOW_FALLING, 300, 0); p.extinguish(); }),
        Skill.s(7, "Ladrão Divino", "Atrasa e enfraquece inimigos a 6 blocos.", (p, w, l) -> {
            for (LivingEntity e : Fx.near(p, 6)) { Fx.eff(e, SLOWNESS, 100, 2); Fx.eff(e, WEAKNESS, 100, 0); } }),
        Skill.s(10, "Velocidade Divina", "Velocidade máxima, pressa e um avanço.", (p, w, l) -> {
            Fx.eff(p, SPEED, 200, 4); Fx.eff(p, HASTE, 200, 2); Fx.dash(p, 2.8); })),

    ARTEMIS("artemis", "Ártemis", "Deusa da caça, da lua e dos arqueiros.", ParticleTypes.ENCHANT,
        Skill.s(1, "Visão da Caçadora", "Visão noturna e revela inimigos a 15 blocos.", (p, w, l) -> {
            Fx.eff(p, NIGHT_VISION, 400, 0);
            for (LivingEntity e : Fx.near(p, 15)) Fx.eff(e, GLOWING, 200, 0); }),
        Skill.s(3, "Passo Silencioso", "Velocidade e pulo.", (p, w, l) -> {
            Fx.eff(p, SPEED, 160, 1); Fx.eff(p, JUMP_BOOST, 160, 0); }),
        Skill.s(5, "Flechas Espectrais", "Fere os 3 inimigos mais próximos (5 de dano).", (p, w, l) -> {
            for (LivingEntity e : Fx.nearest(p, 20, 3)) Fx.hurt(w, p, e, 5); }),
        Skill.s(7, "Marca da Presa", "Marca, atrasa e enfraquece inimigos a 12 blocos.", (p, w, l) -> {
            for (LivingEntity e : Fx.near(p, 12)) {
                Fx.eff(e, GLOWING, 300, 0); Fx.eff(e, SLOWNESS, 100, 1); Fx.eff(e, WEAKNESS, 100, 0); } }),
        Skill.s(10, "Caçada Perfeita", "7 de dano em todos a 16 blocos, força e velocidade.", (p, w, l) -> {
            for (LivingEntity e : Fx.near(p, 16)) Fx.hurt(w, p, e, 7);
            Fx.eff(p, STRENGTH, 200, 1); Fx.eff(p, SPEED, 200, 2); }));

    public static final int MAX_LEVEL = 10;
    public final String key, label, desc;
    public final ParticleEffect fx;
    public final Skill[] skills;

    God(String key, String label, String desc, ParticleEffect fx, Skill... skills) {
        this.key = key; this.label = label; this.desc = desc; this.fx = fx; this.skills = skills;
    }

    public void cast(ServerPlayerEntity p, ServerWorld w, int level) {
        Skill.castAll(skills, p, w, level, fx);
    }

    public static God byKey(String k) {
        for (God g : values()) if (g.key.equals(k)) return g;
        return null;
    }
}
