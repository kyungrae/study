package hama;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelPipeline;

public class ModifyChannelPipeline {
    public void modifyPipeline(ChannelPipeline pipeline) {
        FirstHandler handler1 = new FirstHandler();
        pipeline.addLast("handler1", handler1);
        pipeline.addFirst("handler2", new SecondHandler());
        pipeline.addLast("handler3", new ThirdHandler());

        pipeline.remove("handler3");
        pipeline.remove(handler1);
        pipeline.replace("handler2", "handler4", new FourthHandler());
    }

    private class FirstHandler extends ChannelHandlerAdapter {}
    private class SecondHandler extends ChannelHandlerAdapter {}
    private class ThirdHandler extends ChannelHandlerAdapter {}
    private class FourthHandler extends ChannelHandlerAdapter {}
}
