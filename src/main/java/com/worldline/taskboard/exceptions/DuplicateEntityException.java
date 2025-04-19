package com.worldline.taskboard.exceptions;

public class DuplicateEntityException extends RuntimeException {
    public DuplicateEntityException(String message, RuntimeException e) {
        super(message, e);
    }
}
