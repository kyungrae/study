package hama;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.util.CharsetUtil;

public class WriteHandlers {

    public void writeViaChannel(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        channel.write(Unpooled.copiedBuffer("Netty in Action", CharsetUtil.UTF_8));
    }

    public void writeViaChannelPipeline(ChannelHandlerContext ctx) {
        ChannelPipeline pipeline = ctx.pipeline();
        pipeline.write(Unpooled.copiedBuffer("Netty in Action", CharsetUtil.UTF_8));
    }

    public void writeViaChannelHandlerContext(ChannelHandlerContext ctx) {
        ctx.write(Unpooled.copiedBuffer("Netty in Action", CharsetUtil.UTF_8));
    }
}
