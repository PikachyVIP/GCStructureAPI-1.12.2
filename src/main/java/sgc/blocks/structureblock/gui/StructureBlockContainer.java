package sgc.blocks.structureblock.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import sgc.blocks.structureblock.StructureBlockTile;

public class StructureBlockContainer extends Container {
    private final StructureBlockTile te;
    public StructureBlockContainer(IInventory pInv, StructureBlockTile tile) { this.te = tile; }
    @Override public boolean canInteractWith(EntityPlayer player) { return true; }
}
