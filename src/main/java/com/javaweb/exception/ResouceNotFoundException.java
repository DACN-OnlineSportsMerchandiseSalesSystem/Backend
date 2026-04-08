package com.javaweb.exception;

public class ResouceNotFoundException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ResouceNotFoundException(String message) {
        super(message);
    }
}