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
        Skill.s(1, "Véu", "Invisibilidade total (esconde armadura e itens) e velocidade.", (p, w, l) -> {
            Stealth.start(p, 200); Fx.eff(p, SPEED, 200, 1); }),
        Skill.s(4, "Passo Sombrio", "Avanço rápido para a frente.", (p, w, l) -> Fx.dash(p, 1.8)),
        Skill.s(8, "Eclipse", "Cega e enfraquece inimigos a 8 blocos.", (p, w, l) -> {
            for (LivingEntity e : Fx.near(p, 8)) { Fx.eff(e, BLINDNESS, 100, 0); Fx.eff(e, WEAKNESS, 100, 1); } })),

    TEMPO("tempo", "Pérola do Tempo", 500, "Pérola temporal. Distorce o tempo ao seu redor.", ParticleTypes.ENCHANT,
        Skill.s(1, "Lentidão", "Deixa inimigos lentos e fracos.", (p, w, l) -> {
            for (LivingEntity e : Fx.near(p, 8)) { Fx.eff(e, SLOWNESS, 100, 3); Fx.eff(e, WEAKNESS, 100, 1); } }),
        Skill.s(4, "Aceleração", "Você fica muito mais rápido.", (p, w, l) -> {
            Fx.eff(p, SPEED, 200, 2); Fx.eff(p, HASTE, 200, 1); }),
        Skill.s(8, "Tempo Parado", "Quase congela inimigos a 10 blocos.", (p, w, l) -> {
            for (LivingEntity e : Fx.near(p, 10)) { Fx.eff(e, SLOWNESS, 60, 6); Fx.eff(e, WEAKNESS, 100, 2); } })),

    GELO("gelo", "Pérola do Gelo", 400, "Pérola do inverno. Congela e protege com uma casca de gelo.", ParticleTypes.SNOWFLAKE,
        Skill.s(1, "Rajada Gélida", "Fere e deixa lentos os inimigos a 7 blocos.", (p, w, l) -> {
            for (LivingEntity e : Fx.near(p, 7)) { Fx.hurt(w, p, e, 3); Fx.eff(e, SLOWNESS, 100, 2); } }),
        Skill.s(4, "Armadura de Gelo", "Absorção e resistência, e apaga o fogo.", (p, w, l) -> {
            Fx.eff(p, ABSORPTION, 300, 1); Fx.eff(p, RESISTANCE, 200, 1); p.extinguish(); }),
        Skill.s(8, "Nevasca", "Fere, enfraquece e quase congela inimigos a 10 blocos.", (p, w, l) -> {
            for (LivingEntity e : Fx.near(p, 10)) {
                Fx.hurt(w, p, e, 5); Fx.eff(e, SLOWNESS, 100, 4); Fx.eff(e, WEAKNESS, 100, 0); } })),

    LUZ("luz", "Pérola da Luz", 450, "Pérola da luz. Cega os inimigos e cura os aliados.", ParticleTypes.END_ROD,
        Skill.s(1, "Clarão", "Cega e revela inimigos a 10 blocos.", (p, w, l) -> {
            for (LivingEntity e : Fx.near(p, 10)) { Fx.eff(e, BLINDNESS, 60, 0); Fx.eff(e, GLOWING, 200, 0); } }),
        Skill.s(4, "Bênção", "Cura 6 de vida, regeneração e visão noturna.", (p, w, l) -> {
            p.heal(6); Fx.eff(p, REGENERATION, 100, 1); Fx.eff(p, NIGHT_VISION, 300, 0); }),
        Skill.s(8, "Raio de Luz", "Fere inimigos a 8 blocos (8 de dano) e cura você.", (p, w, l) -> {
            for (LivingEntity e : Fx.near(p, 8)) { Fx.hurt(w, p, e, 8); p.heal(1); } })),

    ALMA("alma", "Pérola da Alma", 500, "Pérola das almas. Rouba a vida de quem está por perto.", ParticleTypes.SOUL,
        Skill.s(1, "Colheita", "Fere inimigos a 5 blocos e rouba vida.", (p, w, l) -> {
            for (LivingEntity e : Fx.near(p, 5)) { Fx.hurt(w, p, e, 3); p.heal(2); } }),
        Skill.s(4, "Sussurros", "Enfraquece e atrasa inimigos a 8 blocos.", (p, w, l) -> {
            for (LivingEntity e : Fx.near(p, 8)) { Fx.eff(e, WEAKNESS, 120, 0); Fx.eff(e, SLOWNESS, 60, 1); } }),
        Skill.s(8, "Ceifador", "Wither e 5 de dano a 8 blocos; você recupera vida.", (p, w, l) -> {
            for (LivingEntity e : Fx.near(p, 8)) { Fx.eff(e, WITHER, 80, 1); Fx.hurt(w, p, e, 5); p.heal(3); } })),

    FADA("fada", "Pérola da Fada", 350, "Pérola da magia das fadas. Cura, faz voar e muda o seu tamanho.", ParticleTypes.GLOW,
        Skill.s(1, "Poeira de Fada", "Cura 6 de vida, queda suave e um pouco de velocidade.", (p, w, l) -> {
            p.heal(6); Fx.eff(p, SLOW_FALLING, 200, 0); Fx.eff(p, SPEED, 120, 0); }),
        Skill.s(3, "Pequena Fada", "Você fica minúsculo por 20s: muito rápido e pula alto.", (p, w, l) -> {
            Forms.start(p, "small", 400);
            Fx.eff(p, SPEED, 400, 2); Fx.eff(p, JUMP_BOOST, 400, 1); Fx.eff(p, SLOW_FALLING, 400, 0); }),
        Skill.s(5, "Voo Encantado", "Você sobe no ar por 2 segundos e depois cai devagar.", (p, w, l) -> {
            Fx.eff(p, LEVITATION, 40, 1); Fx.eff(p, SLOW_FALLING, 300, 0); }),
        Skill.s(7, "Gigante", "Você vira um gigante por 15s: força e resistência.", (p, w, l) -> {
            Forms.start(p, "giant", 300);
            Fx.eff(p, STRENGTH, 300, 1); Fx.eff(p, RESISTANCE, 300, 1); }),
        Skill.s(10, "Encanto", "Revela e enfraquece inimigos a 8 blocos; você regenera.", (p, w, l) -> {
            for (LivingEntity e : Fx.near(p, 8)) {
                Fx.eff(e, GLOWING, 200, 0); Fx.eff(e, SLOWNESS, 80, 1); Fx.eff(e, WEAKNESS, 100, 0); }
            Fx.eff(p, REGENERATION, 100, 1); })),

    FENIX("fenix", "Pérola da Fênix", 600, "Pérola do renascimento. Queima inimigos e cura você com chamas.", ParticleTypes.FLAME,
        Skill.s(1, "Chamas Renascentes", "Cura 6 de vida, regeneração e imunidade ao fogo.", (p, w, l) -> {
            p.heal(6); Fx.eff(p, REGENERATION, 100, 1); Fx.eff(p, FIRE_RESISTANCE, 200, 0); }),
        Skill.s(4, "Asas de Fogo", "Avança e queima quem estiver perto.", (p, w, l) -> {
            Fx.dash(p, 2.0);
            for (LivingEntity e : Fx.near(p, 4)) { e.setOnFireFor(4); Fx.hurt(w, p, e, 3); } }),
        Skill.s(8, "Renascimento", "Cura 20, absorção e explosão de fogo a 7 blocos.", (p, w, l) -> {
            p.heal(20); Fx.eff(p, ABSORPTION, 400, 2);
            for (LivingEntity e : Fx.near(p, 7)) { e.setOnFireFor(6); Fx.hurt(w, p, e, 5); } })),

    MEDUSA("medusa", "Pérola da Medusa", 550, "Pérola do olhar de pedra. Quem você petrifica te dá força e velocidade.", ParticleTypes.SCULK_SOUL,
        Skill.s(1, "Olhar Petrificante", "Petrifica os 3 inimigos mais próximos; você ganha força e velocidade.", (p, w, l) -> {
            int n = Fx.petrifyAll(w, p, Fx.nearest(p, 12, 3), 60);
            Fx.stoneBoost(p, n, 160); }),
        Skill.s(4, "Veneno de Serpente", "Envenena e fere inimigos a 8 blocos.", (p, w, l) -> {
            for (LivingEntity e : Fx.near(p, 8)) { Fx.eff(e, POISON, 120, 1); Fx.hurt(w, p, e, 2); } }),
        Skill.s(8, "Pedra Total", "Petrifica todos a 10 blocos; cada um aumenta sua força e velocidade.", (p, w, l) -> {
            int n = Fx.petrifyAll(w, p, Fx.near(p, 10), 100);
            Fx.stoneBoost(p, n, 200); Fx.eff(p, RESISTANCE, 100, 1); })),

    DRAGAO("dragao", "Pérola do Dragão", 600, "Pérola do dragão ancestral. Sopra fogo e protege com escamas.", ParticleTypes.DRAGON_BREATH,
        Skill.s(1, "Sopro de Fogo", "Queima e fere os 3 inimigos mais próximos.", (p, w, l) -> {
            for (LivingEntity e : Fx.nearest(p, 8, 3)) { e.setOnFireFor(5); Fx.hurt(w, p, e, 5); } }),
        Skill.s(4, "Escamas", "Resistência e força.", (p, w, l) -> {
            Fx.eff(p, RESISTANCE, 200, 1); Fx.eff(p, STRENGTH, 200, 0); }),
        Skill.s(8, "Fúria Dracônica", "Fere (8), queima e empurra todos a 9 blocos; força alta.", (p, w, l) -> {
            for (LivingEntity e : Fx.near(p, 9)) { e.setOnFireFor(6); Fx.hurt(w, p, e, 8); Fx.push(p, e, 1.5); }
            Fx.eff(p, STRENGTH, 200, 1); })),

    KRAKEN("kraken", "Pérola do Kraken", 500, "Pérola das profundezas. Puxa e esmaga com tentáculos.", ParticleTypes.SQUID_INK,
        Skill.s(1, "Tentáculos", "Puxa, fere e atrasa inimigos a 6 blocos.", (p, w, l) -> {
            for (LivingEntity e : Fx.near(p, 6)) { Fx.push(p, e, -1.0); Fx.hurt(w, p, e, 2); Fx.eff(e, SLOWNESS, 100, 3); } }),
        Skill.s(4, "Maré Negra", "Cega e enfraquece inimigos; você respira e nada bem na água.", (p, w, l) -> {
            for (LivingEntity e : Fx.near(p, 8)) { Fx.eff(e, BLINDNESS, 60, 0); Fx.eff(e, WEAKNESS, 80, 0); }
            Fx.eff(p, WATER_BREATHING, 400, 0); Fx.eff(p, DOLPHINS_GRACE, 300, 0); }),
        Skill.s(8, "Esmagar", "Puxa todos a 8 blocos e esmaga os 3 mais próximos (9 de dano).", (p, w, l) -> {
            for (LivingEntity e : Fx.near(p, 8)) Fx.push(p, e, -1.5);
            for (LivingEntity e : Fx.nearest(p, 8, 3)) Fx.hurt(w, p, e, 9); })),

    VAMPIRO("vampiro", "Pérola do Vampiro", 500, "Pérola noturna. Drena sangue e transforma você em morcego.", ParticleTypes.CRIMSON_SPORE,
        Skill.s(1, "Mordida", "Fere o inimigo mais próximo (5) e cura você em 5.", (p, w, l) -> {
            for (LivingEntity e : Fx.nearest(p, 4, 1)) { Fx.hurt(w, p, e, 5); p.heal(5); } }),
        Skill.s(4, "Névoa Carmesim", "Invisibilidade total (esconde armadura e itens), velocidade e visão noturna.", (p, w, l) -> {
            Stealth.start(p, 100); Fx.eff(p, SPEED, 160, 1); Fx.eff(p, NIGHT_VISION, 300, 0); }),
        Skill.s(6, "Forma de Morcego", "Vira um morcego por 10s: você voa, fica pequeno e invisível.", (p, w, l) -> {
            Morph.bat(p, 200); Fx.eff(p, NIGHT_VISION, 200, 0); }),
        Skill.s(6, "Forma de Morcego", "Vira um morcego por 15s: pequeno, voando, veloz e com visão noturna.", (p, w, l) -> {
            Forms.start(p, "bat", 300);
            Fx.eff(p, SPEED, 300, 1); Fx.eff(p, NIGHT_VISION, 300, 0); }),
        Skill.s(8, "Banquete de Sangue", "Fere todos a 7 blocos e cura você por inimigo atingido.", (p, w, l) -> {
            for (LivingEntity e : Fx.near(p, 7)) { Fx.hurt(w, p, e, 4); p.heal(4); }
            Fx.eff(p, STRENGTH, 200, 0); }));

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
