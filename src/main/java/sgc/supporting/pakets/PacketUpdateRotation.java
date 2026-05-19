package sgc.supporting.pakets;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import sgc.blocks.structureblock.StructureBlockTile;

public class PacketUpdateRotation implements IMessage {
    private BlockPos pos;
    private int rotation;

    public PacketUpdateRotation() {}

    public PacketUpdateRotation(BlockPos pos, int rotation) {
        this.pos = pos;
        this.rotation = rotation;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = BlockPos.fromLong(buf.readLong());
        rotation = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(pos.toLong());
        buf.writeInt(rotation);
    }

    public static class Handler implements IMessageHandler<PacketUpdateRotation, IMessage> {
        @Override
        public IMessage onMessage(PacketUpdateRotation message, MessageContext ctx) {
            ctx.getServerHandler().player.getServerWorld().addScheduledTask(() -> {
                World world = ctx.getServerHandler().player.world;
                StructureBlockTile te = (StructureBlockTile) world.getTileEntity(message.pos);
                if (te != null) {
                    te.setRotation(message.rotation);
                }
            });
            return null;
        }
    }
}