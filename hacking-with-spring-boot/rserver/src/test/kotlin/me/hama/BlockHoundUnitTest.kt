package me.hama

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.Duration

class BlockHoundUnitTest {

    @Test
    fun threadSleepIsABlockingCall() {
        Mono.delay(Duration.ofSeconds(1))
            .flatMap {
                Thread.sleep(10)
                Mono.just(true)
            }
            .`as`(StepVerifier::create)
            .expectErrorMatches() { throwable ->
                assertThat(throwable.message).contains("Blocking call! java.lang.Thread.sleep")
                true
            }
    }
}
