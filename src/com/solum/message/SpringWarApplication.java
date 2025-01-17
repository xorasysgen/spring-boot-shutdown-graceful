package com.solum.message;

import java.util.Date;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.solum.app.config.ContextClosedHandler;
import com.solum.app.config.ShutdownHealthChecker;
import com.solum.app.config.ShutdownHookHandler;
import com.solum.app.exception.CommonException;

@SpringBootApplication
@RestController
public class SpringWarApplication {
	
	private static final Log log = LogFactory.getLog(SpringWarApplication.class);

	@Bean
	public ContextClosedHandler contextClosedHandler() {
		return new ContextClosedHandler();

	}

	@Bean
	public HealthIndicator gracefulShutdownHealthCheck() {
		return new ShutdownHealthChecker();
	}

	@Autowired
	private ConfigurableApplicationContext applicationContext;

	public static void main(String[] args) {
		SpringApplication.run(SpringWarApplication.class, args);
	}

	@GetMapping("/")
	public String serverDate() {
		log.debug("system is up and running...");
		return new Date().toString();
	}

	@GetMapping("/long-process")
	public String pause() {
		int runUntill=100;
		System.out.println("Thread having PID [" + Thread.currentThread().getId() + "] long process started.." + runUntill + " times should execute");
		try {
			for (int i = 1; i <= runUntill; i++) {
				if(i!=100)
					System.out.println("thread Job PID " + Thread.currentThread().getId() + " processing " + i + "th item out of "+ runUntill);
				else
					System.out.println("thread Job PID " + Thread.currentThread().getId() + " [ Completed ]" + i + "th item out of "+ runUntill + " Thread exited...");
				Thread.sleep(500);
			}
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
		//TODO  #may perform clean up
		log.debug(":::::Pending active thread task  completed:::::");
	}

}
