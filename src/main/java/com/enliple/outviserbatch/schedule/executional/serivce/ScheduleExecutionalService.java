package com.enliple.outviserbatch.schedule.executional.serivce;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.enliple.outviserbatch.common.data.DataMap;
import com.enliple.outviserbatch.outviser.api.action.service.ActionService;
import com.enliple.outviserbatch.outviser.front.exe.chk.service.ExeChkService;

@Service
public class ScheduleExecutionalService {

	@Autowired
	private ActionService actionService;

	@Autowired
	private ExeChkService exeChkService;

	@Value("${spring.profiles.active}")
	private String activeServer;

	public void runExecutional(DataMap exe) throws Exception {

		DataMap param = new DataMap();
		param.put("exeRowid", exe.getLong("exeRowid"));
		param.put("campRowid", exe.getLong("campRowid"));
		param.put("sessionUserRowId", exe.get("sessionAdverId"));
		param.put("sessionUserId", exe.get("sessionUserId"));
		param.put("sessionAdverId", exe.get("sessionUserRowId"));
		param.put("exeChkComment", "Start");
		param.put("tranDate", exe.getString("tranDate"));
		// 집행 체크
		exeChkService.insertExecutionalChk(param);
		// 집행 시작
		actionService.requestExecutional(param);
	}
}
