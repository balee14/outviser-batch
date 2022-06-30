package com.enliple.outviserbatch.schedule.cafe24.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@NoArgsConstructor
public class ScheduleCafe24TokenReissuanceVo {

	private String acctLoginId;
	private String mallId;
	private String refreshTokenExpiresAt;

	public ScheduleCafe24TokenReissuanceVo(String acctLoginId, String mallId, String refreshTokenExpiresAt) {
		this.acctLoginId = acctLoginId;
		this.mallId = mallId;
		this.refreshTokenExpiresAt = refreshTokenExpiresAt;
	}
}