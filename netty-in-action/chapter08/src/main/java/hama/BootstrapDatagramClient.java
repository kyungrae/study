package hama;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.oio.OioDatagramChannel;

import java.net.InetSocketAddress;

public class BootstrapDatagramClient {
    public void bootstrap() {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(new OioEventLoopGroup())
                .channel(OioDatagramChannel.class)
                .handler(new SimpleChannelInboundHandler<DatagramPacket>() {
                    @Override
                    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) {

                    }
                });
        ChannelFuture future = bootstrap.bind(new InetSocketAddress(0));
        future.addListener((ChannelFutureListener) future1 -> {
            if (future1.isSuccess())
                System.out.println("Channel bound");
            else {
                System.err.println("Bind attempt failed");
                future1.cause().printStackTrace();
            }
        });
    }
}
