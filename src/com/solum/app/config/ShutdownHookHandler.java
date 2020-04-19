

package com.solum.app.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.StringUtils;

import java.util.Map;


public class ShutdownHookHandler implements Runnable {
    protected static final String GRACEFUL_SHUTDOWN_WAIT_SECONDS = "GRACEFUL_SHUTDOWN_WAIT_SECONDS";
    private static final String DEFAULT_GRACEFUL_SHUTDOWN_WAIT_SECONDS = "30";

    private static final Log log = LogFactory.getLog(ShutdownHookHandler.class);

    private final ConfigurableApplicationContext applicationContext;

    public ShutdownHookHandler(ConfigurableApplicationContext applicationContext) {
    	log.debug("application context received");
        this.applicationContext = applicationContext;
    }
    
    @Override
    public void run() {
    	System.out.println("running...");
    	setReadynessToFalse();
    	delayShutdownSpringContext();
    	shutdownSpringContext();
    	
    }


    private void shutdownSpringContext() {
        log.info("Spring Application context starting to shutdown");
        applicationContext.close();
        log.info("Spring Application context is shutdown");
    }

    private void setReadynessToFalse() {
        log.debug("Incoming connection is being closed, Stopped accepting new HTTP requests");
        //applicationContext.refresh(); illegal thread exception might occurred
        final Map<String, IProbeController> probeControllers = applicationContext.getBeansOfType(IProbeController.class);
        if (probeControllers.size() < 1) {
            log.error("Could not find a ProbeController Bean" + IProbeController.class.getName());
        }
        if (probeControllers.size() > 1) {
            log.warn("More than one ProbeController for Readyness-Check registered. " +
                    "Most probably one as Rest service and one in automatically configured as Actuator health check.");
        }
        
        for (IProbeController probeController : probeControllers.values()) {
            probeController.setReady(false);
        }
    }

    
    private void delayShutdownSpringContext() {
        try {
            int shutdownWaitSeconds = getShutdownWaitSeconds();
            log.warn("Commencing graceful shutdown, allowing up to " +shutdownWaitSeconds+ "s for active requests to completet!");
            Thread.sleep(shutdownWaitSeconds * 1000);
        } catch (InterruptedException e) {
            log.error("Error while gracefulshutdown Thread.sleep", e);
        }
    }

    private int getShutdownWaitSeconds() {
        String waitSeconds = System.getProperty(GRACEFUL_SHUTDOWN_WAIT_SECONDS);
        if (StringUtils.isEmpty(waitSeconds)) {
          waitSeconds = applicationContext.getEnvironment().getProperty(GRACEFUL_SHUTDOWN_WAIT_SECONDS, DEFAULT_GRACEFUL_SHUTDOWN_WAIT_SECONDS);
        }
        return Integer.parseInt(waitSeconds);
    }


}