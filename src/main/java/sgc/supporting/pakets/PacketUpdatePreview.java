package sgc.supporting.pakets;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import sgc.blocks.structureblock.StructureBlockTile;

public class PacketUpdatePreview implements IMessage {
    private BlockPos pos;
    private boolean showPreview;

    public PacketUpdatePreview() {}

    public PacketUpdatePreview(BlockPos pos, boolean showPreview) {
        this.pos = pos;
        this.showPreview = showPreview;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = BlockPos.fromLong(buf.readLong());
        showPreview = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(pos.toLong());
        buf.writeBoolean(showPreview);
    }

    public static class Handler implements IMessageHandler<PacketUpdatePreview, IMessage> {
        @Override
        public IMessage onMessage(PacketUpdatePreview message, MessageContext ctx) {
            ctx.getServerHandler().player.getServerWorld().addScheduledTask(() -> {
                World world = ctx.getServerHandler().player.world;
                StructureBlockTile te = (StructureBlockTile) world.getTileEntity(message.pos);
                if (te != null) {
                    te.setShowLoadPreview(message.showPreview);
                }
            });
            return null;
        }
    }
}