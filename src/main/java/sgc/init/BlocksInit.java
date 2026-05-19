package sgc.init;

import java.util.*;


import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import sgc.Main;
import sgc.blocks.BasicBlocks;
import sgc.blocks.structureblock.StructureBlock;
import sgc.blocks.structureblock.StructureBlockTile;

public class BlocksInit {
	public static final List<Block> BLOCKS = new ArrayList<Block>();
	
	public static final Block STRUCTURE_BLOCK = new StructureBlock("structure_block", Material.BARRIER, -0.1f, -0.1f, "", 1, SoundType.METAL);



	public static void tileReg() {
		registerTile(StructureBlockTile.class, STRUCTURE_BLOCK);
	}

	private static void registerTile(Class<? extends TileEntity> clazz, Block key) { GameRegistry.registerTileEntity(clazz, Main.MODID + ":" + key.getUnlocalizedName().toString()); }

}
