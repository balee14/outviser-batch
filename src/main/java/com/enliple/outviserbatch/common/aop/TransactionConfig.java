package com.enliple.outviserbatch.common.aop;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.aspectj.lang.annotation.Aspect;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.interceptor.RollbackRuleAttribute;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import lombok.RequiredArgsConstructor;

@Aspect
@Configuration
@RequiredArgsConstructor
public class TransactionConfig {

	private final PlatformTransactionManager txManager;

	@Bean
	public DefaultPointcutAdvisor txAdviceService() {
		AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
		pointcut.setExpression("execution(* com.enliple.outviserbatch..*Service.*(..))");
		return new DefaultPointcutAdvisor(pointcut, txAdvice());
	}

	/**
	 * Service 로 끝나는 class 에 대한 처리
	 * 
	 * @return
	 */
	private TransactionInterceptor txAdvice() {

		String transactionAttributesDefinition = "";
		Properties txAttributes = new Properties();

		List<RollbackRuleAttribute> rollbackRules = new ArrayList<>();
		rollbackRules.add(new RollbackRuleAttribute(Exception.class));

		RuleBasedTransactionAttribute attribute = new RuleBasedTransactionAttribute();
		attribute.setRollbackRules(rollbackRules);
		attribute.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
		attribute.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		transactionAttributesDefinition = attribute.toString();

		// SchedulePointJob -> SchedulePointService.class
		txAttributes.setProperty("removeExpiryPoint", transactionAttributesDefinition);
		// ScheduleReportJob -> ScheduleReportService.class
		txAttributes.setProperty("reportMts", transactionAttributesDefinition);
		// ScheduleAtalkInspectionJob -> ScheduleAtalkInspectionService.class
		txAttributes.setProperty("inspection", transactionAttributesDefinition);
		// SendService.class
		txAttributes.setProperty("sendProcess", transactionAttributesDefinition);
		// ExeRunService.class
		txAttributes.setProperty("insertExeRunHst", transactionAttributesDefinition);
		// ExeChkService.class
		txAttributes.setProperty("insertExecutionalChk", transactionAttributesDefinition);

		attribute.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		transactionAttributesDefinition = attribute.toString();

		// 그 외
		txAttributes.setProperty("delete*", transactionAttributesDefinition);
		txAttributes.setProperty("update*", transactionAttributesDefinition);
		txAttributes.setProperty("insert*", transactionAttributesDefinition);

		// read-only
		attribute = new RuleBasedTransactionAttribute();
		attribute.setReadOnly(true);
		transactionAttributesDefinition = attribute.toString();

		txAttributes.setProperty("select*", transactionAttributesDefinition);

		TransactionInterceptor txAdvice = new TransactionInterceptor();
		txAdvice.setTransactionAttributes(txAttributes);
		txAdvice.setTransactionManager(txManager);
		return txAdvice;
	}

	/**
	 * AbstractJobRepositoryFactoryBean.class
	 * 
	 ********* BATCH 에서 기본적으로 트랜잭션(AOP 처리)이 동작하므로 아래 로직은 한번쯤 참고...
	 * 
	private void initializeProxy() throws Exception {
		if (proxyFactory == null) {
			proxyFactory = new ProxyFactory();
			TransactionInterceptor advice = new TransactionInterceptor(transactionManager,
					PropertiesConverter.stringToProperties("create*=PROPAGATION_REQUIRES_NEW,"
							+ isolationLevelForCreate + "\ngetLastJobExecution*=PROPAGATION_REQUIRES_NEW,"
							+ isolationLevelForCreate + "\n*=PROPAGATION_REQUIRED"));
			if (validateTransactionState) {
				DefaultPointcutAdvisor advisor = new DefaultPointcutAdvisor(new MethodInterceptor() {
					@Override
					public Object invoke(MethodInvocation invocation) throws Throwable {
						if (TransactionSynchronizationManager.isActualTransactionActive()) {
							throw new IllegalStateException(
									"Existing transaction detected in JobRepository. "
											+ "Please fix this and try again (e.g. remove @Transactional annotations from client).");
						}
						return invocation.proceed();
					}
				});
				NameMatchMethodPointcut pointcut = new NameMatchMethodPointcut();
				pointcut.addMethodName("create*");
				advisor.setPointcut(pointcut);
				proxyFactory.addAdvisor(advisor);
			}
			proxyFactory.addAdvice(advice);
			proxyFactory.setProxyTargetClass(false);
			proxyFactory.addInterface(JobRepository.class);
			proxyFactory.setTarget(getTarget());
		}
	}
	 */
}
