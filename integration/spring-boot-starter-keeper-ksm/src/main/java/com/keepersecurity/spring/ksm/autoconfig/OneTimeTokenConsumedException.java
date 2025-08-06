package com.keepersecurity.spring.ksm.autoconfig;

/**
 * Exception thrown after a one-time token has been consumed.
 *
 * <p>This signals that the application should terminate and remove the {@code
 * keeper.ksm.one-time-token} property before restarting.
 */
public class OneTimeTokenConsumedException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  /** Creates a new exception with no detail message or cause. */
  public OneTimeTokenConsumedException() {}

  /**
   * Creates a new exception with the specified detail message.
   *
   * @param message detail message
   */
  public OneTimeTokenConsumedException(String message) {
    super(message);
  }

  /**
   * Creates a new exception with the specified cause.
   *
   * @param cause underlying cause
   */
  public OneTimeTokenConsumedException(Throwable cause) {
    super(cause);
  }

  /**
   * Creates a new exception with the specified detail message and cause.
   *
   * @param message detail message
   * @param cause underlying cause
   */
  public OneTimeTokenConsumedException(String message, Throwable cause) {
    super(message, cause);
  }
}
