package sgc.api.gui;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;
import sgc.blocks.structureblock.StructureBlockTile;

public class BlockPreviewRenderer {

    private static final int PREVIEW_SIZE = 150;

    public static void drawBlockPreview(StructureBlockTile te, NBTTagCompound loadedNbt,
                                        int previewX, int previewY, int guiWidth, int guiHeight,
                                        float zoomLevel, net.minecraft.client.Minecraft mc,
                                        net.minecraft.client.gui.FontRenderer fontRenderer) {
        drawRect(previewX - 1, previewY - 1, previewX + PREVIEW_SIZE + 1, previewY + PREVIEW_SIZE + 1, 0xFF555555);
        drawRect(previewX, previewY, previewX + PREVIEW_SIZE, previewY + PREVIEW_SIZE, 0xFF000000);

        String previewTitle = "Preview";
        fontRenderer.drawString(previewTitle, previewX, previewY - 12, 0xFFFFFF);

        if (loadedNbt == null) return;

        NBTTagList palette = loadedNbt.getTagList("palette", 10);
        NBTTagList blocks = loadedNbt.getTagList("blocks", 10);
        NBTTagList size = loadedNbt.getTagList("size", 3);

        int sizeX = size.getIntAt(0);
        int sizeY = size.getIntAt(1);
        int sizeZ = size.getIntAt(2);

        GlStateManager.pushMatrix();
        GlStateManager.enableDepth();
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableTexture2D();

        mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        int scaleFactor = new ScaledResolution(mc).getScaleFactor();
        GL11.glScissor(previewX * scaleFactor,
                mc.displayHeight - (previewY + PREVIEW_SIZE) * scaleFactor,
                PREVIEW_SIZE * scaleFactor,
                PREVIEW_SIZE * scaleFactor);

        GlStateManager.translate(previewX + PREVIEW_SIZE / 2, previewY + PREVIEW_SIZE / 2, 100.0F);

        float maxSize = Math.max(sizeX, Math.max(sizeY, sizeZ));
        float blockScale = ((PREVIEW_SIZE / 2.0F) / maxSize) * zoomLevel;
        GlStateManager.scale(blockScale, -blockScale, blockScale);

        long time = System.currentTimeMillis() % 10000;
        float angle = (time / 10000.0F) * 360.0F;
        GlStateManager.rotate(25.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(angle, 0.0F, 1.0F, 0.0F);

        GlStateManager.translate(-sizeX / 2.0F, -sizeY / 2.0F, -sizeZ / 2.0F);

        BlockRendererDispatcher dispatcher = mc.getBlockRendererDispatcher();

        for (int i = 0; i < blocks.tagCount(); i++) {
            NBTTagCompound blockEntry = blocks.getCompoundTagAt(i);
            NBTTagList posList = blockEntry.getTagList("pos", 3);
            int state = blockEntry.getInteger("state");

            if (state < 0 || state >= palette.tagCount()) continue;

            NBTTagCompound blockStateNbt = palette.getCompoundTagAt(state);
            String blockName = blockStateNbt.getString("Name");

            if (blockName.equals("minecraft:air")) continue;

            Block block = Block.getBlockFromName(blockName);
            if (block == null) continue;

            IBlockState blockState = block.getDefaultState();

            int count = posList.tagCount() / 3;
            for (int j = 0; j < count; j++) {
                int x = posList.getIntAt(j * 3);
                int y = posList.getIntAt(j * 3 + 1);
                int z = posList.getIntAt(j * 3 + 2);

                GlStateManager.pushMatrix();
                GlStateManager.translate(x, y, z);
                dispatcher.renderBlockBrightness(blockState, 1.0F);
                GlStateManager.popMatrix();
            }
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        GlStateManager.disableTexture2D();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableDepth();
        GlStateManager.popMatrix();
    }

    private static void drawRect(int left, int top, int right, int bottom, int color) {
        if (left < right) {
            int i = left;
            left = right;
            right = i;
        }
        if (top < bottom) {
            int j = top;
            top = bottom;
            bottom = j;
        }
        float f3 = (float)(color >> 24 & 255) / 255.0F;
        float f = (float)(color >> 16 & 255) / 255.0F;
        float f1 = (float)(color >> 8 & 255) / 255.0F;
        float f2 = (float)(color & 255) / 255.0F;
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(f, f1, f2, f3);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3f((float)left, (float)bottom, 0.0F);
        GL11.glVertex3f((float)right, (float)bottom, 0.0F);
        GL11.glVertex3f((float)right, (float)top, 0.0F);
        GL11.glVertex3f((float)left, (float)top, 0.0F);
        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }
}