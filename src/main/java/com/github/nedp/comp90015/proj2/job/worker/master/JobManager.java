package com.github.nedp.comp90015.proj2.job.worker.master;

import com.github.nedp.comp90015.proj2.job.Job;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;

/**
 * Delegates Jobs to a WorkerPool, recording their Results.
 * <p/>
 * Uses the injected {@link WorkerPool} to allocate {@link Job}s to workers.
 * Uses the injected {@link Map} instance to associate Jobs with their
 * {@link Result}s.
 * <p/>
 * Offers no concurrency guarantees.
 *
 * @author nedp
 */
public class JobManager {
    @NotNull
    private final Map<Job, Optional<Result>> jobResults;
    @NotNull
    private final WorkerPool pool;

    /**
     * Creates a new JobManager with the specified WorkerPool and Map instances.
     * <p/>
     * These instances may be pre-initialised before being injected.
     *
     * @param pool should be ready to allocate Jobs, not null
     * @param jobResults should be ready to store Job/Result pairs, not null
     */
    JobManager(@NotNull WorkerPool pool, @NotNull Map<Job, Optional<Result>> jobResults) {
        this.pool = pool;
        this.jobResults = jobResults;
    }

    /**
     * Executes the specified {@link Job} via this' {@link WorkerPool}.
     * <p/>
     * Delegates Worker allocation and execution of the Job to
     * this' WorkerPool, returning and storing the Result.
     * The method will return when the job terminates.
     * It has unspecified behaviour when called twice for the same Job.
     *
     * @param job the Job to be executed, not null
     * @return the {@link Result} of the Job
     * @throws WorkerUnavailableException when this' WorkerPool has no
     *                                    available Workers.
     */
    @NotNull
    public Result execute(@NotNull Job job) throws WorkerUnavailableException {
        this.jobResults.put(job, Optional.empty());
        final Result result = this.pool.allocateAndSend(job);
        assert(Optional.empty().equals(
            this.jobResults.replace(job, Optional.of(result))));
        return result;
    }
}
