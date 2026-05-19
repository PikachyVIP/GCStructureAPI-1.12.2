package sgc.blocks;

import net.minecraft.block.state.*;
import net.minecraft.nbt.*;
import net.minecraft.network.*;
import net.minecraft.network.play.server.*;
import net.minecraft.server.management.*;
import net.minecraft.tileentity.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;

import javax.annotation.*;

public class BasicTile extends TileEntity {
    @Override public boolean shouldRefresh(World w, BlockPos p, @Nonnull IBlockState oldState, @Nonnull IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }

    @Nonnull @Override public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        NBTTagCompound ret = super.writeToNBT(nbt);
        writePacketNBT(ret);
        return ret;
    }

    @Nonnull @Override public final NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        readPacketNBT(nbt);
    }

    public void writePacketNBT(NBTTagCompound nbt) {}
    public void readPacketNBT(NBTTagCompound nbt) {}

    @Override public final SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound tag = new NBTTagCompound();
        writePacketNBT(tag);
        return new SPacketUpdateTileEntity(pos, 1, tag);
    }

    @Override public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        super.onDataPacket(net, packet);
        readPacketNBT(packet.getNbtCompound());
    }

    public static void dispatchTEToNearbyPlayers(World world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        if(tile != null) dispatchTEToNearbyPlayers(tile);
    }
    public static void dispatchTEToNearbyPlayers(TileEntity tile) {
        SPacketUpdateTileEntity packet = tile.getUpdatePacket();
        if(packet != null && tile.getWorld() instanceof WorldServer) {
            PlayerChunkMapEntry chunk = ((WorldServer) tile.getWorld()).getPlayerChunkMap().getEntry(tile.getPos().getX() >> 4, tile.getPos().getZ() >> 4);
            if(chunk != null) chunk.sendPacket(packet);
        }
    }
}
