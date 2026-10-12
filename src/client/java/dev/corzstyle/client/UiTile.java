package dev.corzstyle.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class UiTile extends AbstractWidget {
    private final String description;
    private final String badge;
    private final boolean enabled;
    private final boolean implemented;
    private final Runnable action;
    private final int base;

    public UiTile(int x, int y, int w, int h, String name, String description, String badge,
                  boolean enabled, boolean implemented, int base, Runnable action) {
        super(x, y, w, h, Component.literal(name));
        this.description = description;
        this.badge = badge;
        this.enabled = enabled;
        this.implemented = implemented;
        this.base = base;
        this.action = action;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) {
        int x = getX(), y = getY(), w = getWidth(), h = getHeight();
        int color = isHovered() ? 0xFF22202C : base;
        int border = enabled ? 0xFF9259E9 : isHovered() ? 0xFF5D4879 : 0xFF303034;
        round(g, x, y, w, h, border);
        round(g, x + 1, y + 1, w - 2, h - 2, color);
        var font = Minecraft.getInstance().font;
        g.text(font, getMessage().getString(), x + 12, y + 11, 0xFFF5F4F8, true);
        if (h >= 45 && !description.isBlank()) {
            String text = description;
            int max = w - 25;
            if (font.width(text) > max) text = font.plainSubstrByWidth(text, max - 13) + "...";
            g.text(font, text, x + 12, y + 30, 0xFF86808D, false);
        }
        if (enabled) g.text(font, "ON", x + w - 24, y + 11, 0xFFB39AFF, true);
        else if (!implemented) g.text(font, "SOON", x + w - 38, y + 11, 0xFF817C89, false);
        else if (!badge.isEmpty()) g.text(font, badge, x + w - Math.max(30, font.width(badge) + 12), y + 11,
                badge.equals("NEW") ? 0xFF6FE1AC : 0xFFB69BE9, false);
    }

    private static void round(GuiGraphicsExtractor g, int x, int y, int w, int h, int color) {
        if (w <= 0 || h <= 0) return;
        g.fill(x + 3, y, x + w - 3, y + h, color);
        g.fill(x, y + 3, x + w, y + h - 3, color);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, color);
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) { if (action != null) action.run(); }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) { }
}
