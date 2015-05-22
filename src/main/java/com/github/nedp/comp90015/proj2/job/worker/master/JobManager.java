package com.github.nedp.comp90015.proj2.job.worker.master;

import com.github.nedp.comp90015.proj2.job.Job;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
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
    private final List<JobResult> jobResults;
    @NotNull
    private final WorkerPool pool;

    /**
     * Creates a new JobManager with the specified WorkerPool and Map instances.
     * <p/>
     * These instances may be pre-initialised before being injected.
     *
     * @param pool should be ready to allocate Jobs, not null
     */
    JobManager(@NotNull WorkerPool pool) {
        this.pool = pool;
        this.jobResults = new ArrayList<>();
    }

    /**
     * Submits a {@link Job} for subsequent execution.
     *
     * @param job the Job to be executed, not null
     * @return the unique (within this JobManager)
     * positive integer id of the submitted Job.
     */
    public synchronized int submit(@NotNull Job job) {
        final int id = this.jobResults.size();
        final boolean ok = this.jobResults.add(new JobResult(job, Optional.empty()));
        assert(ok);
        return id;
    }

    /**
     * Executes the specified {@link Job} via this' {@link WorkerPool}.
     * <p/>
     * Delegates Worker allocation and execution of the Job to
     * this' WorkerPool, returning and storing the {@link Result}.
     * The method will return when the job terminates.
     * It has unspecified behaviour when called twice for the same Job.
     *
     * @param id  the unique integer id of the Job to be executed.
     * @return the Result of the Job, after it has executed
     * @throws WorkerUnavailableException when this' WorkerPool has no
     *                                    available Workers.
     */
    @NotNull
    public Result execute(int id) throws WorkerUnavailableException {
        final Job job = this.jobResults.get(id).job;
        final Result result = this.pool.allocateAndExecute(job);

        assert(!this.jobResults.get(id).result.isPresent());
        this.jobResults.set(id, new JobResult(job, Optional.of(result)));
        return result;
    }

    /**
     * Returns the {@link Result} of the specified {@link Job}.
     *
     * @param id  the integer id of the Job of interest.
     * @return Optional.of(Result) if the Job has terminated,
     *         otherwise Optional.empty
     * @throws IndexOutOfBoundsException if the job is not tracked.
     */
    @NotNull
    public Optional<Result> resultOf(int id) {
        final JobResult jobResult = this.jobResults.get(id);
        if (jobResult == null) {
            throw new IndexOutOfBoundsException(String.format(
                "no job with the requested id (%d) is tracked", id));
        } else {
            return jobResult.result;
        }
    }

    private class JobResult {
        @NotNull private final Job job;
        @NotNull private final Optional<Result> result;

        private JobResult(@NotNull Job job, @NotNull Optional<Result> result) {
            this.job = job;
            this.result = result;
        }
    }
}
