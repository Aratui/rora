package br.guardians.aurora;

import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

/** Uma habilidade que se desbloqueia ao atingir um nível. */
public record Skill(int level, String name, String desc, Fn fn) {
    public interface Fn { void run(ServerPlayerEntity p, ServerWorld w, int l); }

    public static Skill s(int level, String name, String desc, Fn fn) {
        return new Skill(level, name, desc, fn);
    }

    /** Executa todas as habilidades já desbloqueadas pelo nível. */
    public static void castAll(Skill[] sk, ServerPlayerEntity p, ServerWorld w, int lvl, ParticleEffect fx) {
        Fx.curFx = fx;
        try {
            Fx.cast(w, p, fx, lvl);
            for (Skill s : sk) if (lvl >= s.level()) s.fn().run(p, w, lvl);
        } finally {
            Fx.curFx = null;
        }
    }
}
