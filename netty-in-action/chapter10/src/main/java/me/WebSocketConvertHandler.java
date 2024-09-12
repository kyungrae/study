package me;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.handler.codec.http.websocketx.*;

import java.util.List;

public class WebSocketConvertHandler extends MessageToMessageCodec<WebSocketFrame, WebSocketConvertHandler.MyWebSocketFrame> {

    @Override
    protected void encode(ChannelHandlerContext ctx, MyWebSocketFrame msg, List<Object> out) {
        ByteBuf payload = msg.buf.duplicate().retain();
        switch (msg.type) {
            case BINARY -> out.add(new BinaryWebSocketFrame(payload));
            case TEXT -> out.add(new TextWebSocketFrame(payload));
            case CLOSE -> out.add(new CloseWebSocketFrame(true, 0, payload));
            case CONTINUATION -> out.add(new ContinuationWebSocketFrame(payload));
            case PING -> out.add(new PongWebSocketFrame(payload));
            case PONG -> out.add(new PingWebSocketFrame(payload));
            default -> throw new IllegalStateException("Unsupported websocket message");
        }
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, WebSocketFrame msg, List<Object> out) {
        ByteBuf payload = msg.content().duplicate().retain();

        if (msg instanceof BinaryWebSocketFrame) {
            out.add(new MyWebSocketFrame(MyWebSocketFrame.FrameType.BINARY, payload));
        } else if (msg instanceof CloseWebSocketFrame) {
            out.add(new MyWebSocketFrame(MyWebSocketFrame.FrameType.CLOSE, payload));
        } else if (msg instanceof PingWebSocketFrame) {
            out.add(new MyWebSocketFrame(MyWebSocketFrame.FrameType.PING, payload));
        } else if (msg instanceof PongWebSocketFrame) {
            out.add(new MyWebSocketFrame(MyWebSocketFrame.FrameType.PONG, payload));
        } else if (msg instanceof TextWebSocketFrame) {
            out.add(new MyWebSocketFrame(MyWebSocketFrame.FrameType.TEXT, payload));
        } else if (msg instanceof ContinuationWebSocketFrame) {
            out.add(new MyWebSocketFrame(MyWebSocketFrame.FrameType.CONTINUATION, payload));
        } else {
            throw new IllegalStateException("Unsupported websocket msg " + msg);
        }
    }

    public record MyWebSocketFrame(FrameType type, ByteBuf buf) {
        public enum FrameType {
            BINARY, CLOSE, PING, PONG, TEXT, CONTINUATION
        }
    }
}
