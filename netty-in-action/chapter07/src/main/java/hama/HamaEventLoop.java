package hama;

import io.netty.channel.DefaultEventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SingleThreadEventLoop;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;

public class HamaEventLoop extends SingleThreadEventLoop {

    public HamaEventLoop() {
        this((EventLoopGroup) null);
    }

    public HamaEventLoop(ThreadFactory threadFactory) {
        this(null, threadFactory);
    }

    public HamaEventLoop(Executor executor) {
        this(null, executor);
    }

    public HamaEventLoop(EventLoopGroup parent) {
        this(parent, new DefaultThreadFactory(DefaultEventLoop.class));
    }

    public HamaEventLoop(EventLoopGroup parent, ThreadFactory threadFactory) {
        super(parent, threadFactory, true);
    }

    public HamaEventLoop(EventLoopGroup parent, Executor executor) {
        super(parent, executor, true);
    }

    @Override
    protected void run() {
        for (; ; ) {
            Runnable task = takeTask();
            if (task != null) {
                runTask(task);
                updateLastExecutionTime();
            }

            if (confirmShutdown()) {
                break;
            }
        }
    }
}
