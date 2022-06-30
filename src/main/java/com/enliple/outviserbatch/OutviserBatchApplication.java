package com.enliple.outviserbatch;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.env.Environment;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
@EnableBatchProcessing
@EnableAspectJAutoProxy
@MapperScan(basePackageClasses = OutviserBatchApplication.class)
public class OutviserBatchApplication extends SpringBootServletInitializer {

	/*
	 * 로컬PC 환경에서 LIVE 브랜치 실행 시키지 않도록 하기 위함
	 *  ** virtualbox 설치된 경우 192.168.56.1 로 표출됨...
	 */
	private final static String[] LOCAL_IP = {"192.168.150.*", "192.168.56.1"};

	@Autowired
	private Environment environment;

	@PostConstruct
	private void postConstruct() throws Exception {

		String active = this.environment.getActiveProfiles()[0].toUpperCase();

		log.warn("###########################");
		log.warn("###########################");
		log.warn("## Profile = {}", active);
		log.warn("###########################");
		log.warn("###########################");

		if ("LIVE".equals(active)) {

			String ip = "";
			try {
				ip = InetAddress.getLocalHost().getHostAddress();
			} catch (UnknownHostException e) {}

			for (String deniedIp : LOCAL_IP) {
				if (("*".equals(deniedIp.substring(deniedIp.length() -1)) && ip.contains(deniedIp.replace("*", "")))
						|| ip.equals(deniedIp)) {

					if ("192.168.150.80".equals(ip)) {
						continue;
					}

					throw new Exception(String.format("It's not an IP that can run as a live branch - deniedIp: %s", deniedIp));
				}
			}
		}
	}

	@PreDestroy
	private void preDestroy() {
		log.warn("###########################");
		log.warn("###########################");
		log.warn("## STOP !!!!");
		log.warn("###########################");
		log.warn("###########################");
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(OutviserBatchApplication.class);
	}

	public static void main(String[] args) throws Exception {
		SpringApplication app = new SpringApplication(OutviserBatchApplication.class);
		app.addListeners(new ApplicationPidFileWriter());
		app.run(args);
	}
}
