package hama;

import io.netty.channel.*;

import java.io.File;
import java.io.FileInputStream;

public class FileRegionWriterHandler extends ChannelInboundHandlerAdapter {
    private final File file;
    private final Channel channel;

    public FileRegionWriterHandler(File file, Channel channel) {
        this.file = file;
        this.channel = channel;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        FileInputStream in = new FileInputStream(file);
        FileRegion region = new DefaultFileRegion(in.getChannel(), 0, file.length());

        channel.writeAndFlush(region).addListener((ChannelFutureListener) future -> {
            if (!future.isSuccess()) {
                Throwable cause = future.cause();
            }
        });
    }
}
