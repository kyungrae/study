package hama;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

public class ConnectExample {

    public static void connect() {
        Channel channel = new NioSocketChannel();
        ChannelFuture future = channel.connect(new InetSocketAddress("192.168.0.1", 25));
        future.addListener((ChannelFutureListener) channelFuture -> {
            if (future.isSuccess()) {
                ByteBuf buffer = Unpooled.copiedBuffer("Hello", Charset.defaultCharset());
            } else {
                Throwable cause = future.cause();
                cause.printStackTrace();
            }
        });
    }
}
