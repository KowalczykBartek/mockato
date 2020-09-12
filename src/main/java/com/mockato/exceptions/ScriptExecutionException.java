package com.mockato.exceptions;

/**
 * Indicate exception occurred during script execution.
 */
public class ScriptExecutionException extends RuntimeException {
    public ScriptExecutionException(Exception rootCause) {
        super(rootCause);
    }
    public ScriptExecutionException(String msg) {
        super(msg);
    }
}
