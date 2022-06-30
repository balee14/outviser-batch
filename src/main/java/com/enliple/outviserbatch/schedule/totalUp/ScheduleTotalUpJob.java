package com.enliple.outviserbatch.schedule.totalUp;


import com.enliple.outviserbatch.common.exception.batch.BatchExceptionHandler;
import com.enliple.outviserbatch.schedule.totalUp.service.ScheduleTotalUpService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ScheduleTotalUpJob {

    @Autowired
    private ScheduleTotalUpService scheduleTotalUpService;

    @Autowired
    private BatchExceptionHandler handler;

    @Bean
    public Job totalUpScheduleJob(JobBuilderFactory jobFactory, Step totalUpScheduleStep) throws Exception {

        return jobFactory.get("totalUpScheduleJob")
                .start(totalUpScheduleStep)
                .build();
    }

    @Bean
    public Step totalUpScheduleStep(StepBuilderFactory stepFactory) throws Exception {

        return stepFactory.get("totalUpScheduleStep")
                .tasklet((contribution, chunkContext) -> {

                    try{
                       scheduleTotalUpService.totalUpProcess();
                    }catch ( Exception e ){
                    }

                    return RepeatStatus.FINISHED;
                })
                .exceptionHandler(handler)
                .build();
    }

}
