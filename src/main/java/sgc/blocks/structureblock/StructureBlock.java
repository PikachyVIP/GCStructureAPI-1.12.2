package sgc.blocks.structureblock;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import sgc.Main;
import sgc.blocks.BasicBlockContainer;
import sgc.init.InitGui;

import javax.annotation.Nullable;

public class StructureBlock extends BasicBlockContainer {
    public StructureBlock(String name, Material material, float hardness, float resistanse, String hravLevel, int level, SoundType soundtype) {
        super(name, material, hardness, resistanse, hravLevel, level, soundtype);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        TileEntity te = world.getTileEntity(pos);
        if (!world.isRemote && te instanceof StructureBlockTile) {
            player.openGui(Main.instance, InitGui.GUI_MTS, world, pos.getX(), pos.getY(), pos.getZ());
        }

        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new StructureBlockTile();
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new StructureBlockTile();
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }
}