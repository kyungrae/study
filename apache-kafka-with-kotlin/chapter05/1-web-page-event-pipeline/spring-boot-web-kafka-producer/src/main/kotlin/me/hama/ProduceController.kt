package me.hama

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.util.concurrent.ListenableFutureCallback
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.text.SimpleDateFormat
import java.util.*

@RestController
class ProduceController(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val objectMapper: ObjectMapper
) {
    private val logger: Logger = LoggerFactory.getLogger(ProduceController::class.java)

    @GetMapping("/api/select")
    fun selectColor(
        @RequestHeader("user-agent") userAgent: String,
        @RequestParam color: String,
        @RequestParam user: String
    ) {
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZ")
        val userEventVO = UserEventVO(formatter.format(Date()), userAgent, color, user)

        kafkaTemplate.send("select-color", objectMapper.writeValueAsString(userEventVO))
            .addCallback(object : ListenableFutureCallback<SendResult<String, String>> {
                override fun onSuccess(result: SendResult<String, String>?) = logger.info(result?.toString())
                override fun onFailure(ex: Throwable) = logger.error(ex.message, ex)
            })
    }
}
