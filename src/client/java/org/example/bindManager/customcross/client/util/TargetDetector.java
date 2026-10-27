package org.example.bindManager.customcross.client.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

public final class TargetDetector {

    public static TargetType getTargetType(MinecraftClient client, float tickDelta) {
        if (client == null || client.player == null || client.world == null) return TargetType.NONE;

        HitResult target = client.crosshairTarget;
        if (target == null) return TargetType.NONE;

        if (target.getType() == HitResult.Type.ENTITY && target instanceof EntityHitResult ehr) {
            Entity entity = ehr.getEntity();
            if (entity instanceof PlayerEntity) return TargetType.PLAYER;
            if (entity instanceof LivingEntity) return TargetType.MOB;
        } else if (target.getType() == HitResult.Type.BLOCK && target instanceof BlockHitResult bhr) {
            if (bhr.getType() == HitResult.Type.BLOCK) {
                return TargetType.BLOCK;
            }
        }
        return TargetType.NONE;
    }

    public enum TargetType { NONE, PLAYER, MOB, BLOCK }
}
