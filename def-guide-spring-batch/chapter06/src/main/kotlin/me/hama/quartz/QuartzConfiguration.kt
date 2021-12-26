package me.hama.quartz

import org.quartz.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class QuartzConfiguration {
    @Bean
    fun quartzJobDetail(): JobDetail {
        return JobBuilder.newJob(BatchScheduledJob::class.java)
            .storeDurably()
            .build()
    }

    @Bean
    fun jobTrigger(): Trigger {
        val scheduleBuilder = SimpleScheduleBuilder.simpleSchedule()
            .withIntervalInSeconds(5)
            .withRepeatCount(4)

        return TriggerBuilder.newTrigger()
            .forJob(quartzJobDetail())
            .withSchedule(scheduleBuilder)
            .build()
    }
}
