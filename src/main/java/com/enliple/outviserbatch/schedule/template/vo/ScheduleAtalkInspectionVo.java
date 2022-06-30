package com.enliple.outviserbatch.schedule.template.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@NoArgsConstructor
public class ScheduleAtalkInspectionVo {

	private int acctRowid;

	private String acctLoginId;

	private String corpNm;

	private int tmpSenderRowid;

	private String atlkSenderKey;

	private String atlkName;

	private String atlkChannelId;

	public ScheduleAtalkInspectionVo(int acctRowid, String acctLoginId, String corpNm,
			int tmpSenderRowid, String atlkSenderKey, String atlkName, String atlkChannelId) {

		this.acctRowid = acctRowid;
		this.acctLoginId = acctLoginId;
		this.corpNm = corpNm;
		this.tmpSenderRowid = tmpSenderRowid;
		this.atlkSenderKey = atlkSenderKey;
		this.atlkName = atlkName;
		this.atlkChannelId = atlkChannelId;
	}
}