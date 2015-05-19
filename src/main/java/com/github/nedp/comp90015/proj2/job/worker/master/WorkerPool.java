package com.github.nedp.comp90015.proj2.job.worker.master;

import com.github.nedp.comp90015.proj2.job.Job;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Manages a set of {@link Worker}s, and allocates {@link Job}s to them.
 * <p/>
 * In the current implementation, round robin allocation is used.
 * TODO swap to a more advanced allocation algorithm.
 *
 * @author nedp
 */
public class WorkerPool {
    @NotNull
    private final List<Worker> workers; // Must be a List for round robin.

    private int iWorker = 0;

    /**
     * Creates a WorkerPool storing {@link Worker}s in the supplied List.
     *
     * @param initialWorkers  the list to store workers in, not null,
     *                        may be populated.
     *                        The provided list, not a copy, is injected.
     */
    WorkerPool(@NotNull List<Worker> initialWorkers) {
        this.workers = initialWorkers;
    }

    /**
     * Allocates the specified {@link Job} to a {@link Worker} and
     * delegates execution of the Job to the Worker.
     *
     * @param job  the job to be executed, not null.
     * @return the {@link Result} of the job.
     * @throws WorkerUnavailableException if there are no workers to
     * allocate the Job to.
     */
    Result allocateAndExecute(Job job) throws WorkerUnavailableException {
        final int size = workers.size();
        // Loop through the workers.
        // Throw an exception if there are no workers.
        if (this.iWorker >= this.workers.size()) {
            if (size == 0) {
                throw new WorkerUnavailableException(
                    "tried to allocate a job in an empty WorkerPool" );
            }
            this.iWorker = 0;
        }
        // Allocate to the current worker (round robin).
        final Result result = this.workers.get(iWorker).execute(job);
        this.iWorker += 1;

        return result;
    }
}
