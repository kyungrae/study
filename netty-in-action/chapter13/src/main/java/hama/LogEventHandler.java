package hama;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class LogEventHandler extends SimpleChannelInboundHandler<LogEvent> {

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, LogEvent event) {
        String msg = event.getReceivedTimestamp() + " [" + event.getSource().toString() + "] [" + event.getLogfile() + "] : " + event.getMsg();
        System.out.println(msg);
    }
}
