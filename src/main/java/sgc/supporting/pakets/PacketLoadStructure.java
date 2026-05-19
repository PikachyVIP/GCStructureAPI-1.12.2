package sgc.supporting.pakets;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import sgc.blocks.structureblock.StructureBlockTile;

public class PacketLoadStructure implements IMessage {
    private BlockPos pos;
    private String fileName;
    private int rotation;
    private boolean showPreview;

    public PacketLoadStructure() {}

    public PacketLoadStructure(BlockPos pos, String fileName, int rotation, boolean showPreview) {
        this.pos = pos;
        this.fileName = fileName;
        this.rotation = rotation;
        this.showPreview = showPreview;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = BlockPos.fromLong(buf.readLong());
        fileName = ByteBufUtils.readUTF8String(buf);
        rotation = buf.readInt();
        showPreview = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(pos.toLong());
        ByteBufUtils.writeUTF8String(buf, fileName);
        buf.writeInt(rotation);
        buf.writeBoolean(showPreview);
    }

    public static class Handler implements IMessageHandler<PacketLoadStructure, IMessage> {
        @Override
        public IMessage onMessage(PacketLoadStructure message, MessageContext ctx) {
            ctx.getServerHandler().player.getServerWorld().addScheduledTask(() -> {
                World world = ctx.getServerHandler().player.world;
                StructureBlockTile te = (StructureBlockTile) world.getTileEntity(message.pos);
                if (te != null) {
                    te.loadStructure(message.fileName, message.rotation, message.showPreview);
                }
            });
            return null;
        }
    }
}