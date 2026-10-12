package dev.corzstyle.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static dev.corzstyle.client.Feature.Group.*;
import static dev.corzstyle.client.Feature.Badge.*;

public final class Features {
    public static final List<Feature> ALL = new ArrayList<>();

    private static void add(String id, String name, String desc, Feature.Badge badge, boolean working, Feature.Group... groups) {
        ALL.add(new Feature(id, name, desc, badge, working, groups));
    }

    static {
        // Screenshot-visible Combat cards. "SOON" means catalog-only, not a fake cheat.
        add("auto_crystal", "Auto Crystal", "Place and detonate end crystals", POPULAR, false, COMBAT);
        add("anchor_macro", "Anchor Macro", "Charge and detonate respawn anchors", ADVANCED, false, COMBAT);
        add("safe_anchor", "Safe Anchor", "Anchor placement with protection logic", NONE, false, COMBAT);
        add("auto_hit_crystal", "Auto Hit Crystal", "Detonate nearby end crystals", NONE, false, COMBAT);
        add("crystal_optimizer", "Crystal Optimizer", "Fast client-side crystal cleanup", ADVANCED, false, COMBAT);
        add("obsidian_optimizer", "Obsidian Optimizer", "Place obsidian on selected slot", NONE, false, COMBAT);
        add("auto_trap", "AutoTrap", "Enclose a nearby target in obsidian", ADVANCED, false, COMBAT);
        add("aim_assist", "Aim Assist", "Smooth aiming when attack is held", POPULAR, true, COMBAT);
        add("silent_aim", "Silent Aim", "Target aiming without turning camera", ADVANCED, false, COMBAT);
        add("trigger_bot", "Trigger Bot", "Attack when crosshair is on an entity", NONE, true, COMBAT);
        add("auto_clicker", "AutoClicker", "Repeat attacks at tick-limited intervals", NONE, true, COMBAT);
        add("criticals", "Criticals", "Critical hit helper", NONE, false, COMBAT);
        add("reach", "Reach", "Extended attack range", ADVANCED, false, COMBAT);
        add("hit_boxes", "Hit Boxes", "Larger target hit boxes", NONE, false, COMBAT);
        add("shield_disabler", "Auto Shield Disabler", "Swap to axe on shield block", NONE, false, COMBAT);
        add("auto_mace", "Auto Mace", "Time mace smashes", NEW, false, COMBAT);
        add("mace_swap", "Mace Swap", "Hotbar mace swap on hit", NONE, false, COMBAT);
        add("wind_charge", "Perfect Wind Charge", "Wind-charge launch helper", NEW, false, COMBAT);
        add("auto_totem", "Auto Totem", "Replace a consumed offhand totem", POPULAR, false, COMBAT, DONUTSMP);
        add("inventory_totem", "Auto Inventory Totem", "Keep a totem in your offhand", NONE, false, COMBAT, DONUTSMP);
        add("hotbar_refill", "Auto Refill Hotbar", "Refill consumables from inventory", NONE, false, COMBAT, DONUTSMP);
        add("auto_xp", "Auto XP", "Throw XP bottles held in main hand", NONE, true, COMBAT, DONUTSMP);
        add("key_pearl", "Key Pearl", "Press V to throw pearl from hotbar", NONE, true, COMBAT, DONUTSMP);
        add("aggro_pearl", "Auto Aggro Pearl", "Follow enemy pearl teleport", ADVANCED, false, COMBAT, DONUTSMP);
        // Extras: actual implementations with their own categories.
        add("auto_sprint", "Auto Sprint", "Sprint automatically while moving", POPULAR, true, MOVEMENT);
        add("fullbright", "Fullbright", "Use highest vanilla gamma option", POPULAR, true, VISUALS);
        add("coordinates", "Coordinates HUD", "Position overlay (planned)", NONE, false, VISUALS);
        add("fps", "FPS HUD", "Frames-per-second overlay (planned)", NONE, false, VISUALS);
        add("no_fog", "No Fog", "Remove fog (planned)", NONE, false, VISUALS);
        add("base_finds", "Base Finds", "Cloud listings require a real backend", NONE, false, DONUTSMP, MISC);
        add("scaffold", "Scaffold", "Automatic block placement (planned)", ADVANCED, false, MOVEMENT);
        add("inventory_move", "Inventory Move", "Move when menus are open (planned)", NONE, false, MOVEMENT);
        add("schematic_builder", "Schematic Auto Build", "Build nearby blocks from .schem; key B starts/pauses", NEW, true, DONUTSMP, MISC);
        add("ah_sell", "AH Sell", "Press H to sell held item at configured price", NEW, true, DONUTSMP, MISC);
        add("rtp_base_finder", "RTP Base Finder", "Press J for /rtp + scan nearby loaded chunks", POPULAR, true, DONUTSMP, MISC);
        add("auto_reconnect", "Auto Reconnect", "Reconnect after a kick (planned)", NONE, false, MISC);
    }

    public static Feature get(String id) {
        for (Feature feature : ALL) if (feature.id.equals(id)) return feature;
        throw new IllegalArgumentException("No feature: " + id);
    }

    public static int count(Feature.Group group) {
        int count = 0;
        for (Feature f : ALL) if (f.groups.contains(group)) count++;
        return count;
    }

    private Features() {}
}
