package sgc.supporting;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.util.text.translation.I18n;

import java.util.Collections;
import java.util.List;

public class GuiContainerHandler extends GuiContainer {
    public GuiContainerHandler(Container container) { super(container); }
    @Override protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {}

    public String translate(String key) { return I18n.translateToLocalFormatted(key); }
    public String translate(String key, Object... args) { return I18n.translateToLocalFormatted(key, args); }

    public boolean isMouseOver(int x1, int y1, int x2, int y2, int mouseX, int mouseY) { return mouseX >= x1 && mouseX < x1 + x2 && mouseY >= y1 && mouseY < y1 + y2; }

    public void drawInfoBoxText(String text, int x1, int y1, int x2, int y2, int mouseX, int mouseY) {
        if (isMouseOver(x1, y1, x2, y2, mouseX, mouseY)) drawHoveringText(Collections.singletonList(text), mouseX, mouseY);
    }
    public void drawInfoBoxTextLines(List<String> textLines, int x1, int y1, int x2, int y2, int mouseX, int mouseY) {
        if (isMouseOver(x1, y1, x2, y2, mouseX, mouseY)) drawHoveringText(textLines, mouseX, mouseY);
    }

    public void drawHorizontalPBar(float x, float y, int u, int v, int uWidth, int vHeight, float pro, float maxPro, int tileWidth, int tileHeight) {
        int barW = (int) ((pro / maxPro) * uWidth);
        if (barW > 0) HelperGui.drawScaledCustomSize(x, y, u, v, barW, vHeight, barW, vHeight, tileWidth, tileHeight);
    }
    public void drawVerticalPBar(float x, float y, int u, int v, int uWidth, int vHeight, float pro, float maxPro, int tileWidth, int tileHeight) {
        int barH = (int) ((pro / maxPro) * vHeight);
        if (barH > 0) {
            int barY = v + (vHeight - barH);
            HelperGui.drawScaledCustomSize(x, y + (vHeight - barH), u, barY, uWidth, barH, uWidth, barH, tileWidth, tileHeight);
        }
    }

    public void drawBox(int x, int y, int width, int height, int color) { drawRect(x, y, x + width, y + height, color); }

    public void drawSwingingImage(float x, float y, int u, int v, int uWidth, int vHeight, float width, float height, int tileWidth, int tileHeight, float amplitude, float speed, float time) {
        float cycleProgress = (time % speed) / speed;
        float offset = amplitude * (float)Math.sin(cycleProgress < 0.5f ? cycleProgress * Math.PI : (cycleProgress - 0.5f) * Math.PI) * (cycleProgress < 0.5f ? 1 : -1);

        HelperGui.drawScaledCustomSize(x + offset, y, u, v, uWidth, vHeight, width, height, tileWidth, tileHeight);
    }
}
