package sgc.supporting;


import net.minecraft.block.*;
import net.minecraft.item.*;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.*;
import net.minecraftforge.fml.common.Mod.*;
import net.minecraftforge.fml.common.eventhandler.*;
import sgc.init.BlocksInit;
import sgc.init.ItemsInit;

@EventBusSubscriber
public class RegistryHandler {
	@SubscribeEvent
	public static void onItemRegister(RegistryEvent.Register<Item> event) { event.getRegistry().registerAll(ItemsInit.ITEMS.toArray(new Item[0])); }
	@SubscribeEvent
	public static void onBlockRegister(RegistryEvent.Register<Block> event) {
		event.getRegistry().registerAll(BlocksInit.BLOCKS.toArray(new Block[0]));
		BlocksInit.tileReg();
	}
	
	@SubscribeEvent
	public static void onModelRegister(ModelRegistryEvent event) {
		for(Item item : ItemsInit.ITEMS) if(item instanceof IHasModel) ((IHasModel)item).registerModels();
		for(Block block : BlocksInit.BLOCKS) if(block instanceof IHasModel) ((IHasModel)block).registerModels();
	}
}
