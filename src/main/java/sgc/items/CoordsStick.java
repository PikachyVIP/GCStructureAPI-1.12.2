package sgc.items;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import sgc.api.gui.SelectionTool;
import sgc.blocks.structureblock.StructureBlockTile;

public class CoordsStick extends SelectionTool {
    public CoordsStick(String name) {
        super(name, 1);
        setMaxStackSize(1);
    }
}