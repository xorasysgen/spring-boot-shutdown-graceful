package com.solum.app.exception;

public class CommonException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;

	public CommonException(String message){
		super(message);
	}
}