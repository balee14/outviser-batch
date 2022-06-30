package com.enliple.outviserbatch.schedule.point;

import com.enliple.outviserbatch.common.data.DataMap;
import com.enliple.outviserbatch.common.exception.CommonException;
import com.enliple.outviserbatch.common.exception.batch.BatchExceptionHandler;
import com.enliple.outviserbatch.schedule.point.service.SchedulePointService;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.session.SqlSession;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.support.SynchronizedItemStreamReader;
import org.springframework.batch.item.support.builder.SynchronizedItemStreamReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Configuration
public class SchedulePointJob {

	@Autowired
	private SchedulePointService schedulePointService;

	@Autowired
	private BatchExceptionHandler handler;

	@Autowired
	private SqlSession sqlSession;

	@Autowired
	private DataSource dataSource;

	private final int size = 1;

	@Bean
	public Job pointScheduleJob(JobBuilderFactory jobFactory, Step pointScheduleStep) throws Exception {

		return jobFactory.get("pointScheduleJob")
				.start(pointScheduleStep)
				.build();
	}

	@Bean
	public Step pointScheduleStep(StepBuilderFactory stepFactory) throws Exception {

		return stepFactory.get("pointScheduleStep")
				.<Integer, Integer>chunk(size)
				.reader(pointRead())
				.writer(pointWrite())
				.exceptionHandler(handler)
				.build();
	}

	private SynchronizedItemStreamReader<Integer> pointRead() {

		JdbcCursorItemReader<Integer> reader = new JdbcCursorItemReaderBuilder<Integer>()
				.fetchSize(size)
				.dataSource(dataSource)
				.rowMapper(new RowMapper<Integer>() {
					@Override
					public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
						return rs.getInt("ACCT_ROWID");
					}
				})
				.sql(getSelectAcctRowidListForExpiryQuery())
				.name("pointRead")
				.build();

		return new SynchronizedItemStreamReaderBuilder<Integer>()
				.delegate(reader)
				.build();
	}

	private ItemWriter<Integer> pointWrite() {

		return list -> {
			int acctRowid = list.get(0);
			if (acctRowid <= 0) {
				throw new CommonException("SchedulePointJob acctRowid is null");
			}

			List<DataMap> listPoint = schedulePointService.selectChargeListForExpiry(acctRowid);
			schedulePointService.removeExpiryPoint( acctRowid, listPoint );
		};
	}

	private String getSelectAcctRowidListForExpiryQuery() {
		String mapperName = "com.enliple.outviserbatch.schedule.point.mapper.SchedulePointMapper.selectAcctRowidListForExpiry";

		BoundSql boundSql = sqlSession.getConfiguration().getMappedStatement(mapperName).getBoundSql(null);
		return boundSql.getSql();
	}
}
