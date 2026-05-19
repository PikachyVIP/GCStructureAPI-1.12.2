package sgc.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import sgc.blocks.structureblock.StructureBlockTile;
import sgc.items.BasicItems;
import sgc.items.CoordsStick;

public class SelectionTool extends BasicItems {

    private static final String X1 = "x1", Y1 = "y1", Z1 = "z1";
    private static final String X2 = "x2", Y2 = "y2", Z2 = "z2";

    public SelectionTool(String name, int maxStack) {
        super(name, maxStack);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        if (stack.isEmpty() || !(stack.getItem() instanceof CoordsStick)) return EnumActionResult.PASS;

        if (world.isRemote) return EnumActionResult.SUCCESS;

        if (player.isSneaking() && world.getTileEntity(pos) instanceof StructureBlockTile) {
            NBTTagCompound nbt = stack.getTagCompound();
            if (nbt == null || !nbt.hasKey(X1) || !nbt.hasKey(X2)) {
                player.sendMessage(new TextComponentString("§cСначала задайте оба угла!"));
                return EnumActionResult.FAIL;
            }
            BlockPos c1 = new BlockPos(nbt.getInteger(X1), nbt.getInteger(Y1), nbt.getInteger(Z1));
            BlockPos c2 = new BlockPos(nbt.getInteger(X2), nbt.getInteger(Y2), nbt.getInteger(Z2));
            StructureBlockTile te = (StructureBlockTile) world.getTileEntity(pos);
            te.setCorners(c1, c2);
            player.sendMessage(new TextComponentString("§aКоординаты переданы блоку!"));
            return EnumActionResult.SUCCESS;
        }

        BlockPos target = getTargetPos(player, world);
        if (target == null) return EnumActionResult.PASS;

        if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
        NBTTagCompound nbt = stack.getTagCompound();

        if (player.isSneaking()) {
            nbt.setInteger(X2, target.getX());
            nbt.setInteger(Y2, target.getY());
            nbt.setInteger(Z2, target.getZ());
            player.sendMessage(new TextComponentString("§eУгол 2: " + target.getX() + ", " + target.getY() + ", " + target.getZ()));
        } else {
            nbt.setInteger(X1, target.getX());
            nbt.setInteger(Y1, target.getY());
            nbt.setInteger(Z1, target.getZ());
            player.sendMessage(new TextComponentString("§eУгол 1: " + target.getX() + ", " + target.getY() + ", " + target.getZ()));
        }

        return EnumActionResult.SUCCESS;
    }

    private BlockPos getTargetPos(EntityPlayer player, World world) {
        double reach = 5.0D;
        Vec3d start = player.getPositionEyes(1.0F);
        Vec3d look = player.getLook(1.0F);
        Vec3d end = start.addVector(look.x * reach, look.y * reach, look.z * reach);
        RayTraceResult result = world.rayTraceBlocks(start, end, false, true, false);
        if (result != null && result.typeOfHit == RayTraceResult.Type.BLOCK) {
            return result.getBlockPos();
        }
        return null;
    }
}