package com.enliple.outviserbatch.schedule.template;

import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.session.SqlSession;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.support.SynchronizedItemStreamReader;
import org.springframework.batch.item.support.builder.SynchronizedItemStreamReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import com.enliple.outviserbatch.common.data.DataMap;
import com.enliple.outviserbatch.common.exception.batch.BatchExceptionHandler;
import com.enliple.outviserbatch.outviser.front.template.service.TemplateService;
import com.enliple.outviserbatch.schedule.template.service.ScheduleAtalkInspectionService;
import com.enliple.outviserbatch.schedule.template.vo.ScheduleAtalkInspectionVo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class ScheduleAtalkInspectionJob {

	@Autowired
	private ScheduleAtalkInspectionService scheduleAtalkInspectionService;

	@Autowired
	private TemplateService templateService;

	@Autowired
	private BatchExceptionHandler handler;

	@Autowired
	private SqlSession sqlSession;

	@Autowired
	private DataSource dataSource;

	private final int size = 1;

	@Bean
	public Job atalkInspectionScheduleJob(JobBuilderFactory jobFactory, Step atalkInspectionScheduleStep) throws Exception {

		return jobFactory.get("atalkInspectionScheduleJob")
				.start(atalkInspectionScheduleStep)
				.build();
	}

	@Bean
	public Step atalkInspectionScheduleStep(StepBuilderFactory stepFactory) throws Exception {

		return stepFactory.get("atalkInspectionScheduleStep")
				.<ScheduleAtalkInspectionVo, DataMap>chunk(size)
				.reader(atalkInspectionRead())
				.processor(atalkInspectionProcessor())
				.writer(atalkInspectionWrite())
				.exceptionHandler(handler)
				.build();
	}

	private SynchronizedItemStreamReader<ScheduleAtalkInspectionVo> atalkInspectionRead() {

		JdbcCursorItemReader<ScheduleAtalkInspectionVo> reader = new JdbcCursorItemReaderBuilder<ScheduleAtalkInspectionVo>()
				.fetchSize(size)
				.dataSource(dataSource)
				.rowMapper(new BeanPropertyRowMapper<>(ScheduleAtalkInspectionVo.class))
				.sql(getSelectAllSenderInfoListQuery())
				.name("atalkInspectionRead")
				.build();

		return new SynchronizedItemStreamReaderBuilder<ScheduleAtalkInspectionVo>()
				.delegate(reader)
				.build();
	}

	private ItemProcessor<ScheduleAtalkInspectionVo, DataMap> atalkInspectionProcessor() {

		return item -> {
			DataMap output = new DataMap();
			output.put("acctRowid", item.getAcctRowid());
			output.put("acctLoginId", item.getAcctLoginId());
			output.put("corpNm", item.getCorpNm());
			output.put("tmpSenderRowid", item.getTmpSenderRowid());
			output.put("atlkSenderKey", item.getAtlkSenderKey());
			output.put("atlkName", item.getAtlkName());
			output.put("atlkChannelId", item.getAtlkChannelId());
			return output;
		};
	}

	private ItemWriter<DataMap> atalkInspectionWrite() {

		return list -> {
			DataMap data = list.get(0);

			String acctLoginId = data.getString("acctLoginId");
			String corpNm = data.getString("corpNm");
			String atlkName = data.getString("atlkName");
			String atlkChannelId = data.getString("atlkChannelId");
			String atlkSenderKey = data.getString("atlkSenderKey");

			List<DataMap> tmpList = templateService.selectTmpListBySenderRowid(data);
			int tmpSize = tmpList.size();

			String msg = String.format("%s(%s) -> 채널: %s(%s) / 검수요청 템플릿수: %s", acctLoginId, corpNm, atlkName, atlkChannelId, tmpSize);
			log.warn(msg);

			int sCnt = 0, eCnt = 0, pCnt = 0;

			/*
			 * 조회한 템플릿별 검수 상태 체크
			 */
			for (DataMap tmp : tmpList) {
				tmp.put("atlkSenderKey", atlkSenderKey);

				try {
					DataMap dummy = scheduleAtalkInspectionService.inspection(tmp);

					if (ObjectUtils.isNotEmpty(dummy)) {
						if (dummy.containsKey("eCnt")) {
							eCnt++;
						} else if (dummy.containsKey("pCnt")) {
							pCnt++;
						} else if (dummy.containsKey("sCnt")) {
							sCnt++;
						}
					}
				} catch (Exception e) {
					eCnt++;
					log.error(e.getMessage(), e);
				}
			}

			if (tmpSize > 0) {
				msg = String.format("성공: %s / 실패(에러): %s / 결과동일: %s", sCnt, eCnt, pCnt);
				log.warn(msg);
			}
		};
	}

	private String getSelectAllSenderInfoListQuery() {
		String mapperName = "com.enliple.outviserbatch.outviser.front.acct.mapper.AcctMapper.selectAllSenderInfoList";

		BoundSql boundSql = sqlSession.getConfiguration().getMappedStatement(mapperName).getBoundSql(null);
		return boundSql.getSql();
	}
}
