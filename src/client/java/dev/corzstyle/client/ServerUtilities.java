package dev.corzstyle.client;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;

/** Opt-in DonutSMP-style helpers. No world-seed cracking, packet exploits or remote data access. */
public final class ServerUtilities {
    private static final Path LOG = FabricLoader.getInstance().getConfigDir().resolve("corzstyle/base-candidates.csv");
    private static final Map<Long, Integer> scores = new HashMap<>();
    private static int ahPrice = 1000;
    private static long lastRtpMs;
    private static int cursor = -1;
    private static BlockPos scanOrigin;
    private static int scanBestScore;
    private static BlockPos scanBestPos;
    private static final int SCAN_RADIUS = 22, SCAN_HEIGHT = 12;
    private static final int WIDTH = SCAN_RADIUS*2+1;
    private static final int VOLUME = WIDTH*WIDTH*(SCAN_HEIGHT*2+1);

    public static void price(int price) { ahPrice = price; }
    public static int price() { return ahPrice; }
    public static void ahSell(Minecraft mc) {
        if (!Features.get("ah_sell").enabled) { tell(mc,"Enable AH Sell in ClickGUI first"); return; }
        if (mc.player == null || mc.player.connection == null || mc.screen != null) return;
        if (mc.player.getMainHandItem().isEmpty()) { tell(mc,"Hold an item first"); return; }
        // No bypass: this is the normal player-issued server command.
        mc.player.connection.sendCommand("ah sell " + ahPrice);
        tell(mc,"Sent /ah sell " + ahPrice + " (server decides whether to accept)");
    }
    public static void rtpAndScan(Minecraft mc) {
        if (!Features.get("rtp_base_finder").enabled) { tell(mc,"Enable RTP Base Finder in ClickGUI first"); return; }
        if (mc.player == null || mc.player.connection == null || mc.screen != null) return;
        long now=System.currentTimeMillis();
        if (now-lastRtpMs < 20_000) { tell(mc,"RTP cooldown: wait 20 seconds"); return; }
        lastRtpMs=now;
        mc.player.connection.sendCommand("rtp");
        cursor=-2; // Wait for teleport/chunks before scanning.
        scanOrigin=mc.player.blockPosition();
        tell(mc,"Sent /rtp; scanning loaded terrain after teleport");
    }
    public static void scan(Minecraft mc) {
        if (!Features.get("rtp_base_finder").enabled) { tell(mc,"Enable RTP Base Finder first"); return; }
        if (mc.player == null || mc.level == null) return;
        startScan(mc);
        tell(mc,"Scanning nearby loaded blocks for base indicators...");
    }
    private static void startScan(Minecraft mc) {
        cursor=0;
        scanOrigin=mc.player.blockPosition();
        scanBestScore=0;
        scanBestPos=null;
        scores.clear();
    }
    public static void tick(Minecraft mc) {
        if (mc.player == null || mc.level == null) { cursor=-1; return; }
        if (cursor==-2) {
            // Start after the player's position changes from /rtp; no blind scanning before teleport.
            if (scanOrigin!=null && mc.player.blockPosition().distSqr(scanOrigin)>256) startScan(mc);
            return;
        }
        if (cursor<0) return;
        // Bound CPU cost: 3000 blocks at most per client tick, and only chunks available to this client.
        for (int n=0; n<3000 && cursor<VOLUME; n++,cursor++) {
            int x=cursor % WIDTH - SCAN_RADIUS;
            int z=(cursor/WIDTH) % WIDTH - SCAN_RADIUS;
            int y=(cursor/(WIDTH*WIDTH)) - SCAN_HEIGHT;
            BlockPos at=scanOrigin.offset(x,y,z);
            if (!mc.level.hasChunkAt(at)) continue;
            String id=BuiltInRegistries.BLOCK.getKey(mc.level.getBlockState(at).getBlock()).toString();
            int score=weight(id);
            if (score == 0) continue;
            long chunk=((long)(at.getX()>>4)<<32) | ((at.getZ()>>4)&0xffffffffL);
            int total=scores.merge(chunk,score,Integer::sum);
            if (total>scanBestScore) {scanBestScore=total; scanBestPos=at.immutable();}
        }
        if (cursor>=VOLUME) {
            cursor=-1;
            if (scanBestPos!=null && scanBestScore>=12) {
                tell(mc,"Possible base near " + scanBestPos.toShortString() + " (heuristic score " + scanBestScore + ")");
                recordCandidate(mc,scanBestPos,scanBestScore);
            } else tell(mc,"No strong base indicators in loaded blocks nearby");
        }
    }
    private static int weight(String id) {
        if (id.endsWith("_shulker_box") || id.equals("minecraft:shulker_box")) return 12;
        if (id.equals("minecraft:chest") || id.equals("minecraft:trapped_chest") || id.equals("minecraft:barrel") || id.equals("minecraft:ender_chest")) return 9;
        if (id.equals("minecraft:hopper") || id.equals("minecraft:beacon")) return 9;
        if (id.contains("furnace") || id.equals("minecraft:smoker") || id.equals("minecraft:blast_furnace")) return 6;
        if (id.endsWith("_bed") || id.endsWith("_sign") || id.endsWith("_wall_sign")) return 5;
        if (id.equals("minecraft:crafting_table") || id.equals("minecraft:enchanting_table")) return 3;
        return 0;
    }
    private static void recordCandidate(Minecraft mc, BlockPos position,int score) {
        try {
            Files.createDirectories(LOG.getParent());
            if (!Files.exists(LOG)) Files.writeString(LOG,"time,server,x,y,z,score\n", StandardCharsets.UTF_8,StandardOpenOption.CREATE);
            String server = mc.getCurrentServer()==null ? "singleplayer" : mc.getCurrentServer().ip.replaceAll("[^a-zA-Z0-9.:_-]", "_");
            Files.writeString(LOG, Instant.now()+","+server+","+position.getX()+","+position.getY()+","+position.getZ()+","+score+"\n",StandardCharsets.UTF_8,StandardOpenOption.APPEND);
        } catch (IOException e) {tell(mc,"Could not save base candidate: " + e.getMessage());}
    }
    public static String status() {return "AH price="+ahPrice+", base scan=" + (cursor>=0 ? "running" : cursor==-2 ? "waiting for RTP" : "idle");}
    public static void tell(Minecraft mc,String message) {
        if (mc.player != null) mc.player.sendSystemMessage(Component.literal("[CorzStyle] " +message));
    }
    private ServerUtilities(){}
}
