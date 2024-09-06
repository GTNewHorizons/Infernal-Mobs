package atomicstryker.infernalmobs.common.network.packets;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.network.NetworkHelper.IPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class AirPacket implements IPacket {

    private int air;

    public AirPacket() {}

    public AirPacket(int a) {
        air = a;
    }

    @Override
    public void writeBytes(ChannelHandlerContext ctx, ByteBuf bytes) {
        bytes.writeInt(air);
    }

    @Override
    public void readBytes(ChannelHandlerContext ctx, ByteBuf bytes) {
        air = bytes.readInt();
        InfernalMobsCore.proxy.onAirPacket(air);
    }

}
