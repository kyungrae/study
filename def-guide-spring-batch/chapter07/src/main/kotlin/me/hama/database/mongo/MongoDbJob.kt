package me.hama.database.mongo

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.data.MongoItemReader
import org.springframework.batch.item.data.builder.MongoItemReaderBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoOperations

@EnableBatchProcessing
@SpringBootApplication
class MongoDbJob(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory
) {
    @Bean
    @StepScope
    fun tweetsItemReader(
        mongoTemplate: MongoOperations?,
        @Value("#{jobParameters['hashTag']}") hashTag: String?
    ): MongoItemReader<Map<*, *>> {
        return MongoItemReaderBuilder<Map<*, *>>()
            .name("tweetsItemReader")
            .targetType(Map::class.java)
            .jsonQuery("{ \"entities.hashtags.text\": { \$eq: ?0 }}")
            .collection("tweets_collection")
            .parameterValues(listOf(hashTag))
            .pageSize(10)
            .sorts(mapOf("created_at" to Sort.Direction.ASC))
            .template(mongoTemplate!!)
            .build()
    }

    @Bean
    fun itemWriter() = ItemWriter<Map<*, *>> { it.forEach(::println) }

    @Bean
    fun copyFileStep(): Step {
        return stepBuilderFactory["copyFileStep"]
            .chunk<Map<*, *>, Map<*, *>>(10)
            .reader(tweetsItemReader(null, null))
            .writer(itemWriter())
            .build()
    }

    @Bean
    fun job(): Job {
        return jobBuilderFactory["job"]
            .start(copyFileStep())
            .build()
    }
}

fun main(args: Array<String>) {
    runApplication<MongoDbJob>(*args)
}
