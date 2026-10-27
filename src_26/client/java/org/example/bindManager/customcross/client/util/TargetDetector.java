package org.example.bindManager.customcross.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public final class TargetDetector {

    public static TargetType getTargetType(Minecraft client, float tickDelta) {
        if (client == null || client.player == null || client.level == null) return TargetType.NONE;

        HitResult target = client.hitResult;
        if (target == null) return TargetType.NONE;

        if (target.getType() == HitResult.Type.ENTITY && target instanceof EntityHitResult ehr) {
            Entity entity = ehr.getEntity();
            if (entity instanceof Player) return TargetType.PLAYER;
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
