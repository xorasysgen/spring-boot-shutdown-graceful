
package com.solum.app.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

public class ShutdownHealthChecker implements HealthIndicator, IProbeController {

	private static final Log log = LogFactory.getLog(ShutdownHealthChecker.class);
	public static final String GRACEFUL_SHUTDOWN_MSG = "Gracefulshutdown";

	private Health health;

	public ShutdownHealthChecker() {
		setReady(true);
	}

	public Health health() {
		return health;
	}

	@Override
	public void setReady(boolean ready) {
		if (ready) {
			health = new Health.Builder().withDetail(GRACEFUL_SHUTDOWN_MSG, "application up").up().build();
			log.debug(GRACEFUL_SHUTDOWN_MSG + " health status up");
		} else {
			health = new Health.Builder().withDetail(GRACEFUL_SHUTDOWN_MSG, "gracefully shutting down").down().build();
			log.debug(GRACEFUL_SHUTDOWN_MSG + " health status down");
		}
	}

}
