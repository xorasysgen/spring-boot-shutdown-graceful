package com.solum.message;

import java.util.Date;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.solum.app.config.ShutdownHealthChecker;
import com.solum.app.config.ShutdownHookHandler;
import com.solum.app.exception.CommonException;

@SpringBootApplication
@RestController
public class SpringWarApplication {

	@Bean
	HealthIndicator gracefulShutdownHealthCheck() {
		return new ShutdownHealthChecker();
	}

	@Autowired
	private ConfigurableApplicationContext applicationContext;

	public static void main(String[] args) {
		SpringApplication.run(SpringWarApplication.class, args);
	}

	@GetMapping("/date")
	public String serverDate() {
		System.out.println("hello");
		return new Date().toString();
	}

	@GetMapping("/long-process")
	public String pause() {
		System.out.println("process started..");
		try {
			Thread.sleep(20000);
		} catch (InterruptedException e) {
			throw new CommonException("SERVICE_UNAVAILABLE, Server forcefully shutdown");
		}
		return "Even Though commencing graceful shutdown initiated, Process finished successfully..";
	}

	@PostConstruct
	public void registerShutdownHook() {
		SpringApplication app = new SpringApplication(SpringWarApplication.class);
		app.setRegisterShutdownHook(false);
		Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownHookHandler(applicationContext)));

	}

	@PreDestroy
	public void destroy() {
		System.out.println("|||Pending active thread task completed|||");
	}
}
