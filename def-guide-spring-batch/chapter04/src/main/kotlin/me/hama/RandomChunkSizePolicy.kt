package me.hama

import org.springframework.batch.repeat.CompletionPolicy
import org.springframework.batch.repeat.RepeatContext
import org.springframework.batch.repeat.RepeatStatus
import kotlin.properties.Delegates
import kotlin.random.Random

class RandomChunkSizePolicy : CompletionPolicy {

    private var chunkSize by Delegates.notNull<Int>()
    private var totalProcessed by Delegates.notNull<Int>()
    private val random = Random(0)

    override fun isComplete(context: RepeatContext, result: RepeatStatus): Boolean {
        return if (result == RepeatStatus.FINISHED)
            true
        else isComplete(context)
    }

    override fun isComplete(context: RepeatContext): Boolean {
        return totalProcessed >= chunkSize
    }

    override fun start(parent: RepeatContext): RepeatContext {
        chunkSize = random.nextInt(20)
        totalProcessed = 0

        println("The chunk size has been set to $chunkSize")
        return parent
    }

    override fun update(context: RepeatContext) {
        totalProcessed++
    }
}
