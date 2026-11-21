package br.guardians.aurora;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Fx {
    public static List<LivingEntity> near(ServerPlayerEntity p, double r) {
        return p.getWorld().getEntitiesByClass(LivingEntity.class,
                p.getBoundingBox().expand(r), e -> e != p && e.isAlive());
    }

    /** Os n inimigos mais próximos dentro do raio. */
    public static List<LivingEntity> nearest(ServerPlayerEntity p, double r, int n) {
        List<LivingEntity> l = new ArrayList<>(near(p, r));
        l.sort(Comparator.comparingDouble(e -> e.squaredDistanceTo(p)));
        return l.size() > n ? l.subList(0, n) : l;
    }

    public static void eff(LivingEntity e, RegistryEntry<StatusEffect> t, int dur, int amp) {
        e.addStatusEffect(new StatusEffectInstance(t, dur, amp));
    }

    public static void hurt(ServerWorld w, ServerPlayerEntity p, LivingEntity e, float amount) {
        e.damage(w.getDamageSources().playerAttack(p), amount);
    }

    public static void push(ServerPlayerEntity p, LivingEntity e, double power) {
        Vec3d d = e.getPos().subtract(p.getPos()).normalize().multiply(power);
        e.addVelocity(d.x, 0.4, d.z);
        e.velocityModified = true;
    }

    public static void burst(ServerWorld w, ServerPlayerEntity p, ParticleEffect fx, int n) {
        w.spawnParticles(fx, p.getX(), p.getY() + 1.0, p.getZ(), n, 0.5, 0.6, 0.5, 0.05);
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
    }
}
