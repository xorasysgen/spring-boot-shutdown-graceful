

package com.solum.app.config;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.StringUtils;


public class ShutdownHookHandler implements Runnable {
    protected static final String GRACEFUL_SHUTDOWN_WAIT_SECONDS = "30";
    private static final String DEFAULT_GRACEFUL_SHUTDOWN_WAIT_SECONDS = "30";

    private static final Log log = LogFactory.getLog(ShutdownHookHandler.class);

    private final ConfigurableApplicationContext applicationContext;

    public ShutdownHookHandler(ConfigurableApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
    
    @Override
    public void run() {
    	log.debug("ShutdownHookHandler trigger by SIGTERM...");
    	setReadynessToFalse();
    	delayShutdownSpringContext();
    	shutdownSpringContext();
    	
    }


    private void shutdownSpringContext() {
        log.debug("Spring Application context being shutdown");
        applicationContext.close();
        log.debug("Shutdown completed, Spring Application context is shutdown");
    }

    //FIXME #network connection to put on hold during thread pause
    private void setReadynessToFalse() {
        log.debug("Incoming connection is being closed, Stopped accepting new HTTP requests");
        // applicationContext.refresh(); //illegal thread exception might occurred
        final Map<String, IProbeController> probeControllers = applicationContext.getBeansOfType(IProbeController.class);
        if (probeControllers.size() < 1) {
            log.error("Could not find a ProbeController Bean" + IProbeController.class.getName());
        }
        if (probeControllers.size() > 1) {
            log.warn("More than one ProbeController for Readyness-Check registered. " +
                    "Most probably one as Rest service and one in automatically configured as Actuator health check.");
        }
        
        for (IProbeController probeController : probeControllers.values()) {
        	log.warn("connection getting closed");
            probeController.setReady(false);
        }
    }

    
    private void delayShutdownSpringContext() {
        try {
            int shutdownWaitSeconds = getShutdownWaitSeconds();
            log.warn("Commencing graceful shutdown, allowing up to " +shutdownWaitSeconds+ "s for active process to complete!");
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