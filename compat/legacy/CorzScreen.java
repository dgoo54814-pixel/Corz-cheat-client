package dev.corzstyle.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/** Pre-26.x ClickGUI using the classic GuiGraphics API. */
public final class CorzScreen extends Screen {
    private final Screen previous;
    private final Feature.Group group;
    private final int page;
    private static final int PER_PAGE = 6;

    public CorzScreen(Screen previous) {
        this(previous, Feature.Group.COMBAT, 0);
    }

    private CorzScreen(Screen previous, Feature.Group group, int page) {
        super(Component.literal("Corz Client"));
        this.previous = previous;
        this.group = group;
        this.page = page;
    }

    @Override
    protected void init() {
        int x = width / 2 - 170;
        int y = height / 2 - 113;
        int categoryWidth = 66;
        Feature.Group[] groups = Feature.Group.values();
        for (int i = 0; i < groups.length; i++) {
            Feature.Group value = groups[i];
            addRenderableWidget(Button.builder(Component.literal(value.name()), b ->
                minecraft.setScreen(new CorzScreen(previous, value, 0)))
                .bounds(x + i * (categoryWidth + 3), y, categoryWidth, 20).build());
        }

        List<Feature> list = new ArrayList<>();
        for (Feature f : Features.ALL) if (f.groups.contains(group)) list.add(f);
        int pages = Math.max(1, (list.size() + PER_PAGE - 1) / PER_PAGE);
        int active = Math.min(page, pages - 1);
        for (int i = active * PER_PAGE; i < Math.min(list.size(), (active + 1) * PER_PAGE); i++) {
            Feature f = list.get(i);
            int row = i - active * PER_PAGE;
            String state = f.implemented ? (f.enabled ? " [ON]" : " [OFF]") : " [SOON]";
            addRenderableWidget(Button.builder(Component.literal(f.name + state), b -> {
                if (f.implemented) {
                    f.enabled = !f.enabled;
                    SettingsStore.save();
                }
                minecraft.setScreen(new CorzScreen(previous, group, active));
            }).bounds(x + 3, y + 28 + row * 26, 333, 22).build());
        }

        if (active > 0) {
            addRenderableWidget(Button.builder(Component.literal("< Prev"), b ->
                minecraft.setScreen(new CorzScreen(previous, group, active - 1)))
                .bounds(x + 3, y + 187, 87, 20).build());
        }
        if (active < pages - 1) {
            addRenderableWidget(Button.builder(Component.literal("Next >"), b ->
                minecraft.setScreen(new CorzScreen(previous, group, active + 1)))
                .bounds(x + 249, y + 187, 87, 20).build());
        }
        addRenderableWidget(Button.builder(Component.literal("Close"), b -> onClose())
            .bounds(x + 130, y + 187, 78, 20).build());
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float partialTicks) {
        g.fill(0, 0, width, height, 0xC8000006);
        int x = width / 2 - 173;
        int y = height / 2 - 138;
        g.fill(x, y, x + 346, y + 26, 0xFF15111C);
        g.drawString(font, "CORZ CLIENT   |   Fabric 1.21.x", x + 10, y + 9, 0xFFBE94FF, true);
        super.render(g, mx, my, partialTicks);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(previous);
    }
}
