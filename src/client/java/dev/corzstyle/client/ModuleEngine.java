package dev.corzstyle.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class ModuleEngine {
    private static long lastAttackNanos = 0L;
    private static int xpClock = 0;
    private static Double previousGamma;

    public static void tick(Minecraft mc) {
        if (mc.player == null || mc.level == null || mc.gameMode == null) {
            restoreGamma(mc);
            return;
        }
        if (on("auto_sprint") && mc.player.input != null && !mc.player.isSprinting()
                && !mc.player.isCrouching() && mc.options.keyUp.isDown()) {
            // The server still decides whether sprinting is permitted.
            mc.player.setSprinting(true);
        }
        if (on("fullbright")) {
            if (previousGamma == null) previousGamma = mc.options.gamma().get();
            mc.options.gamma().set(1.0);
        } else restoreGamma(mc);

        if (on("auto_xp") && (++xpClock % 3 == 0) && mc.player.getMainHandItem().is(Items.EXPERIENCE_BOTTLE)) {
            mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
        }
        if (mc.screen != null) return;
        if (on("aim_assist") && mc.options.keyAttack.isDown()) aimAssist(mc);
        if (on("trigger_bot") || (on("auto_clicker") && mc.options.keyAttack.isDown())) {
            if (mc.hitResult instanceof EntityHitResult target && target.getEntity() instanceof LivingEntity entity
                    && entity.isAlive() && entity != mc.player) {
                long now = System.nanoTime();
                if (now - lastAttackNanos > 120_000_000L) {
                    mc.gameMode.attack(mc.player, entity);
                    mc.player.swing(InteractionHand.MAIN_HAND);
                    lastAttackNanos = now;
                }
            }
        }
    }

    public static void keyPearl(Minecraft mc) {
        if (!on("key_pearl") || mc.player == null || mc.gameMode == null || mc.screen != null) return;
        int originalSlot = mc.player.getInventory().getSelectedSlot();
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getItem(i).is(Items.ENDER_PEARL)) {
                mc.player.getInventory().setSelectedSlot(i);
                mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
                mc.player.swing(InteractionHand.MAIN_HAND);
                mc.player.getInventory().setSelectedSlot(originalSlot);
                return;
            }
        }
    }

    private static void aimAssist(Minecraft mc) {
        LivingEntity nearest = null;
        double best = 4.5 * 4.5;
        for (LivingEntity entity : mc.level.getEntitiesOfClass(LivingEntity.class, mc.player.getBoundingBox().inflate(4.5),
                e -> e != mc.player && e.isAlive() && !e.isInvisible() && mc.player.hasLineOfSight(e))) {
            double dist = mc.player.distanceToSqr(entity);
            if (dist < best) { best = dist; nearest = entity; }
        }
        if (nearest == null) return;
        Vec3 direction = nearest.getEyePosition().subtract(mc.player.getEyePosition());
        double horizontal = Math.sqrt(direction.x * direction.x + direction.z * direction.z);
        float desiredYaw = (float) Math.toDegrees(Math.atan2(-direction.x, direction.z));
        float desiredPitch = (float) -Math.toDegrees(Math.atan2(direction.y, horizontal));
        mc.player.setYRot(mc.player.getYRot() + Mth.wrapDegrees(desiredYaw - mc.player.getYRot()) * 0.15f);
        mc.player.setXRot(Mth.clamp(mc.player.getXRot() + (desiredPitch - mc.player.getXRot()) * 0.15f, -90f, 90f));
    }

    private static void restoreGamma(Minecraft mc) {
        if (previousGamma != null) {
            mc.options.gamma().set(previousGamma);
            previousGamma = null;
        }
    }

    private static boolean on(String id) { return Features.get(id).enabled; }

    private ModuleEngine() {}
}
