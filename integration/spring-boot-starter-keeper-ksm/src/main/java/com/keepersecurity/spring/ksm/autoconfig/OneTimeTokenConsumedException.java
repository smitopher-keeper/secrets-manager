package com.keepersecurity.spring.ksm.autoconfig;

import lombok.experimental.StandardException;

/**
 * Exception thrown after a one-time token has been consumed.
 * <p>
 * This signals that the application should terminate and remove the
 * {@code keeper.ksm.one-time-token} property before restarting.
 */
@StandardException
public class OneTimeTokenConsumedException extends RuntimeException {
}

