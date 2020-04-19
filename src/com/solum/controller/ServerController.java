package com.solum.controller;

import javax.annotation.PreDestroy;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.solum.app.exception.CommonException;

@RestController
@RequestMapping("/data")
public class ServerController {

	@GetMapping("/long-process")
	public String pause() {
		try {
			Thread.sleep(20000);
		} catch (InterruptedException e) {
			throw new CommonException("SERVICE_UNAVAILABLE, Server forcefully shutdown");
		}
		return "Even Though commencing graceful shutdown initiated, Process finished successfully..";
	}

	@PreDestroy
	public void destroy() {
		System.out.println("PreDestroy Method Triggered.");

		System.out.println("|||Pending active thread task completed|||");
	}

}
