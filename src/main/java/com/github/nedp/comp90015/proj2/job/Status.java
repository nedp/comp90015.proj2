package com.github.nedp.comp90015.proj2.job;

/**
 * Enum Status
 * describes states and transitions for tracking {@link Job} status.
 * <p>
 * Legal transitions:
 * <ul>
 * <li>WAITING --(ok)--> RUNNING</li>
 * <li>WAITING --(not ok)--> FAILED</li>
 * <li>RUNNING --(ok)--> FINISHED</li>
 * <li>RUNNING --(not ok)--> FAILED</li>
 * </ul>
 *
 * @author nedp
 */
public enum Status {
  WAITING,
  RUNNING,
  FINISHED,
  FAILED,;
  // DISCONNECTED is not the responsibility of this package.

  /**
   * Looks up the next state for the specified transition.
   *
   * @param ok false if there has been a failure, otherwise true.
   * @return the next state.
   * @throws IllegalStateException if the transition specified
   *                               by @code{ok} cannot occur.
   */
  public final Status nextState(boolean ok) {
    if (ok) {
      // WAITING --(ok)--> RUNNING
      // RUNNING --(ok)--> FINISHED
      switch (this) {
        case WAITING:
          return RUNNING;
        case RUNNING:
          return FINISHED;
        default:
          // illegal
      }
    } else {
      // WAITING --(not ok)--> FAILED
      // RUNNING --(not ok)--> FAILED
      switch (this) {
        case WAITING: // fallthrough
        case RUNNING:
          return FAILED;
        default:
          // illegal
      }
    }

    throw new IllegalStateException(String.format("this: %s, ok: %b", this.name(), ok));
  }
}
