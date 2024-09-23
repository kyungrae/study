package hama;

import io.netty.channel.*;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedStream;
import io.netty.handler.stream.ChunkedWriteHandler;

import java.io.FileInputStream;

public class ChunkedWriteHandlerInitializer extends ChannelInitializer<Channel> {
    private final SslContext context;

    public ChunkedWriteHandlerInitializer(SslContext context) {
        this.context = context;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(
                new SslHandler(context.newEngine(ch.alloc())),
                new ChunkedWriteHandler(),
                new WriteStreamHandler()
        );
    }

    public static final class WriteStreamHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            super.channelActive(ctx);
            ctx.writeAndFlush(new ChunkedStream(new FileInputStream("")));
        }
    }
}
