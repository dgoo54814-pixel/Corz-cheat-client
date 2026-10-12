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

# Dedicated source entrypoint / GUI for older Minecraft.
from shutil import copyfile
for name in ("CorzClient.java", "CorzScreen.java", "CorzTickMixin.java"):
    copyfile(Path("compat/legacy") / name, root / name)
for name in ("CorzCommands.java", "UiTile.java"):
    (root / name).unlink(missing_ok=True)

# Older LocalPlayer.displayClientMessage() requires a boolean action bar flag.
for p in root.glob("*.java"):
    s=p.read_text()
    s=re.sub(r'displayClientMessage\((Component\.literal\(.*?\))\);', r'displayClientMessage(\1, false);',s)
    p.write_text(s)

import json
meta=Path("src/main/resources/fabric.mod.json")
info=json.loads(meta.read_text())
info["mixins"]=["corzstyle.mixins.json"]
meta.write_text(json.dumps(info,indent=2))
Path("src/main/resources/corzstyle.mixins.json").write_text(json.dumps({
    "required":True,"package":"dev.corzstyle.client",
    "compatibilityLevel":"JAVA_21",
    "client":["CorzTickMixin"],
    "injectors":{"defaultRequire":1}
}, indent=2))
