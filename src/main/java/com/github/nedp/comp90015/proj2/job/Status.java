package java.com.github.nedp.comp90015.proj2.job;

/**
 * Enum Status
 * <p/>
 * A finite state machine for tracking Job status.
 * <p/>
 * Legal transitions:
 * <ul>
 *     <li>WAITING --(okay)--> RUNNING</li>
 *     <li>WAITING --(failure)--> FAILED</li>
 *     <li>RUNNING --(okay)--> FINISHED</li>
 *     <li>RUNNING --(failure)--> FAILED</li>
* </ul>
 *
 * @author nedp
 */
public enum Status {
    WAITING,
    RUNNING,
    FINISHED,
    FAILED,
    ;
    // DISCONNECTED is not the responsibility of this package.

    /**
     * Looks up the next state for the specified transition.
     *
     * @param didFail  whether or not there has been a failure
     *                 since reaching this status.
     * @throws IllegalStateException  if the transition specified
     *                                by @code{didFail} cannot occur.
     * @return the next state.
     */
    final Status nextState(boolean didFail) {
        if (didFail) {
            switch (this) {
                case WAITING:
                case RUNNING:
                    return FAILED;
                default:
                    // illegal
            }
        } else {
            switch (this) {
                case WAITING:
                    return RUNNING;
                case RUNNING:
                    return FINISHED;
                default:
                    // illegal
            }
        }

        throw new IllegalStateException(
                String.format("this: %s, didFail: %b", this.name(), didFail)
        );
    }
}
