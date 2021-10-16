package me.hama

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Hooks
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.util.concurrent.Executors
import kotlin.random.Random

class DebuggingReactorFlows {

    @Test
    fun simpleExample() {
        val executor = Executors.newSingleThreadScheduledExecutor()

        val source: List<Int> = if (Random.nextBoolean())
            List(10) { it + 1 }
        else
            List(4) { it + 1 }

        try {
            executor.submit { source[5] }.get()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            executor.shutdown()
        }
    }

    @Disabled
    @Test
    fun reactorExample() {
        val source: Mono<Int> = if (Random.nextBoolean())
            Flux.range(1, 10).elementAt(5)
        else
            Flux.just(1, 2, 3, 4).elementAt(5)

        source
            .subscribeOn(Schedulers.parallel())
            .block()
    }

    @Disabled
    @Test
    fun reactorDebuggingExample() {
        Hooks.onOperatorDebug()

        val source = if (Random.nextBoolean()) {
            Flux.range(1, 10).elementAt(5)
        } else {
            Flux.just(1, 2, 3, 4).elementAt(5)
        }

        source
            .subscribeOn(Schedulers.parallel())
            .block()
    }
}
