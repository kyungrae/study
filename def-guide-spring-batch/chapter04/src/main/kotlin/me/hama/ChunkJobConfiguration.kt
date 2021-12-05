package me.hama

import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.file.FlatFileItemReader
import org.springframework.batch.item.file.FlatFileItemWriter
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder
import org.springframework.batch.item.file.mapping.PassThroughLineMapper
import org.springframework.batch.item.file.transform.PassThroughLineAggregator
import org.springframework.batch.item.support.ListItemReader
import org.springframework.batch.repeat.CompletionPolicy
import org.springframework.batch.repeat.policy.CompositeCompletionPolicy
import org.springframework.batch.repeat.policy.SimpleCompletionPolicy
import org.springframework.batch.repeat.policy.TimeoutTerminationPolicy
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource
import java.util.*

//@Configuration
class ChunkJobConfiguration(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory
) {
    @Bean
    fun job() = jobBuilderFactory["job"]
        .start(step())
        .build()

    @Bean
    fun step() = stepBuilderFactory["step"]
        .chunk<String, String>(10)
        .reader(fileItemReader(null))
        .writer(fileItemWriter(null))
        .build()

    @StepScope
    @Bean
    fun fileItemReader(@Value("#{jobParameters['inputFile']}") inputFile: Resource?): FlatFileItemReader<String> {
        val inputFile = checkNotNull(inputFile)
        return FlatFileItemReaderBuilder<String>()
            .name("itemReader")
            .resource(inputFile)
            .lineMapper(PassThroughLineMapper())
            .build()
    }

    @StepScope
    @Bean
    fun fileItemWriter(@Value("#{jobParameters['outputFile']}") outputFile: Resource?): FlatFileItemWriter<String> {
        val outputFile = checkNotNull(outputFile)
        return FlatFileItemWriterBuilder<String>()
            .name("itemWriter")
            .resource(outputFile)
            .lineAggregator(PassThroughLineAggregator())
            .build()
    }

    @Bean
    fun chunkBasedJob() =
        jobBuilderFactory["chunkBasedJob"]
            .start(chunkStep())
            .build()

    @Bean
    fun chunkStep() =
        stepBuilderFactory["chunkStepListener"]
            .chunk<String, String>(1000)
//            .chunk<String, String>(completionPolicy())
//            .chunk<String, String>(randomCompletionPolicy())
            .reader(itemReader())
            .writer(itemWriter())
            .listener(LoggingStepStartStopListener())
            .build()

    @Bean
    fun itemReader(): ListItemReader<String> {
        val items = List(100000) { UUID.randomUUID().toString() }
        return ListItemReader(items)
    }

    @Bean
    fun itemWriter() = ItemWriter<String> { items ->
        items.forEach { println(">> current item = $it") }
    }

    @Bean
    fun completionPolicy(): CompletionPolicy {
        val policy = CompositeCompletionPolicy()
        policy.setPolicies(
            arrayOf(
                TimeoutTerminationPolicy(3),
                SimpleCompletionPolicy(1000)
            )
        )
        return policy
    }

    @Bean
    fun randomCompletionPolicy() = RandomChunkSizePolicy()
}
