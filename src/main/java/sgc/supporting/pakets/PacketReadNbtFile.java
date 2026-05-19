package sgc.supporting.pakets;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import sgc.blocks.structureblock.StructureBlockTile;

public class PacketReadNbtFile implements IMessage {
    private BlockPos pos;
    private String fileName;

    public PacketReadNbtFile() {}

    public PacketReadNbtFile(BlockPos pos, String fileName) {
        this.pos = pos;
        this.fileName = fileName;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = BlockPos.fromLong(buf.readLong());
        fileName = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(pos.toLong());
        ByteBufUtils.writeUTF8String(buf, fileName);
    }

    public static class Handler implements IMessageHandler<PacketReadNbtFile, IMessage> {
        @Override
        public IMessage onMessage(PacketReadNbtFile message, MessageContext ctx) {
            ctx.getServerHandler().player.getServerWorld().addScheduledTask(() -> {
                World world = ctx.getServerHandler().player.world;
                StructureBlockTile te = (StructureBlockTile) world.getTileEntity(message.pos);
                if (te != null) {
                    te.readNbtFile(message.fileName);
                    // Mark dirty and sync to client
                    te.markDirty();
                    world.notifyBlockUpdate(message.pos, world.getBlockState(message.pos), world.getBlockState(message.pos), 3);
                }
            });
            return null;
        }
    }
}