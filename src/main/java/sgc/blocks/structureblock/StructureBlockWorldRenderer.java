package sgc.blocks.structureblock;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

public class StructureBlockWorldRenderer {

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null || mc.world == null) return;

        double viewerX = mc.getRenderManager().viewerPosX;
        double viewerY = mc.getRenderManager().viewerPosY;
        double viewerZ = mc.getRenderManager().viewerPosZ;

        // Iterate through all loaded tile entities
        for (TileEntity te : mc.world.loadedTileEntityList) {
            if (te instanceof StructureBlockTile) {
                StructureBlockTile structureTE = (StructureBlockTile) te;
                BlockPos pos = te.getPos();

                double offsetX = pos.getX() - viewerX;
                double offsetY = pos.getY() - viewerY;
                double offsetZ = pos.getZ() - viewerZ;

                renderStructureOverlay(structureTE, offsetX, offsetY, offsetZ);
            }
        }
    }

    private void renderStructureOverlay(StructureBlockTile te, double offsetX, double offsetY, double offsetZ) {
        BlockPos c1 = te.getCorner1();
        BlockPos c2 = te.getCorner2();
        BlockPos pos = te.getPos();

        // Render save region
        if (!(c1.equals(BlockPos.ORIGIN) && c2.equals(BlockPos.ORIGIN))
                && (te.getLoadPosition().equals(BlockPos.ORIGIN) || !te.getShowLoadPreview())) {
            int startX = Math.min(c1.getX(), c2.getX()) - pos.getX();
            int startY = Math.min(c1.getY(), c2.getY()) - pos.getY();
            int startZ = Math.min(c1.getZ(), c2.getZ()) - pos.getZ();
            int endX = Math.max(c1.getX(), c2.getX()) - pos.getX() + 1;
            int endY = Math.max(c1.getY(), c2.getY()) - pos.getY() + 1;
            int endZ = Math.max(c1.getZ(), c2.getZ()) - pos.getZ() + 1;

            GlStateManager.pushMatrix();
            GlStateManager.translate(offsetX, offsetY, offsetZ);
            GlStateManager.disableTexture2D();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();

            float alphaValue = 0.5f + (float)(Math.sin(System.currentTimeMillis() * 0.005) * 0.3f);
            renderOutline(startX, startY, startZ, endX, endY, endZ, 0.0f, 1.0f, 0.0f, alphaValue);

            GlStateManager.enableDepth();
            GlStateManager.enableLighting();
            GlStateManager.disableBlend();
            GlStateManager.enableTexture2D();
            GlStateManager.popMatrix();
        }

        // Render load preview
        if (te.getShowLoadPreview() && !te.getLoadPosition().equals(BlockPos.ORIGIN)) {
            BlockPos loadPos = te.getLoadPosition();
            BlockPos size = te.getRegionSize();
            if (loadPos == null || size == null) return;

            int rotation = te.getRotation();
            int startX = loadPos.getX() - pos.getX();
            int startY = loadPos.getY() - pos.getY();
            int startZ = loadPos.getZ() - pos.getZ();
            int endX, endY, endZ;

            switch (rotation) {
                case 90:
                     startX = startX + 1;
                    endX = startX - size.getZ();
                    endY = startY + size.getY();
                    endZ = startZ + size.getX();
                    break;
                case 180:
                    startX = startX + 1;
                    startZ = startZ + 1;
                    endX = startX - size.getX();
                    endY = startY + size.getY();
                    endZ = startZ - size.getZ();
                    break;
                case 270:
                    startZ = startZ + 1;
                    endX = startX + size.getZ();
                    endY = startY + size.getY();
                    endZ = startZ - size.getX();
                    break;
                default:
                    endX = startX + size.getX();
                    endY = startY + size.getY();
                    endZ = startZ + size.getZ();
                    break;
            }

            int minX = Math.min(startX, endX);
            int maxX = Math.max(startX, endX);
            int minY = Math.min(startY, endY);
            int maxY = Math.max(startY, endY);
            int minZ = Math.min(startZ, endZ);
            int maxZ = Math.max(startZ, endZ);

            GlStateManager.pushMatrix();
            GlStateManager.translate(offsetX, offsetY, offsetZ);
            GlStateManager.disableTexture2D();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();

            float alphaValue = 0.4f + (float)(Math.sin(System.currentTimeMillis() * 0.005) * 0.2f);
            renderOutline(minX, minY, minZ, maxX, maxY, maxZ, 0.0f, 0.5f, 1.0f, alphaValue);

            GlStateManager.enableDepth();
            GlStateManager.enableLighting();
            GlStateManager.disableBlend();
            GlStateManager.enableTexture2D();
            GlStateManager.popMatrix();
        }
    }

    private void renderOutline(double x1, double y1, double z1, double x2, double y2, double z2,
                               float r, float g, float b, float a) {
        GlStateManager.glLineWidth(3.0f);
        GlStateManager.color(r, g, b, a);

        GL11.glBegin(GL11.GL_LINE_STRIP);
        GL11.glVertex3d(x1, y1, z1); GL11.glVertex3d(x2, y1, z1);
        GL11.glVertex3d(x2, y1, z2); GL11.glVertex3d(x1, y1, z2);
        GL11.glVertex3d(x1, y1, z1);
        GL11.glEnd();

        GL11.glBegin(GL11.GL_LINE_STRIP);
        GL11.glVertex3d(x1, y2, z1); GL11.glVertex3d(x2, y2, z1);
        GL11.glVertex3d(x2, y2, z2); GL11.glVertex3d(x1, y2, z2);
        GL11.glVertex3d(x1, y2, z1);
        GL11.glEnd();

        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex3d(x1, y1, z1); GL11.glVertex3d(x1, y2, z1);
        GL11.glVertex3d(x2, y1, z1); GL11.glVertex3d(x2, y2, z1);
        GL11.glVertex3d(x2, y1, z2); GL11.glVertex3d(x2, y2, z2);
        GL11.glVertex3d(x1, y1, z2); GL11.glVertex3d(x1, y2, z2);
        GL11.glEnd();
    }
}