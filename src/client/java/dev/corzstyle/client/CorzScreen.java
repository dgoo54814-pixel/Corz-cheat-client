package dev.corzstyle.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class CorzScreen extends Screen {
    private final Screen previous;
    private Feature.Group group = Feature.Group.COMBAT;
    private String query = "";
    private EditBox searchBox;
    private int scroll;
    private int panelX, panelY, panelW, panelH, contentX, contentY, contentW, viewportHeight;
    private boolean dirty;
    private String status = "RSHIFT GUI  |  B Build  |  H AH Sell  |  J RTP  |  K Scan";
    private static final int BG = 0xFF0C0C0E, PANEL = 0xFF131317, SUB = 0xFF19191E, PURPLE = 0xFF9251E9;

    public CorzScreen(Screen previous) { super(Component.literal("Corz-Inspired Client")); this.previous = previous; }

    @Override
    protected void init() {
        panelW = Math.min(770, width - 24);
        panelH = Math.min(486, height - 22);
        panelX = (width - panelW) / 2;
        panelY = (height - panelH) / 2;
        contentX = panelX + Math.min(154, Math.max(112, panelW / 4));
        contentW = panelX + panelW - contentX - 14;
        contentY = panelY + 110;
        viewportHeight = panelH - 143;
        createWidgets();
    }

    private void createWidgets() {
        int sx = panelX + 12;
        int menuWidth = contentX - sx - 12;
        int y = panelY + 84;
        addRenderableWidget(new UiTile(sx, y, menuWidth, 28, "Combat ("+Features.count(Feature.Group.COMBAT)+")", "", "", group == Feature.Group.COMBAT, true, SUB, () -> select(Feature.Group.COMBAT)));
        y += 34;
        addRenderableWidget(new UiTile(sx, y, menuWidth, 28, "DonutSMP ("+Features.count(Feature.Group.DONUTSMP)+")", "", "", group == Feature.Group.DONUTSMP, true, SUB, () -> select(Feature.Group.DONUTSMP)));
        y += 34;
        addRenderableWidget(new UiTile(sx, y, menuWidth, 28, "Visuals", "", "", group == Feature.Group.VISUALS, true, SUB, () -> select(Feature.Group.VISUALS)));
        y += 34;
        addRenderableWidget(new UiTile(sx, y, menuWidth, 28, "Movement", "", "", group == Feature.Group.MOVEMENT, true, SUB, () -> select(Feature.Group.MOVEMENT)));
        y += 34;
        addRenderableWidget(new UiTile(sx, y, menuWidth, 28, "Misc", "", "", group == Feature.Group.MISC, true, SUB, () -> select(Feature.Group.MISC)));
        y += 46;
        addRenderableWidget(new UiTile(sx, y, menuWidth, 28, "Save config", "", "", false, true, SUB,
                () -> { SettingsStore.save(); status = "Settings saved"; }));
        y += 34;
        addRenderableWidget(new UiTile(sx, y, menuWidth, 28, "Reset toggles", "", "", false, true, SUB,
                () -> { for (Feature f : Features.ALL) f.enabled = false; SettingsStore.save(); status = "Toggles reset"; dirty = true; }));

        searchBox = new EditBox(font, contentX, panelY + 75, contentW, 22, Component.literal("Search modules"));
        searchBox.setMaxLength(64);
        searchBox.setValue(query);
        searchBox.setHint(Component.literal("Search modules..."));
        searchBox.setResponder(value -> { if (!query.equals(value)) { query = value; scroll = 0; dirty = true; } });
        addRenderableWidget(searchBox);

        List<Feature> filtered = filtered();
        int row = 0;
        for (Feature f : filtered) {
            int yy = contentY + row * 58 - scroll;
            row++;
            if (yy < contentY - 58 || yy + 54 > contentY + viewportHeight) continue;
            String badge = f.badge == Feature.Badge.NONE ? "" : f.badge.name();
            addRenderableWidget(new UiTile(contentX, yy, contentW, 52, f.name, f.description, badge,
                    f.enabled, f.implemented, 0xFF121216, () -> toggle(f)));
        }
    }

    private List<Feature> filtered() {
        List<Feature> filtered = new ArrayList<>();
        String q = query.toLowerCase(Locale.ROOT);
        for (Feature f : Features.ALL) if (f.groups.contains(group)
                && (q.isEmpty() || f.name.toLowerCase(Locale.ROOT).contains(q)
                || f.description.toLowerCase(Locale.ROOT).contains(q))) filtered.add(f);
        return filtered;
    }

    private void select(Feature.Group selected) { group = selected; scroll = 0; dirty = true; }

    private void toggle(Feature f) {
        if (!f.implemented) {
            status = f.name + " - catalog entry only; not implemented";
            return;
        }
        f.enabled = !f.enabled;
        status = f.name + (f.enabled ? " enabled" : " disabled");
        SettingsStore.save();
        dirty = true;
    }

    @Override
    public void tick() {
        super.tick();
        if (dirty) {
            dirty = false;
            boolean focus = searchBox != null && searchBox.isFocused();
            clearWidgets();
            createWidgets();
            if (focus) { searchBox.setFocused(true); setFocused(searchBox); }
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        if (mouseX < contentX || mouseX > contentX + contentW || mouseY < contentY) return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
        int total = filtered().size() * 58;
        scroll = Math.max(0, Math.min(Math.max(0, total - viewportHeight), scroll - (int) (vertical * 35)));
        dirty = true;
        return true;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mx, int my, float delta) {
        graphics.fill(0, 0, width, height, 0xDC050508);
        graphics.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xFF35303D);
        graphics.fill(panelX + 1, panelY + 1, panelX + panelW - 1, panelY + panelH - 1, PANEL);
        graphics.fill(panelX + 1, panelY + 1, panelX + panelW - 1, panelY + 54, 0xFF0B0D12);
        graphics.fill(panelX + 10, panelY + 20, panelX + 14, panelY + 34, PURPLE);
        graphics.text(font, "CORZ", panelX + 24, panelY + 16, 0xFFFDFDFF, true);
        graphics.text(font, "INSPIRED / 26.1.2", panelX + 60, panelY + 17, 0xFFA89CB5, false);
        graphics.text(font, "FEATURES", panelX + 12, panelY + 62, 0xFF87848E, false);
        graphics.text(font, group.name() + " MODULES", contentX, panelY + 62, 0xFFFFFFFF, true);
        graphics.fill(contentX - 9, panelY + 61, contentX - 8, panelY + panelH - 29, 0xFF373139);
        graphics.fill(panelX + 12, panelY + panelH - 25, panelX + panelW - 12, panelY + panelH - 24, 0xFF39323C);
        graphics.text(font, status, panelX + 12, panelY + panelH - 18, 0xFF8C8794, false);
        super.extractRenderState(graphics, mx, my, delta);
    }

    @Override
    public void onClose() { minecraft.setScreen(previous); }
}
