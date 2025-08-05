package com.keepersecurity.spring.ksm.autoconfig;

/**
 * Exception thrown after a one-time token has been consumed.
 * <p>
 * This signals that the application should terminate and remove the
 * {@code keeper.ksm.one-time-token} property before restarting.
 */
public class OneTimeTokenConsumedException extends RuntimeException {

    /**
     * Creates a new exception indicating a one-time token has already been used.
     *
     * @param message detail message describing the condition
     */
    public OneTimeTokenConsumedException(String message) {
        super(message);
    }
}

