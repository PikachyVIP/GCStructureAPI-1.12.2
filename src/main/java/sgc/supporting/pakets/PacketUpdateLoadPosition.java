package sgc.supporting.pakets;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import sgc.blocks.structureblock.StructureBlockTile;

public class PacketUpdateLoadPosition implements IMessage {
    private BlockPos pos;
    private BlockPos loadPos;

    public PacketUpdateLoadPosition() {}

    public PacketUpdateLoadPosition(BlockPos pos, BlockPos loadPos) {
        this.pos = pos;
        this.loadPos = loadPos;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = BlockPos.fromLong(buf.readLong());
        loadPos = BlockPos.fromLong(buf.readLong());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(pos.toLong());
        buf.writeLong(loadPos.toLong());
    }

    public static class Handler implements IMessageHandler<PacketUpdateLoadPosition, IMessage> {
        @Override
        public IMessage onMessage(PacketUpdateLoadPosition message, MessageContext ctx) {
            ctx.getServerHandler().player.getServerWorld().addScheduledTask(() -> {
                World world = ctx.getServerHandler().player.world;
                StructureBlockTile te = (StructureBlockTile) world.getTileEntity(message.pos);
                if (te != null) {
                    te.setLoadPosition(message.loadPos);
                }
            });
            return null;
        }
    }
}