package me.hama

import io.rsocket.metadata.WellKnownMimeType
import org.springframework.http.MediaType
import org.springframework.http.MediaType.*
import org.springframework.http.ResponseEntity
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.net.URI
import java.time.Duration

@RestController
class RSocketController(builder: RSocketRequester.Builder) {

    private val requester: Mono<RSocketRequester> = builder
        .dataMimeType(APPLICATION_JSON)
        .metadataMimeType(parseMediaType(WellKnownMimeType.MESSAGE_RSOCKET_ROUTING.toString()))
        .connectTcp("localhost", 7000)
        .retry(5)
        .cache()

    @PostMapping("/items/request-response")
    fun addNewItemUsingRSocketRequestResponse(@RequestBody item: Item): Mono<ResponseEntity<Any>> {
        return requester
            .flatMap { it.route("newItems.request-response").data(item).retrieveMono(Item::class.java) }
            .map { ResponseEntity.created(URI.create("/items/request-response")).body(it) }
    }

    @GetMapping(value = ["/items/request-stream"], produces = [APPLICATION_NDJSON_VALUE])
    fun findItemsUsingRSocketRequestStream(): Flux<Item> {
        return requester.flatMapMany {
            it.route("newItems.request-stream")
                .retrieveFlux(Item::class.java)
                .delayElements(Duration.ofSeconds(1))
        }
    }

    @PostMapping("/items/fire-and-forget")
    fun addNewItemUsingRSocketFireAndForget(@RequestBody item: Item): Mono<ResponseEntity<Any>> {
        return requester.flatMap {
            it.route("newItems.fire-and-forget")
                .data(item)
                .send()
        }
            .then(Mono.just(ResponseEntity.created(URI.create("/items/fire-and-forget")).build()))
    }

    @GetMapping(value = ["/items"], produces = [TEXT_EVENT_STREAM_VALUE])
    fun liveUpdates(): Flux<Item> {
        return requester.flatMapMany { it.route("newItems.monitor").retrieveFlux(Item::class.java) }
    }
}
