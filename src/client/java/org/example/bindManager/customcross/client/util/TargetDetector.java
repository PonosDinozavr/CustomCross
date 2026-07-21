package org.example.bindManager.customcross.client.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import java.util.Optional;

public final class TargetDetector {
    private static final double FALLBACK_RANGE = 3.5;

    public static TargetType getTargetType(MinecraftClient client, float tickDelta) {
        if (client.player == null || client.world == null) return TargetType.NONE;
        double reach = client.player.getEntityInteractionRange();
        if (reach <= 0) reach = FALLBACK_RANGE;
        Entity camera = client.getCameraEntity() != null ? client.getCameraEntity() : client.player;
        Vec3d cameraPos = camera.getCameraPosVec(tickDelta);
        Vec3d rotationVec = camera.getRotationVec(tickDelta);
        Vec3d endPos = cameraPos.add(rotationVec.multiply(reach));
        HitResult blockHit = client.world.raycast(new RaycastContext(
                cameraPos, endPos, RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE, camera));
        double blockDist = blockHit.getType() == HitResult.Type.BLOCK
                ? cameraPos.distanceTo(blockHit.getPos()) : reach;
        Vec3d checkEnd = cameraPos.add(rotationVec.multiply(blockDist));
        Box searchBox = camera.getBoundingBox()
                .stretch(rotationVec.multiply(blockDist)).expand(1.0);
        Entity closest = null;
        double closestDist = blockDist;
        for (Entity entity : client.world.getEntitiesByClass(LivingEntity.class, searchBox,
                e -> e != camera && e.isAlive() && !e.isSpectator())) {
            Box entityBox = entity.getBoundingBox().expand(entity.getTargetingMargin());
            Optional<Vec3d> hit = entityBox.raycast(cameraPos, checkEnd);
            if (hit.isPresent()) {
                double dist = cameraPos.distanceTo(hit.get());
                if (dist < closestDist) { closestDist = dist; closest = entity; }
            }
        }
        if (closest == null) return TargetType.NONE;
        if (closest instanceof PlayerEntity) return TargetType.PLAYER;
        if (closest instanceof LivingEntity) return TargetType.MOB;
        return TargetType.OTHER;
    }

    public enum TargetType { NONE, PLAYER, MOB, OTHER }
}
