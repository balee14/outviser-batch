package com.enliple.outviserbatch.outviser.batch.common;

import javax.sql.DataSource;

import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JobConfig extends DefaultBatchConfigurer {

	@Autowired
	private DataSource dataSource;

	@Bean
	public JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor(JobRegistry jobRegistry) {

		JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor = new JobRegistryBeanPostProcessor();
		jobRegistryBeanPostProcessor.setJobRegistry(jobRegistry);

		return jobRegistryBeanPostProcessor;
	}

	@Override
	protected JobRepository createJobRepository() throws Exception {

		JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
		factory.setDataSource(dataSource);
		factory.setTransactionManager(getTransactionManager());
		factory.setIsolationLevelForCreate("ISOLATION_REPEATABLE_READ");
//		factory.setIncrementerFactory(customIncrementerFactory());
		factory.afterPropertiesSet();

		return factory.getObject();
	}


	/*
	 ******* 
	 ******* 필요시 사용
	 ******* 

	private DataFieldMaxValueIncrementerFactory customIncrementerFactory() {
		return new CustomDataFieldMaxValueIncrementerFactory(dataSource);
	}

	private class CustomDataFieldMaxValueIncrementerFactory extends DefaultDataFieldMaxValueIncrementerFactory {

		CustomDataFieldMaxValueIncrementerFactory(DataSource dataSource) {
			super(dataSource);
		}

		@Override
		public DataFieldMaxValueIncrementer getIncrementer(String incrementerType, String incrementerName) {
			DataFieldMaxValueIncrementer incrementer = super.getIncrementer(incrementerType, incrementerName);
			if (incrementer instanceof SqlServerMaxValueIncrementer) {
				((SqlServerMaxValueIncrementer) incrementer).setCacheSize(20);
			}
			return incrementer;
		}
	}
	*/
}
