package sgc.supporting;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HelperGui {
    public static class GuiScrollPanel {
        private Minecraft mc;
        private float x;
        private float y;
        private float widthBg, heightBg;
        private float widthT, heightT;
        private float widthS, heightS;

        private int bgColor = 0x404040FF, trackColor = 0x606060FF, sliderColor = 0x808080FF;

        private String title;
        private int titleColor = 0xFFFFFF;
        private float xTitle, yTitle;

        private ResourceLocation texture;
        private int textureAtlW, textureAtlH;
        private boolean bg = false, track = false, slider = false;

        private int bgU, bgV, bgW, bgH;
        private float xTrack, yTrack;
        private int trackU, trackV, trackW, trackH;
        private boolean isSliderStaticSize = false;
        private int sliderU, sliderV, sliderW, sliderH;
        private float sliderTopIndent = 0f, sliderBottomIndent = 0f;

        private boolean debugModeSH = false;
        private boolean customSH = false;
        private float shX, shY, shWidth, shHeight;

        private List<ScrollPanelEntry> entryList = new ArrayList<>();
        private float entryWidth, entryHeight;
        private float currentScroll = 0f;
        private float lastContentHeight = 0f;

        private boolean isDragging = false;
        private float dragOffset = 0f;

        public GuiScrollPanel(Minecraft mc, float x, float y, float widthBg, float heightBg, float xTrack, float yTrack, float widthT, float heightT, boolean isSliderStaticSize, float widthS, float heightS, float sliderTopIndent, float sliderBottomIndent) {
            this.mc = mc;
            this.x = x; this.y = y; this.widthBg = widthBg; this.heightBg = heightBg;
            this.xTrack = xTrack; this.yTrack = yTrack; this.widthT = widthT; this.heightT = heightT;
            this.isSliderStaticSize = isSliderStaticSize; this.widthS = widthS; this.heightS = heightS; this.sliderTopIndent = sliderTopIndent; this.sliderBottomIndent = sliderBottomIndent;
        }

        public void setBasicColor(int bgColor, int trackColor, int sliderColor) { this.bgColor = bgColor; this.trackColor = trackColor; this.sliderColor = sliderColor; }

        public void setTitle(float xTitle, float yTitle, String title, int titleColor) { this.xTitle = xTitle; this.yTitle = yTitle; this.title = title; this.titleColor = titleColor; }
        public void setCustomTexture(@Nullable ResourceLocation texture, int textureAtlW, int textureAtlH, boolean bg, boolean track, boolean slider) {
            this.texture = texture; this.textureAtlW = textureAtlW; this.textureAtlH = textureAtlH;
            this.bg = bg; this.track = track; this.slider = slider;
        }

        public void setBg(int bgU, int bgV, int bgW, int bgH) { this.bgU = bgU; this.bgV = bgV; this.bgW = bgW; this.bgH = bgH; }
        public void setTrack(int trackU, int trackV, int trackW, int trackH) { this.trackU = trackU; this.trackV = trackV; this.trackW = trackW; this.trackH = trackH; }
        public void setSlider(int sliderU, int sliderV, int sliderW, int sliderH) { this.sliderU = sliderU; this.sliderV = sliderV; this.sliderW = sliderW; this.sliderH = sliderH; }

        public void setSH(boolean debugModeSH, boolean customSH, float shX, float shY, float shWidth, float shHeight) { this.debugModeSH = debugModeSH; this.customSH = customSH; this.shX = shX; this.shY = shY; this.shWidth = shWidth; this.shHeight = shHeight; }

        public List<ScrollPanelEntry> getEntryList() { return entryList; }
        public void setEntryWH(float entryWidth, float entryHeight) { this.entryWidth = entryWidth; this.entryHeight = entryHeight; }
        public float getEntryWidth() { return this.entryWidth; }
        public float getEntryHeight() { return this.entryHeight; }

        public void drawScreen(int mouseX, int mouseY, float partialTicks) {
            drawBG();
            drawTitle();

            drawSH();

            drawTrack();
            drawSlider();
        }

        private void drawBG() {
            if (bg && texture != null) {
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                mc.getTextureManager().bindTexture(texture);
                drawScaledCustomSize(x, y, bgU, bgV, bgW, bgH, widthBg, heightBg, textureAtlW, textureAtlH);
            } else drawRectS(x, y, x + widthBg, y + heightBg, bgColor);
        }
        private void drawTitle() { if (title != null && !title.isEmpty()) mc.fontRenderer.drawString(title, xTitle, yTitle, titleColor, false); }
        private void drawSH() {
            // Рассчитываем видимую область контента
            float contentX = getContentX();
            float contentY = getContentY();
            float contentWidth = getContentWidth();
            float contentHeight = getVisibleContentHeight();

            ScissorHelper.setDebugMode(debugModeSH);
            ScissorHelper.startScissor(mc, contentX, contentY, contentWidth, contentHeight);
            drawContent(contentX, contentY, contentWidth, contentHeight);
            ScissorHelper.endScissor();
        }
        private void drawContent(float contentX, float contentY, float contentWidth, float contentHeight) {
            if (entryList.isEmpty()) return;

            ScaledResolution res = new ScaledResolution(mc);
            int mouseX = Mouse.getX() / res.getScaleFactor();
            int mouseY = (mc.displayHeight - Mouse.getY()) / res.getScaleFactor();

            int visibleCount = (int) Math.ceil(contentHeight / entryHeight); // Рассчитываем сколько элементов помещается в видимой области
            int firstVisible = (int) (currentScroll / entryHeight); // Определяем первый видимый элемент
            firstVisible = Math.max(0, Math.min(firstVisible, entryList.size() - visibleCount));

            // Отрисовываем только видимые элементы
            for (int i = firstVisible; i < Math.min(entryList.size(), firstVisible + visibleCount + 1); i++) {
                float yPos = contentY + (i * entryHeight) - currentScroll;

                // Проверяем, находится ли элемент в видимой области
                if (yPos + entryHeight >= contentY && yPos <= contentY + contentHeight) entryList.get(i).drawEntry(i, contentX, yPos, entryWidth, entryHeight, mouseX, mouseY);
            }
        }
        private void drawTrack() {
            if (track && texture != null) {
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                mc.getTextureManager().bindTexture(texture);
                drawScaledCustomSize(xTrack, yTrack, trackU, trackV, trackW, trackH, widthT, heightT, textureAtlW, textureAtlH);
            } else drawRectS(xTrack, yTrack, xTrack + widthT, yTrack + heightT, trackColor);
        }
        private void drawSlider() {
            float contentHeight =  getVisibleContentHeight();
            float totalContentHeight = entryList.size() * entryHeight;

            if (totalContentHeight < lastContentHeight) {
                float diff = lastContentHeight - totalContentHeight;
                currentScroll = Math.max(0, currentScroll - diff);
            }
            lastContentHeight = totalContentHeight;

            if (totalContentHeight <= contentHeight) return;

            float xSlider = xTrack + (widthT - widthS) / 2;
            float ySlider;
            float actualSliderHeight;

            if (isSliderStaticSize) {
                actualSliderHeight = heightS;
                float maxScroll = totalContentHeight - contentHeight;
                float scrollRatio = currentScroll / maxScroll;
                float availableTrackHeight = heightT - sliderTopIndent - sliderBottomIndent - actualSliderHeight;
                ySlider = yTrack + sliderTopIndent + availableTrackHeight * scrollRatio;
            } else {
                actualSliderHeight = Math.max(10f, heightT * (contentHeight / totalContentHeight));
                float maxScroll = totalContentHeight - contentHeight;
                float scrollRatio = currentScroll / maxScroll;
                float availableTrackHeight = heightT - sliderTopIndent - sliderBottomIndent - actualSliderHeight;
                ySlider = yTrack + sliderTopIndent + availableTrackHeight * scrollRatio;
            }

            ySlider = Math.max(yTrack + sliderTopIndent, Math.min(yTrack + heightT - sliderBottomIndent - actualSliderHeight, ySlider));

            if (slider && texture != null) {
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                mc.getTextureManager().bindTexture(texture);
                drawScaledCustomSize(xSlider, ySlider, sliderU, sliderV, sliderW, sliderH, widthS, actualSliderHeight, textureAtlW, textureAtlH);
            } else drawRectS(xSlider, ySlider, xSlider + widthS, ySlider + actualSliderHeight, sliderColor);
        }

        public void handleMouseInput() throws IOException {
            ScaledResolution res = new ScaledResolution(mc);
            int scroll = Mouse.getEventDWheel();
            int mouseX = Mouse.getEventX() / res.getScaleFactor();
            int mouseY = (mc.displayHeight - Mouse.getEventY()) / res.getScaleFactor();
            if (scroll != 0 && (isMouseOverBackground(mouseX, mouseY) || isMouseOverTrack(mouseX, mouseY))) {
                float contentHeight =  getVisibleContentHeight();
                float totalContentHeight = entryList.size() * entryHeight;

                if (totalContentHeight > contentHeight) {
                    currentScroll -= scroll * 0.5f; // Меняем множитель для скорости прокрутки
                    currentScroll = MathHelper.clamp(currentScroll, 0f, totalContentHeight - contentHeight);
                }
            }
        }
        public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
            if (mouseButton == 0 && isMouseOverSlider(mouseX, mouseY)) {
                isDragging = true;
                dragOffset = mouseY - getCurrentSliderY();
                return true;
            }
            if (mouseButton == 0 && isMouseOverTrack(mouseX, mouseY)) {
                float contentHeight = getVisibleContentHeight();
                float totalHeight = entryList.size() * entryHeight;

                if (totalHeight > contentHeight) {
                    float sliderY = getCurrentSliderY();
                    float sliderHeight = getCurrentSliderHeight();

                    if (mouseY < sliderY) currentScroll -= contentHeight * 0.5f;
                    else if (mouseY > sliderY + sliderHeight) currentScroll += contentHeight * 0.5f;
                    clampScrollPosition();
                }
                return true;
            }

            if (isMouseOverBackground(mouseX, mouseY)) {
                float contentX = getContentX();
                float contentY = getContentY();
                float contentWidth = getContentWidth();
                float contentHeight = getVisibleContentHeight();

                int visibleCount = (int) Math.ceil(contentHeight / entryHeight);
                int firstVisible = (int) (currentScroll / entryHeight);
                firstVisible = Math.max(0, Math.min(firstVisible, entryList.size() - visibleCount));

                for (int i = firstVisible; i < Math.min(entryList.size(), firstVisible + visibleCount + 1); i++) {
                    float yPos = contentY + (i * entryHeight) - currentScroll;
                    if (yPos + entryHeight >= contentY && yPos <= contentY + contentHeight) {
                        int relativeX = (int) (mouseX - contentX);
                        int relativeY = (int) (mouseY - yPos);

                        if (entryList.get(i).mousePressed(i, mouseX, mouseY, mouseButton, relativeX, relativeY)) return true;
                    }
                }
            }
            return false;
        }

        private boolean isMouseOverBackground(int mouseX, int mouseY) { return mouseX >= x && mouseX <= x + widthBg && mouseY >= y && mouseY <= y + heightBg; }
        private boolean isMouseOverTrack(int mouseX, int mouseY) { return mouseX >= xTrack && mouseX <= xTrack + widthT && mouseY >= yTrack && mouseY <= yTrack + heightT; }

        private boolean isMouseOverSlider(int mouseX, int mouseY) {
            float sliderY = getCurrentSliderY();
            float sliderHeight = getCurrentSliderHeight();
            return mouseX >= xTrack && mouseX <= xTrack + widthT && mouseY >= sliderY && mouseY <= sliderY + sliderHeight;
        }

        private float getCurrentSliderY() {
            float contentHeight = getVisibleContentHeight();
            float totalHeight = entryList.size() * entryHeight;
            float sliderHeight = getCurrentSliderHeight();

            if (totalHeight <= contentHeight) return yTrack;

            float maxScroll = totalHeight - contentHeight;
            float scrollRatio = currentScroll / maxScroll;
            return yTrack + sliderTopIndent + (heightT - sliderHeight - sliderTopIndent - sliderBottomIndent) * scrollRatio;
        }

        private float getCurrentSliderHeight() {
            if (isSliderStaticSize) {
                return heightS;
            } else {
                float contentHeight = getVisibleContentHeight();
                float totalHeight = entryList.size() * entryHeight;
                return Math.max(10f, heightT * (contentHeight / totalHeight));
            }
        }

        private void clampScrollPosition() {
            float contentHeight = getVisibleContentHeight();
            float totalHeight = entryList.size() * entryHeight;
            currentScroll = MathHelper.clamp(currentScroll, 0f, Math.max(0f, totalHeight - contentHeight));
        }

        public boolean mouseReleased(int mouseX, int mouseY, int mouseButton) {
            isDragging = false;
            return false;
        }

        public void updateMouse(int mouseX, int mouseY) {
            if (isDragging) {
                float contentHeight = getVisibleContentHeight();
                float totalHeight = entryList.size() * entryHeight;
                float sliderHeight = getCurrentSliderHeight();

                if (totalHeight > contentHeight) {
                    float newY = mouseY - dragOffset;
                    float trackStart = yTrack + sliderTopIndent;
                    float trackEnd = yTrack + heightT - sliderBottomIndent - sliderHeight;
                    float scrollRatio = (newY - trackStart) / (trackEnd - trackStart);

                    currentScroll = scrollRatio * (totalHeight - contentHeight);
                    clampScrollPosition();
                }
            }
        }

        private float getContentX() { return customSH ? shX : x + 5; }
        private float getContentY() { return customSH ? shY : y + (title != null ? yTitle + mc.fontRenderer.FONT_HEIGHT + 5 : 5); }
        private float getContentWidth() { return customSH ? shWidth : widthBg - widthT - 10; }
        private float getVisibleContentHeight() { return customSH ? shHeight : heightBg - (title != null ? yTitle + mc.fontRenderer.FONT_HEIGHT + 10 : 10); }

        public static abstract class ScrollPanelEntry {
            public abstract void drawEntry(int slotIndex, float x, float y, float listWidth, float slotHeight, int mouseX, int mouseY);
            public abstract boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY);
        }
    }
    public static class ScissorHelper {
        private static boolean debugMode = false;
        private static int debugColor = 0x80FF0000;

        public static void setDebugMode(boolean enabled) { debugMode = enabled; }
        public static void setDebugColor(int colorARGB) { debugColor = colorARGB; }

        public static void startScissorAngle(Minecraft mc, float left, float top, float right, float bottom) {
            ScaledResolution res = new ScaledResolution(mc);
            int scale = res.getScaleFactor();

            float width = right - left;
            float height = bottom - top;

            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            GL11.glScissor((int)(left * scale), (int)(mc.displayHeight - bottom * scale), (int)(width * scale), (int)(height * scale));

            if (debugMode) drawDebugOverlay(mc, left, top, width, height);
        }

        public static void startScissor(Minecraft mc, float x, float y, float width, float height) {
            startScissorAngle(mc, x, y, x + width, y + height);
        }

        private static void drawDebugOverlay(Minecraft mc, float x, float y, float width, float height) {
            GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            float a = (float)(debugColor >> 24 & 255) / 255.0F;
            float r = (float)(debugColor >> 16 & 255) / 255.0F;
            float g = (float)(debugColor >> 8 & 255) / 255.0F;
            float b = (float)(debugColor & 255) / 255.0F;

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();

            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            buffer.pos(x, y + height, 0).color(r, g, b, a).endVertex();
            buffer.pos(x + width, y + height, 0).color(r, g, b, a).endVertex();
            buffer.pos(x + width, y, 0).color(r, g, b, a).endVertex();
            buffer.pos(x, y, 0).color(r, g, b, a).endVertex();
            tessellator.draw();

            GL11.glPopAttrib();
        }

        public static void endScissor() { GL11.glDisable(GL11.GL_SCISSOR_TEST); }
    }

    public static void drawInfoImage(Minecraft mc, int mouseX, int mouseY, String info, int color, ResourceLocation texture, float x, float y, int u, int v, int uWidth, int vHeight, float width, float height, int tileWidth, int tileHeight) {
        boolean hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(texture);
        drawScaledCustomSize(x, y, u, v, uWidth, vHeight, width, height, tileWidth, tileHeight);

        if (hovered) {
            int textWidth = mc.fontRenderer.getStringWidth(info) + 10;
            int tooltipX = mouseX + 8;
            int tooltipY = mouseY - 8;
            int tooltipHeight = (int)height + 5;

            drawRectS(tooltipX, tooltipY, tooltipX + textWidth, tooltipY + tooltipHeight, 0x1E1E1EDD);
            mc.fontRenderer.drawString(info, tooltipX + 5, tooltipY + 2, color);
        }
    }
    public static void drawTextInBox(Minecraft mc, String text, float x, float y, int textColor, int boxColor, int borderColor, float padding) {
        // Получаем размеры текста
        int textWidth = mc.fontRenderer.getStringWidth(text);
        int textHeight = mc.fontRenderer.FONT_HEIGHT;

        // Рассчитываем размеры области с учетом отступов
        float boxWidth = textWidth + padding * 2;
        float boxHeight = textHeight + padding * 2;

        // Рисуем прямоугольник фона
        drawRectS(x, y, x + boxWidth, y + boxHeight, boxColor);

        // Рисуем границу
        drawBorderRect(x, y, boxWidth, boxHeight, 1f, borderColor);

        // Рисуем текст (центрированный по вертикали и горизонтали)
        Minecraft.getMinecraft().fontRenderer.drawString(text, x + padding, y + padding, textColor, false);
    }

    public static void drawRectS(float left, float top, float right, float bottom, int color) {
        float red = (float)(color >> 24 & 255) / 255.0F;
        float green = (float)(color >> 16 & 255) / 255.0F;
        float blue = (float)(color >> 8 & 255) / 255.0F;
        float alpha = (float)(color & 255) / 255.0F;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO
        );
        GlStateManager.color(red, green, blue, alpha);

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        buffer.pos(left, bottom, 0.0).endVertex();
        buffer.pos(right, bottom, 0.0).endVertex();
        buffer.pos(right, top, 0.0).endVertex();
        buffer.pos(left, top, 0.0).endVertex();
        tessellator.draw();

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }
    public static void drawBorderRect(float x, float y, float width, float height, float thickness, int color) {
        drawRectS(x, y, x + width, y + thickness, color);
        drawRectS(x, y + height - thickness, x + width, y + height, color);
        drawRectS(x, y, x + thickness, y + height, color);
        drawRectS(x + width - thickness, y, x + width, y + height, color);
    }

    public static void drawScaledCustomSize(float x, float y, int u, int v, int uWidth, int vHeight, float width, float height, int tileWidth, int tileHeight, int color) {
        float red = (float)(color >> 24 & 255) / 255.0F;
        float green = (float)(color >> 16 & 255) / 255.0F;
        float blue = (float)(color >> 8 & 255) / 255.0F;
        float alpha = (float)(color & 255) / 255.0F;

        GlStateManager.color(red, green, blue, alpha);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        float texU = u / (float)tileWidth;
        float texV = v / (float)tileHeight;
        float texUEnd = (u + uWidth) / (float)tileWidth;
        float texVEnd = (v + vHeight) / (float)tileHeight;

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(x, y + height, 0).tex(texU, texVEnd).endVertex();
        buffer.pos(x + width, y + height, 0).tex(texUEnd, texVEnd).endVertex();
        buffer.pos(x + width, y, 0).tex(texUEnd, texV).endVertex();
        buffer.pos(x, y, 0).tex(texU, texV).endVertex();
        tessellator.draw();

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }
    public static void drawScaledCustomSize(float x, float y, int u, int v, int uWidth, int vHeight, float width, float height, int tileWidth, int tileHeight) {
        drawScaledCustomSize(x, y, u, v, uWidth, vHeight, width, height, tileWidth, tileHeight, 0xFFFFFFFF);
    }
}