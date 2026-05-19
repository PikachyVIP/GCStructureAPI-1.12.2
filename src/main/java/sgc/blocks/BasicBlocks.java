package sgc.blocks;


import net.minecraft.block.*;
import net.minecraft.block.material.*;
import net.minecraft.item.*;
import sgc.Main;
import sgc.init.BlocksInit;
import sgc.init.ItemsInit;
import sgc.supporting.IHasModel;

public class BasicBlocks extends Block implements IHasModel {
	public BasicBlocks(final String name, final Material material, float hardness, float resistanse, String hravLevel, int level, SoundType soundtype) {
		super(material);
		this.setRegistryName(name);
		this.setUnlocalizedName(name);
		this.setSoundType(soundtype);
		this.setHardness(hardness);
		this.setResistance(resistanse);
		this.setHarvestLevel(hravLevel, level);
		setCreativeTab(Main.CT_MOD);
		
		BlocksInit.BLOCKS.add(this);
		ItemsInit.ITEMS.add(new ItemBlock(this).setRegistryName(this.getRegistryName()));
	}
	@Override public void registerModels() { Main.proxy.registerItemRenderer(Item.getItemFromBlock(this), 0, "inventory"); }
}
