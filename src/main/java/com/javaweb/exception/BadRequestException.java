package com.javaweb.exception;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(formatMessage(message));
    }
    
    private static String formatMessage(String message) {
        if (message == null || message.isEmpty()) {
            return "Invalid request";
        }
        if ("password".equals(message)) return "Password is too short";
        return message + " already exists";
    }   
}