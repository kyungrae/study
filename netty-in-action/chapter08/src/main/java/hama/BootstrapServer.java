package hama;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

public class BootstrapServer {
    public void bootstrap() {
        EventLoopGroup group = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(group)
                .channel(NioServerSocketChannel.class)
                .handler(new SimpleChannelInboundHandler<ByteBuf>() {
                    @Override
                    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
                        System.out.println("Received data");
                    }
                });

        ChannelFuture future = bootstrap.bind(new InetSocketAddress(8080));
        future.addListener((ChannelFutureListener) channelFuture -> {
            if(channelFuture.isSuccess())
                System.out.println("Server bound");
            else {
                System.err.println("Bound attempt failed");
                channelFuture.cause().printStackTrace();
            }
        });
    }
}
