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
     * Jobs may only be executed once.
     *
     * @param id  the unique integer id of the Job to be executed,
     *            must be for a Job which is not already allocated.
     * @return the Result of the Job, after it has executed
     * @throws WorkerUnavailableException when this' WorkerPool has
     * no available Workers.
     * In this case, the call may subsequently be repeated.
     */
    @NotNull
    public Result execute(int id) throws WorkerUnavailableException {
        return this.jobResults.get(id).execute(this.pool);
    }

    /**
     * Returns the {@link Result} of the specified {@link Job}.
     * <p/>
     * Not synchronized, read only.
     *
     * @param id  the integer id of the Job of interest,
     *            must correspond to a tracked Job.
     * @return Optional.of(Result) if the Job has terminated,
     *         otherwise Optional.empty
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

    /**
     * Reports whether the {@link Job} with the specified id has been allocated.
     * <p/>
     * Not synchronised, read only.
     *
     * @param id  the integer id of the Job of interest,
     *            must correspond to a tracked Job.
     * @return true if the Job has been allocated, otherwise false.
     */
    public boolean hasAllocated(int id) {
        final JobResult jobResult = this.jobResults.get(id);
        if (jobResult == null) {
            throw new IndexOutOfBoundsException(String.format(
                "no job with the requested id (%d) is tracked", id));
        } else {
            return this.jobResults.get(id).hasBeenAllocated;
        }
    }

    private static class JobResult {
        @NotNull private final Job job;
        @NotNull private Optional<Result> result;
        private boolean hasBeenAllocated = false;

        private JobResult(@NotNull Job job, @NotNull Optional<Result> result) {
            this.job = job;
            this.result = result;
        }

        /*
         * Ensure that the job hasn't been allocated,
         * then allocate and execute it via the WorkerPool.
         * Synchronised to enforce run-once semantics.
         */
        private synchronized Result execute(WorkerPool pool) throws WorkerUnavailableException {
            if (this.hasBeenAllocated) {
                throw new IllegalStateException("tried to allocate a job twice");
            }
            final Result result = pool.allocateAndExecute(this.job);
            this.result = Optional.of(result);
            this.hasBeenAllocated = true;
            return result;
        }
    }
}
