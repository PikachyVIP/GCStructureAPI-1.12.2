package sgc.items;


import net.minecraft.item.*;
import sgc.Main;
import sgc.init.ItemsInit;
import sgc.supporting.IHasModel;

public class BasicItems extends Item implements IHasModel {
	public BasicItems(String name, int maxStack) {
		setUnlocalizedName(name);
		setRegistryName(name);
		setMaxStackSize(maxStack);
		setCreativeTab(Main.CT_MOD);
		
		ItemsInit.ITEMS.add(this);
	}
	@Override public void registerModels() { Main.proxy.registerItemRenderer(this, 0, "inventory"); }
}
