package com.github.nedp.comp90015.proj2.job;

import org.jetbrains.annotations.NotNull;

/**
 * A threadsafe interface for a finite state machine tracking
 * {@link Job} status.
 * <p/>
 * Wraps the states and transitions defined in {@link Status}.
 * Supports operations in the following order:
 * <ol>
 *     <li>{@link #start}</li>
 *     <li>{@link #finish}</li>
 * </ol><p/>
 * {@link #current} may be called at any time.
 *
 * @author nedp
 */
@SuppressWarnings("WeakerAccess") // TODO Dependents aren't implemented yet.
public class StatusTracker {
    private Status status = Status.WAITING;

    public StatusTracker() {}

    /**
     * Triggers transition from WAITING to RUNNING.
     */
    synchronized void start() {
        assert(this.status == Status.WAITING);
        final boolean OK = true;
        this.status = this.status.nextState(OK);
    }

    /**
     * Triggers a transition from RUNNING to FINISHED, or FAILED,
     * depending on {@code ok}.
     *
     * @param ok  passed to {@link Status#nextState}.
     */
    synchronized void finish(boolean ok) {
        assert(this.status == Status.RUNNING);
        this.status = this.status.nextState(ok);
    }

    /**
     * Retrieves the currently tracked status.
     *
     * @return the status
     */
    @NotNull
    synchronized Status current() {
        return this.status;
    }
}
