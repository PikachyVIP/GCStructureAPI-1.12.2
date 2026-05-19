package sgc.supporting.pakets;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import sgc.blocks.structureblock.StructureBlockTile;

public class PacketUpdateCorners implements IMessage {
    private BlockPos tilePos;
    private BlockPos corner1;
    private BlockPos corner2;

    public PacketUpdateCorners() {}

    public PacketUpdateCorners(BlockPos tilePos, BlockPos corner1, BlockPos corner2) {
        this.tilePos = tilePos;
        this.corner1 = corner1;
        this.corner2 = corner2;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        tilePos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        corner1 = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        corner2 = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(tilePos.getX());
        buf.writeInt(tilePos.getY());
        buf.writeInt(tilePos.getZ());
        buf.writeInt(corner1.getX());
        buf.writeInt(corner1.getY());
        buf.writeInt(corner1.getZ());
        buf.writeInt(corner2.getX());
        buf.writeInt(corner2.getY());
        buf.writeInt(corner2.getZ());
    }

    public static class Handler implements IMessageHandler<PacketUpdateCorners, IMessage> {
        @Override
        public IMessage onMessage(PacketUpdateCorners message, MessageContext ctx) {
            ctx.getServerHandler().player.getServerWorld().addScheduledTask(() -> {
                StructureBlockTile te = (StructureBlockTile) ctx.getServerHandler().player.world.getTileEntity(message.tilePos);
                if (te != null) {
                    te.setCorners(message.corner1, message.corner2);
                    // не трогаем ignoreAir и name
                }
            });
            return null;
        }
    }
}