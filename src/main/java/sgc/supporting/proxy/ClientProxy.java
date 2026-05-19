package sgc.supporting.proxy;

import net.minecraft.client.renderer.block.model.*;
import net.minecraft.item.*;
import net.minecraftforge.client.model.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.*;
//import sgc.blocks.structureblock.StructureBlockRenderer;
import sgc.blocks.structureblock.StructureBlockTile;
import sgc.blocks.structureblock.StructureBlockWorldRenderer;

public class ClientProxy extends CommonProxy {
	public void preInit(FMLPreInitializationEvent e) {
		super.preInit(e);
		//ClientRegistry.bindTileEntitySpecialRenderer(StructureBlockTile.class, new StructureBlockRenderer());
		MinecraftForge.EVENT_BUS.register(new StructureBlockWorldRenderer());
	}
	public void init(FMLInitializationEvent e) {super.init(e);}
	public void postInit(FMLPostInitializationEvent e) {super.postInit(e);}

	public void registerItemRenderer(Item item, int meta, String id) { ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(item.getRegistryName(), id)); }
}
