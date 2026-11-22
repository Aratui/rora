package br.guardians.aurora;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Fx {
    /** Partícula do poder em uso: faz near()/nearest() desenharem anéis e raios automaticamente. */
    public static ParticleEffect curFx;

    // ---------- alvos ----------
    static List<LivingEntity> raw(ServerPlayerEntity p, double r) {
        return p.getWorld().getEntitiesByClass(LivingEntity.class,
                p.getBoundingBox().expand(r), e -> e != p && e.isAlive());
    }

    /** Inimigos num raio; desenha um anel de partículas no chão. */
    public static List<LivingEntity> near(ServerPlayerEntity p, double r) {
        if (curFx != null && p.getWorld() instanceof ServerWorld w) ring(w, p.getX(), p.getY() + 0.15, p.getZ(), r, curFx);
        return raw(p, r);
    }

    /** Os n inimigos mais próximos; desenha um raio de partículas até cada um. */
    public static List<LivingEntity> nearest(ServerPlayerEntity p, double r, int n) {
        List<LivingEntity> l = new ArrayList<>(raw(p, r));
        l.sort(Comparator.comparingDouble(e -> e.squaredDistanceTo(p)));
        List<LivingEntity> out = l.size() > n ? l.subList(0, n) : l;
        if (curFx != null && p.getWorld() instanceof ServerWorld w) for (LivingEntity e : out) beam(w, p, e, curFx);
        return out;
    }

    // ---------- efeitos de jogo ----------
    public static void eff(LivingEntity e, RegistryEntry<StatusEffect> t, int dur, int amp) {
        e.addStatusEffect(new StatusEffectInstance(t, dur, amp));
    }

    public static void hurt(ServerWorld w, ServerPlayerEntity p, LivingEntity e, float amount) {
        e.damage(w.getDamageSources().playerAttack(p), amount);
        w.spawnParticles(ParticleTypes.CRIT, e.getX(), e.getBodyY(0.5), e.getZ(), 8, 0.3, 0.3, 0.3, 0.2);
    }

    public static void push(ServerPlayerEntity p, LivingEntity e, double power) {
        Vec3d d = e.getPos().subtract(p.getPos()).normalize().multiply(power);
        e.addVelocity(d.x, 0.4, d.z);
        e.velocityModified = true;
    }

    public static void bolt(ServerWorld w, Vec3d pos, ServerPlayerEntity p) {
        LightningEntity b = EntityType.LIGHTNING_BOLT.create(w);
        if (b == null) return;
        b.refreshPositionAfterTeleport(pos);
        b.setChanneler(p);
        w.spawnEntity(b);
    }

    /** Raios nos inimigos de posição [from, from+count) por proximidade. */
    public static void bolts(ServerWorld w, ServerPlayerEntity p, int from, int count) {
        List<LivingEntity> t = nearest(p, 14, from + count);
        for (int i = from; i < t.size(); i++) bolt(w, t.get(i).getPos(), p);
    }

    public static Vec3d target(ServerPlayerEntity p, double dist) {
        HitResult h = p.raycast(dist, 1f, false);
        return h.getPos();
    }

    public static void dash(ServerPlayerEntity p, double power) {
        Vec3d v = p.getRotationVec(1f).multiply(power).add(0, 0.3, 0);
        p.setVelocity(v);
        p.velocityModified = true;
        p.fallDistance = 0;
        if (curFx != null && p.getWorld() instanceof ServerWorld w)
            w.spawnParticles(curFx, p.getX(), p.getY() + 0.8, p.getZ(), 30, 0.3, 0.4, 0.3, 0.15);
    }

    // ---------- visual ----------
    public static void burst(ServerWorld w, ServerPlayerEntity p, ParticleEffect fx, int n) {
        w.spawnParticles(fx, p.getX(), p.getY() + 1.0, p.getZ(), n, 0.5, 0.6, 0.5, 0.05);
    }

    public static void ring(ServerWorld w, double x, double y, double z, double r, ParticleEffect fx) {
        int n = (int) Math.min(48, Math.max(16, r * 8));
        for (int i = 0; i < n; i++) {
            double a = 2 * Math.PI * i / n;
            w.spawnParticles(fx, x + Math.cos(a) * r, y, z + Math.sin(a) * r, 1, 0, 0.05, 0, 0.01);
        }
    }

    public static void beam(ServerWorld w, ServerPlayerEntity p, LivingEntity e, ParticleEffect fx) {
        Vec3d a = p.getPos().add(0, 1.2, 0), b = e.getPos().add(0, e.getHeight() * 0.5, 0);
        for (int i = 0; i <= 12; i++) {
            Vec3d q = a.lerp(b, i / 12.0);
            w.spawnParticles(fx, q.x, q.y, q.z, 1, 0.02, 0.02, 0.02, 0.0);
        }
    }

    /** Espiral subindo ao redor do jogador + anel no chão. */
    public static void spiral(ServerWorld w, ServerPlayerEntity p, ParticleEffect fx) {
        for (int i = 0; i < 36; i++) {
            double t = i / 36.0, a = t * Math.PI * 4;
            w.spawnParticles(fx, p.getX() + Math.cos(a) * 0.9, p.getY() + t * 2.2, p.getZ() + Math.sin(a) * 0.9,
                    1, 0, 0, 0, 0);
        }
        ring(w, p.getX(), p.getY() + 0.1, p.getZ(), 1.6, fx);
    }

    static void sound(ServerWorld w, ServerPlayerEntity p, SoundEvent s, float vol, float pitch) {
        w.playSound(null, p.getX(), p.getY(), p.getZ(), s, SoundCategory.PLAYERS, vol, pitch);
    }

    /** Efeito de lançamento: espiral, som do elemento e, no nível 10, um brilho dourado extra. */
    public static void cast(ServerWorld w, ServerPlayerEntity p, ParticleEffect fx, int lvl) {
        spiral(w, p, fx);
        if (fx == ParticleTypes.FLAME || fx == ParticleTypes.LAVA) sound(w, p, SoundEvents.ENTITY_BLAZE_SHOOT, 1f, 0.9f);
        else if (fx == ParticleTypes.ELECTRIC_SPARK) sound(w, p, SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, 0.4f, 1.3f);
        else if (fx == ParticleTypes.SPLASH) sound(w, p, SoundEvents.ENTITY_PLAYER_SPLASH, 1f, 0.9f);
        else if (fx == ParticleTypes.CLOUD) sound(w, p, SoundEvents.ENTITY_ENDER_DRAGON_FLAP, 0.8f, 1.2f);
        else if (fx == ParticleTypes.SOUL) sound(w, p, SoundEvents.ENTITY_WITHER_SHOOT, 0.5f, 1.4f);
        else if (fx == ParticleTypes.SNOWFLAKE) sound(w, p, SoundEvents.BLOCK_GLASS_BREAK, 0.8f, 1.5f);
        else if (fx == ParticleTypes.END_ROD || fx == ParticleTypes.ENCHANT || fx == ParticleTypes.TOTEM_OF_UNDYING)
            sound(w, p, SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, 1f, 0.8f);
        else if (fx == ParticleTypes.CRIT) sound(w, p, SoundEvents.ENTITY_EVOKER_CAST_SPELL, 0.8f, 1f);
        else sound(w, p, SoundEvents.ENTITY_ILLUSIONER_CAST_SPELL, 0.8f, 1f);
        if (lvl >= God.MAX_LEVEL) burst(w, p, ParticleTypes.TOTEM_OF_UNDYING, 30);
    }
}
