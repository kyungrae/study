package me.hama

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.Exchange
import org.springframework.amqp.rabbit.annotation.Queue
import org.springframework.amqp.rabbit.annotation.QueueBinding
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class SpringAmqpItemService(
    private val repository: ItemRepository
) {
    @RabbitListener(
        ackMode = "MANUAL",
        bindings = [QueueBinding(value = Queue(), exchange = Exchange("hacking-spring-boot"), key = ["new-items-spring-amqp"])]
    )
    fun processNewItemViaSpringAmqp(item: Item): Mono<Void> {
        log.info("Consuming => $item")
        return repository.save(item).then()
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(SpringAmqpItemService::class.java)
    }
}
