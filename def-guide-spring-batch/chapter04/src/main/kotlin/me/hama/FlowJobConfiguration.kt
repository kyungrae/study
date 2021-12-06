package me.hama

import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.job.builder.FlowBuilder
import org.springframework.batch.core.job.flow.Flow
import org.springframework.batch.core.step.job.DefaultJobParametersExtractor
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FlowJobConfiguration(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory
) {
    @Bean
    fun loadStockFile() = Tasklet { contribution, chunkContext ->
        println("The stock file has been loaded")
        RepeatStatus.FINISHED
    }

    @Bean
    fun loadCustomerFile() = Tasklet { contribution, chunkContext ->
        println("The customer file has been loaded")
        RepeatStatus.FINISHED
    }

    @Bean
    fun updateStart() = Tasklet { contribution, chunkContext ->
        println("The start has been updated")
        RepeatStatus.FINISHED
    }

    @Bean
    fun runBatchTasklet() = Tasklet { contribution, chunkContext ->
        println("The batch has been run")
        RepeatStatus.FINISHED
    }

    @Bean
    fun preProcessingFlow() = FlowBuilder<Flow>("preProcessingFlow")
        .start(loadFileStep())
        .next(loadCustomerStep())
        .next(updateStartStep())
        .build()

//    @Bean
//    fun conditionalStepLogicJobFlow() = jobBuilderFactory["conditionalStepLogicJobFlow"]
//        .start(preProcessingFlow())
//        .next(runBatch())
//        .end()
//        .build()

//    @Bean
//    fun conditionalStepLogicJobStepWrapping() = jobBuilderFactory["conditionalStepLogicJobStepWrapping"]
//        .start(initializeBatchStep())
//        .next(runBatch())
//        .build()

    @Bean
    fun initializeBatchStep() = stepBuilderFactory["initializeBatchStep"]
        .flow(preProcessingFlow())
        .build()

    @Bean
    fun preProcessingJob() = jobBuilderFactory["preProcessingJob"]
        .start(loadFileStep())
        .next(loadCustomerStep())
        .next(updateStartStep())
        .build()

    @Bean
    fun conditionalStepLogicJobWithoutExternalizing() = jobBuilderFactory["conditionalStepLogicJobWithoutExternalizing"]
        .start(initializeBatch())
        .next(runBatch())
        .build()

    @Bean
    fun initializeBatch() = stepBuilderFactory["initializeBatch"]
        .job(preProcessingJob())
        .parametersExtractor(DefaultJobParametersExtractor())
        .build()

    @Bean
    fun loadFileStep() = stepBuilderFactory["loadFileStep"]
        .tasklet(loadStockFile())
        .build()

    @Bean
    fun loadCustomerStep() = stepBuilderFactory["loadCustomerStep"]
        .tasklet(loadCustomerFile())
        .build()

    @Bean
    fun updateStartStep() = stepBuilderFactory["updateStartStep"]
        .tasklet(updateStart())
        .build()

    @Bean
    fun runBatch() = stepBuilderFactory["runBatch"]
        .tasklet(updateStart())
        .build()
}
