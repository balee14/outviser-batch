package com.enliple.outviserbatch.schedule.refund;


import com.enliple.outviserbatch.common.exception.batch.BatchExceptionHandler;
import com.enliple.outviserbatch.schedule.refund.service.ScheduleRefundService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ScheduleRefundJob {

    @Autowired
    private ScheduleRefundService scheduleRefundService;

    @Autowired
    private BatchExceptionHandler handler;

    @Bean
    public Job refundScheduleJob(JobBuilderFactory jobFactory, Step refundScheduleStep ) throws Exception {

        return jobFactory.get( "refundScheduleJob" )
                .start( refundScheduleStep )
                .build();
    }

    @Bean
    public Step refundScheduleStep(StepBuilderFactory stepFactory) throws Exception{

        return stepFactory.get("refundScheduleStep")
            .tasklet((contribution, chunkContext) -> {
                try{
                    scheduleRefundService.refundInit();
                }catch ( Exception e ){

                }
                return RepeatStatus.FINISHED;
            })
            .exceptionHandler(handler)
            .build();
    }


}
