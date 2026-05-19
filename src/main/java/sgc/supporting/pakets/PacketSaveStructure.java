package sgc.supporting.pakets;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import sgc.blocks.structureblock.StructureBlockTile;

public class PacketSaveStructure implements IMessage {
    private BlockPos pos;
    private String name;

    public PacketSaveStructure() {}

    public PacketSaveStructure(BlockPos pos, String name) {
        this.pos = pos;
        this.name = name;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        name = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());
        ByteBufUtils.writeUTF8String(buf, name);
    }

    public static class Handler implements IMessageHandler<PacketSaveStructure, IMessage> {
        @Override
        public IMessage onMessage(PacketSaveStructure msg, MessageContext ctx) {
            ctx.getServerHandler().player.getServerWorld().addScheduledTask(() -> {
                StructureBlockTile te = (StructureBlockTile) ctx.getServerHandler().player.world.getTileEntity(msg.pos);
                if (te != null) {
                    te.setStructureName(msg.name);
                    te.saveStructure();
                }
            });
            return null;
        }
    }
}