package com.github.nedp.comp90015.proj2.job.worker.master;

import com.github.nedp.comp90015.proj2.job.Job;
import org.jetbrains.annotations.NotNull;

/**
 * Interface for a remote Worker.
 * <p/>
 * Each implementor instance should connect
 * to a single remote worker.
 *
 * @author nedp
 */
interface Worker {
    /**
     * Executes a {@link Job} at the remote Worker.
     * <p/>
     * This method must return only when either:
     * <ul>
     *     <li>The worker reports that the {@link Job} has finished or
     *     failed, and sends the appropriate output files.
     *     In this case, the {@link Job}'s `out` and `log` files must
     *     match those produced by the worker.</li>
     *
     *     <li>The worker disconnects.
     *     In this case, no files must be synchronised.</li>
     * </ul>
     *
     * @param job the {@link Job} to be run by the worker
     */
    @NotNull
    Result execute(Job job);
}
