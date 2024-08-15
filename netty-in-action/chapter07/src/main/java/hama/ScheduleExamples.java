package hama;

import io.netty.channel.Channel;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ScheduleExamples {

    public void schedule() {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(10);
        ScheduledFuture<?> future = executor.schedule(
                () -> System.out.println("Now it is 60 seconds later"),
                60,
                TimeUnit.SECONDS
        );

        executor.shutdown();
    }

    public void scheduleViaEventLoop(Channel ch) {
        ScheduledFuture<?> future = ch.eventLoop().schedule(
                () -> System.out.println("60 seconds later"),
                60,
                TimeUnit.SECONDS
        );
    }

    public void scheduleFixedViaEventLoop(Channel ch) {
        ScheduledFuture<?> future = ch.eventLoop().scheduleAtFixedRate(
                () -> System.out.println("Run Every 60 seconds"),
                60,
                60,
                TimeUnit.SECONDS
        );
    }

    public void cancelingTaskUsingScheduledFuture(Channel ch) {
        ScheduledFuture<?> future = ch.eventLoop().scheduleAtFixedRate(
                () -> System.out.println("Run Every 60 seconds"),
                60,
                60,
                TimeUnit.SECONDS
        );

        future.cancel(true);

    }
}
