package me.hama

import org.springframework.amqp.core.AmqpTemplate
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.net.URI

@RestController
class SpringAmqpItemController(
    private val template: AmqpTemplate
) {

    @PostMapping("/items")
    fun addNewItemUsingSpringAmqp(@RequestBody item: Mono<Item>): Mono<ResponseEntity<Any>> {
        return item.subscribeOn(Schedulers.boundedElastic())
            .flatMap { content ->
                Mono.fromCallable {
                    template.convertAndSend("hacking-spring-boot", "new-items-spring-amqp", content)
                    ResponseEntity.created(URI.create("/items")).build()
                }
            }
    }
}

