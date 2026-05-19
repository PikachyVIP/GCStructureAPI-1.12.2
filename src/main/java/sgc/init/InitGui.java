package sgc.init;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import sgc.blocks.structureblock.StructureBlockTile;
import sgc.blocks.structureblock.gui.StructureBlockContainer;
import sgc.blocks.structureblock.gui.StructureBlockGui;

import javax.annotation.Nullable;

public class InitGui implements IGuiHandler {
    public static final int GUI_MTS = 0;

    @Nullable
    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));

        if (ID == GUI_MTS && tile instanceof StructureBlockTile) {
            return new StructureBlockContainer(player.inventory, (StructureBlockTile) tile);
        }

        return null;
    }

    @Nullable
    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if(world instanceof WorldClient) {
            TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
            if (ID == GUI_MTS && tile instanceof StructureBlockTile) {
                return new StructureBlockGui(player.inventory, (StructureBlockTile) tile);
            }
        }
        return null;
    }
}