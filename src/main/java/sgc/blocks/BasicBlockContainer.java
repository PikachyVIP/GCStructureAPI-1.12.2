package sgc.blocks;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.world.World;
import sgc.Main;
import sgc.init.BlocksInit;
import sgc.init.ItemsInit;
import sgc.supporting.IHasModel;

import javax.annotation.Nullable;

public class BasicBlockContainer extends BlockContainer implements IHasModel {
    public BasicBlockContainer(final String name, final Material material, float hardness, float resistanse, String hravLevel, int level, SoundType soundtype) {
        super(material);
        setRegistryName(name);
        setUnlocalizedName(name);
        setSoundType(soundtype);
        setHardness(hardness);
        setResistance(resistanse);
        setHarvestLevel(hravLevel, level);
        setCreativeTab(Main.CT_MOD);

        BlocksInit.BLOCKS.add(this);
        ItemsInit.ITEMS.add(new ItemBlock(this).setRegistryName(this.getRegistryName()));
    }

    @Override
    public void registerModels() {
        Main.proxy.registerItemRenderer(Item.getItemFromBlock(this), 0, "inventory");
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return null;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return false; // Измените на false по умолчанию
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return null;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }
}