package sgc.supporting.proxy;

import net.minecraft.item.*;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import sgc.Main;
import sgc.init.InitGui;

public class CommonProxy {
	public void preInit(FMLPreInitializationEvent e) {}
	public void init(FMLInitializationEvent e) {
		NetworkRegistry.INSTANCE.registerGuiHandler(Main.instance, new InitGui());
	}
	public void postInit(FMLPostInitializationEvent e) {}
	
	public void registerItemRenderer(Item item, int meta, String id) {}
} 