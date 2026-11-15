package br.guardians.aurora;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class Fx {
    public static List<LivingEntity> near(ServerPlayerEntity p, double r) {
        return p.getWorld().getEntitiesByClass(LivingEntity.class,
                p.getBoundingBox().expand(r), e -> e != p && e.isAlive());
    }

    public static void bolt(ServerWorld w, Vec3d pos, ServerPlayerEntity p) {
        LightningEntity b = EntityType.LIGHTNING_BOLT.create(w);
        if (b == null) return;
        b.refreshPositionAfterTeleport(pos);
        b.setChanneler(p);
        w.spawnEntity(b);
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
