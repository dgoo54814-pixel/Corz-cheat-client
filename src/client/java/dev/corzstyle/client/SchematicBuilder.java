package dev.corzstyle.client;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/** Places at most one reachable, ordinary block every five ticks, with real server interactions. */
public final class SchematicBuilder {
    private static final Path DIRECTORY = FabricLoader.getInstance().getConfigDir().resolve("corzstyle/schematics");
    private static SpongeSchem.Schematic schematic;
    private static BlockPos origin;
    private static boolean building;
    private static int clock;
    private static int placed;
    private static String filename = "";

    public static String load(String name) {
        // Explicitly only permit a basename within the user-controlled schematics directory.
        if (!name.matches("[a-zA-Z0-9 _()\\-]{1,100}\\.schem")) return "Invalid name (use a .schem basename)";
        Path file = DIRECTORY.resolve(name);
        try {
            Files.createDirectories(DIRECTORY);
            schematic = SpongeSchem.read(file);
            filename = name;
            origin = null;
            building = false;
            placed = 0;
            return "Loaded " + name + " (" + schematic.blocks().size() + " solid blocks)";
        } catch (IOException e) {
            return "Cannot load " + name + ": " + e.getMessage();
        }
    }
    public static String toggle(Minecraft mc) {
        if (mc.player == null) return "Enter a world first";
        if (schematic == null) return "Use /corz load <filename.schem> first";
        if (!Features.get("schematic_builder").enabled) return "Enable Schematic Auto Build in ClickGUI first";
        if (!building) {
            if (origin == null) origin = mc.player.blockPosition().offset(2,0,2);
            building = true;
        } else building = false;
        return (building ? "Building near " : "Paused at ") + origin.toShortString();
    }
    public static void tick(Minecraft mc) {
        if (mc.player == null || mc.level == null) { building = false; origin = null; return; }
        if (!building || !Features.get("schematic_builder").enabled || mc.gameMode == null || mc.screen != null) return;
        if (++clock % 5 != 0) return;
        if (schematic == null || origin == null) { building = false; return; }
        int outstanding = 0;
        for (SpongeSchem.Block block : schematic.blocks()) {
            BlockPos at = origin.offset(block.x(),block.y(),block.z());
            if (!mc.level.hasChunkAt(at)) { outstanding++; continue; }
            String current = BuiltInRegistries.BLOCK.getKey(mc.level.getBlockState(at).getBlock()).toString();
            if (block.blockId().equals(current)) continue;
            outstanding++;
            // A state such as [facing=north] needs dedicated orientation logic; skip, do not place it incorrectly.
            if (block.state().contains("[")) continue;
            if (!mc.level.getBlockState(at).canBeReplaced() || mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(at)) > 20.25) continue;
            if (mc.player.getBoundingBox().intersects(new AABB(at))) continue;
            int slot = findBlock(mc,block.blockId());
            if (slot < 0) continue;
            for (Direction side : Direction.values()) {
                BlockPos support = at.relative(side);
                if (mc.level.getBlockState(support).isAir() || !mc.level.getWorldBorder().isWithinBounds(at)) continue;
                // Clicking the support's opposite face places into the empty target cell.
                Direction clickedFace = side.getOpposite();
                mc.player.getInventory().setSelectedSlot(slot);
                BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(support).add(clickedFace.getStepX()*0.5, clickedFace.getStepY()*0.5, clickedFace.getStepZ()*0.5), clickedFace, support, false);
                mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, hit);
                mc.player.swing(InteractionHand.MAIN_HAND);
                placed++;
                return;
            }
        }
        if (outstanding == 0) {
            building = false;
            mc.player.sendSystemMessage(Component.literal("[CorzStyle] Schematic complete (" + placed + " placement attempts)"));
        }
        // If no reachable blocks can be placed, stay paused in the same position; move closer or add supports.
    }
    private static int findBlock(Minecraft mc, String id) {
        for (int i=0;i<9;i++) {
            var stack = mc.player.getInventory().getItem(i);
            if (stack.getItem() instanceof BlockItem item && BuiltInRegistries.BLOCK.getKey(item.getBlock()).toString().equals(id)) return i;
        }
        return -1;
    }
    public static String status() {
        return filename.isEmpty() ? "No schematic loaded" : filename + ", origin=" + (origin == null ? "not set" : origin.toShortString()) + ", attempts=" + placed + ", running=" + building;
    }
    private SchematicBuilder() {}
}
