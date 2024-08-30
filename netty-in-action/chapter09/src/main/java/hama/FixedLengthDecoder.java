package hama;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class FixedLengthDecoder extends ByteToMessageDecoder {
    private final int frameLength;

    public FixedLengthDecoder(int frameLength) {
        if (frameLength <= 0)
            throw new IllegalStateException("frameLength must be a positive integer: " + frameLength);
        this.frameLength = frameLength;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> out) {
        if (in.readableBytes() >= frameLength) {
            ByteBuf buf = in.readSlice(frameLength);
            out.add(buf);
        }
    }
}
