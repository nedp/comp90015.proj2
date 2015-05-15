package com.github.nedp.comp90015.proj2.job.worker.master;

import com.github.nedp.comp90015.proj2.job.Job;
import org.jetbrains.annotations.NotNull;

/**
 * Interface for sending a {@link Job} to a
 * remote worker for running.
 * <p/>
 * Each implementor should be connected to a
 * single remote worker.
 *
 * @author nedp
 */
interface JobSender {
    /**
     * Sends a {@link Job} to a remote worker, to be run by that Worker.
     * <p/>
     * This method must return only when either:
     * <ul>
     *     <li>The worker disconnects.
     *     In this case, no files must be synchronised.</li>
     *
     *     <li>The worker reports that the {@link Job} has finished or
     *     failed, and sends the appropriate output files.
     *     In this case, the {@link Job}'s `out` and `log` files must
     *     match those produced by the worker.</li>
     * </ul>
     *
     * @param job the {@link Job} to be run by the worker
     */
    @NotNull
    Result send(Job job);
}
