package com.enliple.outviserbatch.common.enumeration;

public class Type {

	public enum MTSApiUri {
		SELECT_TEMPLATE("/state/template"),
		INSERT_TEMPLATE("/create/template"),
		UPDATE_TEMPLATE("/modify/template"),
		DELETE_TEMPLATE("/delete/template"),
		INSERT_TEMPLATE_INSPECT("/inspect/template"),
		INSERT_TEMPLATE_COMMENT("/template/comment");

		private String value;

		public String getValue() {
			return value;
		}

		private MTSApiUri(String value) {
			this.value = value;
		}
	}

	public enum AtlkInspectionStatus {
		REG("REG", "등록"), REQ("REQ", "검수요청"), APR("APR", "승인"), KRR("KRR", "등록거절"), REJ("REJ", "승인반려");

		private String value;

		private String desc;

		public String getValue() {
			return value;
		}

		public String getDesc() {
			return desc;
		}

		private AtlkInspectionStatus(String value, String desc) {
			this.value = value;
			this.desc = desc;
		}
	}

	public enum AtlkStatus {
		S("S", "중단"), A("A", "정상"), R("R", "대기(발송전)");

		private String value;

		private String desc;

		public String getValue() {
			return value;
		}

		public String getDesc() {
			return desc;
		}

		private AtlkStatus(String value, String desc) {
			this.value = value;
			this.desc = desc;
		}
	}

	public enum Template {
		ATALK("ATL", "알림톡"),
		FTALK("FTL", "친구톡"),
		SMS("SMS", "문자(단문)"),
		LMS("LMS", "문자(장문)"),
		MMS("MMS", "문자(장문+이미지)"),
		EMAIL("EML", "이메일");

		private String value;

		private String desc;

		public String getValue() {
			return value;
		}

		public String getDesc() {
			return desc;
		}

		private Template(String value, String desc) {
			this.value = value;
			this.desc = desc;
		}
	}

}
