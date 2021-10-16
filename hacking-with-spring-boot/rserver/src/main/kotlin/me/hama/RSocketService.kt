package me.hama

import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks

@Controller
class RSocketService(
    private val repository: ItemRepository
) {
    private val itemsSink: Sinks.Many<Item> = Sinks.many().multicast().onBackpressureBuffer()

    @MessageMapping("newItems.request-response")
    fun processNewItemsViaRSocketRequestResponse(item: Item): Mono<Item> {
        return repository.save(item)
            .doOnNext { savedItem -> itemsSink.tryEmitNext(savedItem) }
    }

    @MessageMapping("newItems.request-stream")
    fun findItemsViaRSocketRequestStream(): Flux<Item> {
        return repository.findAll().doOnNext(itemsSink::tryEmitNext)
    }

    @MessageMapping("newItems.fire-and-forget")
    fun processNewItemsViaRSocketFireAndForget(item: Item): Mono<Void> {
        return repository.save(item)
            .doOnNext { savedItem -> itemsSink.tryEmitNext(savedItem) }
            .then()
    }

    @MessageMapping("newItems.monitor")
    fun monitorNewItems(): Flux<Item> {
        return itemsSink.asFlux()
    }
}
