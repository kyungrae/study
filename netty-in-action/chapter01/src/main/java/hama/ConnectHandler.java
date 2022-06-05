package hama;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;

abstract class ConnectHandler implements ChannelInboundHandler {
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("Client " + ctx.channel().remoteAddress() + " connected");
    }
}
