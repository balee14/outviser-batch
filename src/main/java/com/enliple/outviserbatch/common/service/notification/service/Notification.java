package com.enliple.outviserbatch.common.service.notification.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.enliple.outviserbatch.common.data.DataMap;
import com.enliple.outviserbatch.common.util.CommonUtils;
import com.enliple.outviserbatch.outviser.front.mts.service.MtsService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class Notification {

	@Autowired
	private MtsService mtsService;

	@Value("${notify.mail.alert.yn}")
	private String mailYn;

	@Value("${notify.mail.to.develop}")
	private String mailDevelopTo;

	@Value("${notify.mail.from}")
	private String mailSendFrom;

	@Value("${notify.mail.id}")
	private String mailSendId;

	@Value("${notify.mail.pwd}")
	private String mailSendPwd;

	@Value("${notify.mail.host}")
	private String smtpHost;

	@Value("${notify.mail.port}")
	private String smtpPort;

	@Value("${notify.mail.ssl.enable}")
	private String sslEnable;

	@Value("${notify.sms.from}")
	private String callback;

	@Value("${notify.telegram.alert.yn}")
	private String telegramYn;

	@Value("${notify.telegram.token}")
	private String[] telegramTokens;

	@Value("${notify.telegram.chat.id}")
	private String[] telegramChatIds;

	public void alert(boolean isCustomer, DataMap param) {

		new Notify().alert(isCustomer, param);
	}

	/**
	 * Inner class
	 * <br> - 발송 처리용 내부 클래스 정의
	 * <br> - 일회성이면서 가독성을 위해 분리
	 * 
	 * @author jbnoh
	 */
	class Notify {

		/**
		 * 1. 광고주에게 메일 or 친구톡 발송
		 * <br> - isCustomer 값이 true 인 경우 동작
		 * <br>
		 * 2. DataMap key:value 형태
		 * <br> - subject:제목 / message:본문내용 / msgType:메시지타입(EML=메일, SMS=80자내 문자)
		 * <br> - receiver:수신인정보(메일:abc@enliple.com, 문자:01012345678)
		 * <br>
		 * <br>
		 * 기타: 발신인은 프로퍼티에서 가져옴
		 * 
		 * @param isCustomer
		 * @param param
		 */
		void alert(boolean isCustomer, DataMap param) {
			String subject = param.getString("subject");
			String message = param.getString("message");

			if (isCustomer) {
				/* dataMap 으로 가져오기 */
				String receiver = param.getString("receiver");
				String msgType = param.getString("msgType");

				if ("EML".equalsIgnoreCase(msgType)) {
					// 메일 발송
					sendMail(mailSendFrom, receiver, subject, message);
				} else if ("SMS".equalsIgnoreCase(msgType)) {
					// 문자 발송
					param.put("callback", callback);
					mtsService.insertSmsByNotify(param);
				} else {
					log.warn("Can't send messages to customers -> msgType: {}", msgType);
				}
			}

			alert(subject, message);
		}

		/**
		 * 개발자 알림 전용
		 * 
		 * @param subject
		 * @param message
		 */
		void alert(String subject, String message) {
			// 텔레그램
			if ("Y".equalsIgnoreCase(telegramYn)) {
				message = String.format("%s<br><br>%s", subject, message);
				multiSendTelegramMsg(telegramTokens, telegramChatIds, message);
			}

			// 메일
			if ("Y".equalsIgnoreCase(mailYn)) {
				sendMail(mailSendFrom, mailDevelopTo, subject, message);
			}
		}

		void sendMail(String from, String receiver, String subject, String msg) {
			String[] arrTo = { receiver };
			if (receiver.indexOf(",") > -1) {
				arrTo = receiver.split(",");
			}

			sendMail(from, arrTo, subject, msg);
		}

		void sendMail(String from, String[] arrTo, String subject, String msg) {
			try {
				Properties props = System.getProperties();
				props.put("mail.smtp.host", smtpHost);
				props.put("mail.smtp.port", smtpPort);
				props.put("mail.smtp.auth", "true");
				if ("true".equalsIgnoreCase(sslEnable)) {
					props.put("mail.smtp.ssl.enable", sslEnable);
					props.put("mail.smtp.ssl.trust", smtpHost);
				}

				Session session = Session.getDefaultInstance(props, new Authenticator() {

					@Override
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(mailSendId, mailSendPwd);
					}
				});
				session.setDebug(true);

				MimeMessage message = new MimeMessage(session);
				message.setFrom(new InternetAddress(from));

				// 받는 사람이 복수인 경우 처리
				InternetAddress[] address = new InternetAddress[arrTo.length];
				for (int i = 0; i < arrTo.length; i++) {
					String to = arrTo[i];
					address[i] = new InternetAddress(to);
				}
				message.setRecipients(Message.RecipientType.TO, address);

				message.setSubject(subject);

				MimeBodyPart mbp1 = new MimeBodyPart();
				mbp1.setContent(msg, "text/html;charset=UTF-8");

				Multipart mp = new MimeMultipart();
				mp.addBodyPart(mbp1);

				message.setContent(mp);

				Transport.send(message);
			} catch (Exception e) {
				log.debug("메일 발송 오류 : ", e);
			}
		}

		void multiSendTelegramMsg(String[] tokens, String[] chatIds, String text) {
			if (StringUtils.isNotBlank(text) && tokens.length == chatIds.length) {
				for (int i = 0; i < tokens.length; i++) {
					sendTelegramMsg(tokens[i], chatIds[i], text);
				}
			}
		}

		void sendTelegramMsg(String token, String chatId, String text) {
			BufferedReader in = null;

			try {
				text = text.replace("<br>", "\n");
				text = CommonUtils.urlEncode(text);

				// 호출할 URL
				URL obj = new URL("https://api.telegram.org/bot" + token + "/sendmessage?chat_id=" + chatId + "&text=" + text);

				HttpURLConnection con = (HttpURLConnection)obj.openConnection();
				con.setRequestMethod("GET");
				in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));

				String line;
				while((line = in.readLine()) != null) {
					// response를 차례대로 출력
					log.warn(line);
				}

			} catch(Exception e) {
				log.error(e.getMessage(), e);
			} finally {
				IOUtils.closeQuietly(in);
			}
		}
	}
}
