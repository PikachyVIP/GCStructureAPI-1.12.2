package sgc.supporting.pakets;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import sgc.blocks.structureblock.StructureBlockTile;

public class PacketToggleIgnoreAir implements IMessage {
    private BlockPos pos;
    private boolean ignoreAir;

    public PacketToggleIgnoreAir() {}

    public PacketToggleIgnoreAir(BlockPos pos, boolean ignoreAir) {
        this.pos = pos;
        this.ignoreAir = ignoreAir;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        ignoreAir = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());
        buf.writeBoolean(ignoreAir);
    }

    public static class Handler implements IMessageHandler<PacketToggleIgnoreAir, IMessage> {
        @Override
        public IMessage onMessage(PacketToggleIgnoreAir msg, MessageContext ctx) {
            ctx.getServerHandler().player.getServerWorld().addScheduledTask(() -> {
                StructureBlockTile te = (StructureBlockTile) ctx.getServerHandler().player.world.getTileEntity(msg.pos);
                if (te != null) {
                    te.setIgnoreAir(msg.ignoreAir);
                }
            });
            return null;
        }
    }
}