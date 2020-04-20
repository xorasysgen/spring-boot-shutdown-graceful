package com.solum.app.config;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

public class ContextClosedHandler implements ApplicationListener<ContextClosedEvent> {

	private static final Log log = LogFactory.getLog(ContextClosedHandler.class);

	@Autowired
	ThreadPoolTaskExecutor executor;
	
	@PostConstruct
	public void contextClosedHandler() {
		log.debug("ContextClosedHandler started");
		if(executor!=null)
			log.debug("ThreadPoolTaskExecutor bean found processing soon will begin as per app trigger");
		
	}

	@Override
	public void onApplicationEvent(ContextClosedEvent event) {
		log.debug("ContextClosedHandler triggered by ContextClosedEvent");
			try {
				ThreadPoolExecutor threadPoolExecutor = this.executor.getThreadPoolExecutor();
				log.warn("Permitted up to " + ShutdownHookHandler.GRACEFUL_SHUTDOWN_WAIT_SECONDS + "s for active requests to complete");
				if (!threadPoolExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
					log.warn("Shutting Down application gracefully within " +ShutdownHookHandler.GRACEFUL_SHUTDOWN_WAIT_SECONDS +
							"s Proceeding with graceful shutdown") ;
					threadPoolExecutor.shutdownNow();
					if (!threadPoolExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
						log.error("Tomcat thread pool could not terminated");
					}
				} else {
					log.info("forcefully shutdown completed");
				}
			} catch (InterruptedException ex) {
				log.debug("ContextClosedHandler caught error" + ex);
				Thread.currentThread().interrupt();
			}
		}


}