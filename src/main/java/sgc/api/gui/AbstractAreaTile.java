package sgc.api.gui;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import sgc.blocks.BasicTile;

public abstract class AbstractAreaTile extends BasicTile {

    protected BlockPos corner1 = BlockPos.ORIGIN;
    protected BlockPos corner2 = BlockPos.ORIGIN;
    protected boolean showAreaPreview = true;
    protected int previewColorR = 0;
    protected int previewColorG = 255;
    protected int previewColorB = 0;
    protected float previewAlpha = 0.5f;

    private static final String C1X = "c1x", C1Y = "c1y", C1Z = "c1z";
    private static final String C2X = "c2x", C2Y = "c2y", C2Z = "c2z";
    private static final String SHOW_PREVIEW = "showPreview";
    private static final String COLOR_R = "colorR";
    private static final String COLOR_G = "colorG";
    private static final String COLOR_B = "colorB";
    private static final String ALPHA = "alpha";

    @Override
    public void writePacketNBT(NBTTagCompound nbt) {
        nbt.setInteger(C1X, corner1.getX());
        nbt.setInteger(C1Y, corner1.getY());
        nbt.setInteger(C1Z, corner1.getZ());
        nbt.setInteger(C2X, corner2.getX());
        nbt.setInteger(C2Y, corner2.getY());
        nbt.setInteger(C2Z, corner2.getZ());
        nbt.setBoolean(SHOW_PREVIEW, showAreaPreview);
        nbt.setInteger(COLOR_R, previewColorR);
        nbt.setInteger(COLOR_G, previewColorG);
        nbt.setInteger(COLOR_B, previewColorB);
        nbt.setFloat(ALPHA, previewAlpha);
    }

    @Override
    public void readPacketNBT(NBTTagCompound nbt) {
        corner1 = new BlockPos(nbt.getInteger(C1X), nbt.getInteger(C1Y), nbt.getInteger(C1Z));
        corner2 = new BlockPos(nbt.getInteger(C2X), nbt.getInteger(C2Y), nbt.getInteger(C2Z));
        showAreaPreview = nbt.getBoolean(SHOW_PREVIEW);
        previewColorR = nbt.getInteger(COLOR_R);
        previewColorG = nbt.getInteger(COLOR_G);
        previewColorB = nbt.getInteger(COLOR_B);
        previewAlpha = nbt.getFloat(ALPHA);

        if (world != null && world.isRemote) {
            world.markBlockRangeForRenderUpdate(pos, pos);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        writePacketNBT(nbt);
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        readPacketNBT(nbt);
    }

    // Getters and Setters
    public BlockPos getCorner1() { return corner1; }
    public BlockPos getCorner2() { return corner2; }
    public boolean getShowAreaPreview() { return showAreaPreview; }
    public int getPreviewColorR() { return previewColorR; }
    public int getPreviewColorG() { return previewColorG; }
    public int getPreviewColorB() { return previewColorB; }
    public float getPreviewAlpha() { return previewAlpha; }

    public void setCorners(BlockPos pos1, BlockPos pos2) {
        this.corner1 = pos1;
        this.corner2 = pos2;
        markDirty();
        syncToClient();
    }

    public void setShowAreaPreview(boolean show) {
        this.showAreaPreview = show;
        markDirty();
        syncToClient();
    }


    public void setPreviewColor(int r, int g, int b) {
        this.previewColorR = r;
        this.previewColorG = g;
        this.previewColorB = b;
        markDirty();
        syncToClient();
    }

    public void setPreviewAlpha(float alpha) {
        this.previewAlpha = alpha;
        markDirty();
        syncToClient();
    }

    public BlockPos getRegionStart() {
        return new BlockPos(
                Math.min(corner1.getX(), corner2.getX()),
                Math.min(corner1.getY(), corner2.getY()),
                Math.min(corner1.getZ(), corner2.getZ())
        );
    }

    public BlockPos getRegionSize() {
        return new BlockPos(
                Math.abs(corner1.getX() - corner2.getX()) + 1,
                Math.abs(corner1.getY() - corner2.getY()) + 1,
                Math.abs(corner1.getZ() - corner2.getZ()) + 1
        );
    }

    public BlockPos getRegionEnd() {
        BlockPos start = getRegionStart();
        BlockPos size = getRegionSize();
        return new BlockPos(
                start.getX() + size.getX(),
                start.getY() + size.getY(),
                start.getZ() + size.getZ()
        );
    }

    public boolean hasValidRegion() {
        return !corner1.equals(BlockPos.ORIGIN) && !corner2.equals(BlockPos.ORIGIN);
    }

    protected void syncToClient() {
        if (world != null && !world.isRemote) {
            IBlockState state = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, state, state, 3);
            markDirty();
        }
    }

    @Override
    public double getMaxRenderDistanceSquared() {
        return Double.MAX_VALUE;
    }
}