package com.enliple.outviserbatch.schedule.reviseTotalUp;

import com.enliple.outviserbatch.common.exception.batch.BatchExceptionHandler;
import com.enliple.outviserbatch.schedule.reviseTotalUp.service.ScheduleReviseTotalUpService;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.batch.core.Job;

@Configuration
public class ScheduleReviseTotalUpJob {

    @Autowired
    ScheduleReviseTotalUpService scheduleReviseTotalUpService;

    @Autowired
    private BatchExceptionHandler handler;

    @Bean
    public Job reviseTotalUpScheduleJob(JobBuilderFactory jobFactory, Step reviseTotalUpScheduleStep) throws Exception {

        return jobFactory.get("reviseTotalUpScheduleJob")
                .start(reviseTotalUpScheduleStep)
                .build();
    }

    @Bean
    public Step reviseTotalUpScheduleStep(StepBuilderFactory stepFactory) throws Exception {

        return stepFactory.get("reviseTotalUpScheduleStep")
                .tasklet((contribution, chunkContext) -> {

                    try{
                        scheduleReviseTotalUpService.reviseTotalUpProcess();
                    }catch ( Exception e ){
                    }

                    return RepeatStatus.FINISHED;
                })
                .exceptionHandler(handler)
                .build();
    }
}
