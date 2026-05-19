package sgc.blocks.structureblock;

import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.gen.structure.template.TemplateManager;
import sgc.api.gui.AbstractAreaTile;

public class StructureBlockTile extends AbstractAreaTile {
    public StructureBlockTile(){
        setPreviewColor(255, 255, 255);
        setPreviewAlpha(0.6f);
    }

    private String structureName = "";
    private boolean ignoreAir = false;
    private BlockPos loadPosition = BlockPos.ORIGIN;
    private boolean showLoadPreview = true;
    private int rotation = 0;

    private static final String NAME = "structure_name";
    private static final String IGNORE_AIR = "ignoreAir";
    private static final String LOAD_POS_X = "loadPosX";
    private static final String LOAD_POS_Y = "loadPosY";
    private static final String LOAD_POS_Z = "loadPosZ";
    private static final String SHOW_LOAD_PREVIEW = "showLoadPreview";
    private static final String ROTATION = "rotation";

    @Override
    public void writePacketNBT(NBTTagCompound nbt) {
        super.writePacketNBT(nbt);
        nbt.setString(NAME, structureName);
        nbt.setBoolean(IGNORE_AIR, ignoreAir);
        nbt.setInteger(LOAD_POS_X, loadPosition.getX());
        nbt.setInteger(LOAD_POS_Y, loadPosition.getY());
        nbt.setInteger(LOAD_POS_Z, loadPosition.getZ());
        nbt.setBoolean(SHOW_LOAD_PREVIEW, showLoadPreview);
        nbt.setInteger(ROTATION, rotation);
    }

    @Override
    public void readPacketNBT(NBTTagCompound nbt) {
        super.readPacketNBT(nbt);
        structureName = nbt.getString(NAME);
        ignoreAir = nbt.getBoolean(IGNORE_AIR);

        if (nbt.hasKey(LOAD_POS_X)) {
            loadPosition = new BlockPos(nbt.getInteger(LOAD_POS_X), nbt.getInteger(LOAD_POS_Y), nbt.getInteger(LOAD_POS_Z));
        }
        if (nbt.hasKey(SHOW_LOAD_PREVIEW)) {
            showLoadPreview = nbt.getBoolean(SHOW_LOAD_PREVIEW);
        }
        if (nbt.hasKey(ROTATION)) {
            rotation = nbt.getInteger(ROTATION);
        }

        if (world != null && world.isRemote) {
            world.markBlockRangeForRenderUpdate(pos, pos);
        }
    }

    // Getters and Setters
    public String getStructureName() { return structureName; }
    public void setStructureName(String name) {
        this.structureName = name;
        markDirty();
        syncToClient();
    }

    public boolean getIgnoreAir() { return ignoreAir; }
    public void setIgnoreAir(boolean ignoreAir) {
        this.ignoreAir = ignoreAir;
        markDirty();
        syncToClient();
    }

    public BlockPos getLoadPosition() { return loadPosition; }
    public void setLoadPosition(BlockPos loadPos) {
        this.loadPosition = loadPos;
        markDirty();
        syncToClient();
    }

    public boolean getShowLoadPreview() { return showLoadPreview; }
    public void setShowLoadPreview(boolean showPreview) {
        this.showLoadPreview = showPreview;
        markDirty();
        syncToClient();
    }

    public int getRotation() { return rotation; }
    public void setRotation(int rotation) {
        this.rotation = rotation;
        markDirty();
        syncToClient();
    }

    public boolean saveStructure() {
        if (world.isRemote || structureName.isEmpty()) return false;
        if (!hasValidRegion()) return false;

        BlockPos start = getRegionStart();
        BlockPos size = getRegionSize();

        WorldServer worldserver = (WorldServer) world;
        TemplateManager manager = worldserver.getStructureTemplateManager();
        Template template = manager.getTemplate(world.getMinecraftServer(), new ResourceLocation(structureName));

        if (ignoreAir) {
            template.takeBlocksFromWorld(world, start, size, true, Blocks.AIR);
        } else {
            template.takeBlocksFromWorld(world, start, size, true, null);
        }
        template.setAuthor("StructureBlock");

        boolean saved = manager.writeTemplate(world.getMinecraftServer(), new ResourceLocation(structureName));
        if (saved) System.out.println("Structure saved: " + structureName);
        return saved;
    }

    public void readNbtFile(String fileName) {
        if (world.isRemote || fileName.isEmpty()) return;

        WorldServer worldserver = (WorldServer) world;
        TemplateManager manager = worldserver.getStructureTemplateManager();

        try {
            ResourceLocation location = new ResourceLocation(fileName);
            Template template = manager.getTemplate(world.getMinecraftServer(), location);

            if (template != null) {
                BlockPos size = template.getSize();
                BlockPos defaultPos = pos.up();
                setCorners(defaultPos, defaultPos.add(size.getX() - 1, size.getY() - 1, size.getZ() - 1));
                this.structureName = fileName;
                this.loadPosition = defaultPos;
                this.rotation = 0;
                syncToClient();
                System.out.println("Read .nbt file: " + fileName + ", size: " + size);
            } else {
                System.out.println("Template not found: " + fileName);
            }
        } catch (Exception e) {
            System.out.println("Error reading .nbt file: " + fileName);
            e.printStackTrace();
        }
    }

    public boolean loadStructure(String fileName, int rotationDegrees, boolean showPreview) {
        if (world.isRemote || fileName.isEmpty()) return false;

        WorldServer worldserver = (WorldServer) world;
        TemplateManager manager = worldserver.getStructureTemplateManager();
        ResourceLocation location = new ResourceLocation(fileName);
        Template template = manager.getTemplate(world.getMinecraftServer(), location);

        if (template != null) {
            BlockPos loadPos = loadPosition.equals(BlockPos.ORIGIN) ? pos.up() : loadPosition;

            PlacementSettings placement = new PlacementSettings();
            switch (rotationDegrees) {
                case 90: placement.setRotation(net.minecraft.util.Rotation.CLOCKWISE_90); break;
                case 180: placement.setRotation(net.minecraft.util.Rotation.CLOCKWISE_180); break;
                case 270: placement.setRotation(net.minecraft.util.Rotation.COUNTERCLOCKWISE_90); break;
                default: placement.setRotation(net.minecraft.util.Rotation.NONE);
            }
            placement.setMirror(net.minecraft.util.Mirror.NONE);
            placement.setIgnoreEntities(false);

            template.addBlocksToWorld(world, loadPos, placement);
            System.out.println("Structure loaded: " + fileName + " at " + loadPos + " with rotation " + rotationDegrees);
            return true;
        } else {
            System.out.println("Failed to load structure: " + fileName + " - template not found");
        }
        return false;
    }


}