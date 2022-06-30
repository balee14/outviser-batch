package com.enliple.outviserbatch.common.conifg;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.enliple.outviserbatch.common.service.errorLog.service.CommonErrorLogService;

@Configuration
@EnableAsync
public class AsyncConfig {

	@Value("${thread.pool.core:0}")
	private int coreSize;

	@Value("${thread.pool.core.max:0}")
	private int coreMaxSize;

	@Value("${thread.pool.core.queue:0}")
	private int coreQueueSize;

	@Autowired
	private CommonErrorLogService commonErrLogService;

	@Bean(name = "threadPoolTaskExecutor", destroyMethod = "destroy")
	public Executor threadPoolTaskExecutor() {
		ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();

		if (coreSize > 0) {
			taskExecutor.setCorePoolSize(coreSize);
		}

		if (coreMaxSize > 0) {
			taskExecutor.setMaxPoolSize(coreMaxSize);
		}

		if (coreQueueSize > 0) {
			taskExecutor.setQueueCapacity(coreQueueSize);
		}
		taskExecutor.setThreadNamePrefix("Executor-");
		taskExecutor.initialize();

		return new HandlingExecutor(taskExecutor);
	}

	public class HandlingExecutor implements AsyncTaskExecutor {
		private AsyncTaskExecutor executor;

		public HandlingExecutor(AsyncTaskExecutor executor) {
			this.executor = executor;
		}

		@Override
		public void execute(Runnable task) {
			executor.execute(createWrappedRunnable(task));
		}

		@Override
		public void execute(Runnable task, long startTimeout) {
			executor.execute(createWrappedRunnable(task), startTimeout);
		}

		@Override
		public Future<?> submit(Runnable task) {
			return executor.submit(createWrappedRunnable(task));
		}

		@Override
		public <T> Future<T> submit(final Callable<T> task) {
			return executor.submit(createCallable(task));
		}

		private <T> Callable<T> createCallable(final Callable<T> task) {
			return new Callable<T>() {
				@Override
				public T call() throws Exception {
					try {
						return task.call();
					} catch (Exception ex) {
						handle(ex);
						throw ex;
					}
				}
			};
		}

		private Runnable createWrappedRunnable(final Runnable task) {
			return new Runnable() {
				@Override
				public void run() {
					try {
						task.run();
					} catch (Exception ex) {
						handle(ex);
					}
				}
			};
		}

		private void handle(Exception ex) {
			commonErrLogService.insertErrorLog(ex);
		}

		public void destroy() {
			if (executor instanceof ThreadPoolTaskExecutor) {
				((ThreadPoolTaskExecutor) executor).shutdown();
			}
		}
	}
}