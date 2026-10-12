from pathlib import Path
import re
root=Path("src/client/java/dev/corzstyle/client")
mc_version=__import__("os").environ["MC"]
for p in root.glob("*.java"):
    s=p.read_text()
    s=s.replace("net.minecraft.resources.Identifier","net.minecraft.resources.ResourceLocation")
    s=s.replace("Identifier.fromNamespaceAndPath","ResourceLocation.fromNamespaceAndPath")
    s=s.replace("net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper","net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper")
    s=s.replace("KeyMappingHelper.registerKeyMapping","KeyBindingHelper.registerKeyBinding")
    s=s.replace("net.fabricmc.fabric.api.client.command.v2.ClientCommands","net.fabricmc.fabric.api.client.command.v2.ClientCommandManager")
    s=s.replace("ClientCommands.", "ClientCommandManager.")
    s=s.replace("mc.player.sendSystemMessage(", "mc.player.displayClientMessage(")
    s=s.replace("mc.player.displayClientMessage(Component.literal(\"[CorzStyle] \" + message))", "mc.player.displayClientMessage(Component.literal(\"[CorzStyle] \" + message), false)")
    # GUI rendering was refactored in 26.x.
    s=s.replace("net.minecraft.client.gui.GuiGraphicsExtractor","net.minecraft.client.gui.GuiGraphics")
    s=s.replace("GuiGraphicsExtractor","GuiGraphics")
    s=s.replace("protected void extractWidgetRenderState(", "protected void renderWidget(")
    s=s.replace("public void extractRenderState(", "public void render(")
    s=s.replace("g.text(", "g.drawString(")
    s=s.replace("graphics.text(", "graphics.drawString(")
    s=s.replace("net.minecraft.client.input.MouseButtonEvent", "net.minecraft.client.input.MouseButtonEvent") # newer only
    if p.name=="UiTile.java":
        s=s.replace("import net.minecraft.client.input.MouseButtonEvent;\n","")
        s=s.replace("public void onClick(MouseButtonEvent event, boolean doubleClick)","public void onClick(double mouseX, double mouseY)")
    if p.name=="CorzClient.java":
        s=s.replace("ClientTickEvents.END_CLIENT_TICK.register((Minecraft mc) ->", "ClientTickEvents.END_CLIENT_TICK.register(mc ->")
        # 1.21.11 has key mapping category record, but older 1.21 releases used a String.
        if mc_version!="1.21.11":
            s=s.replace('var category = KeyMapping.Category.register(ResourceLocation.fromNamespaceAndPath("corzstyle", "main"));','String category = "key.category.corzstyle.main";')
    p.write_text(s)
