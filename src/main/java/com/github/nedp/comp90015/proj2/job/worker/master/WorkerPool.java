package com.github.nedp.comp90015.proj2.job.worker.master;

import com.github.nedp.comp90015.proj2.job.Job;
import org.jetbrains.annotations.NotNull;

import java.lang.management.OperatingSystemMXBean;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private final List<Worker> workerList; // Must be a List for round robin.

    @NotNull
    private final Set<Worker> workerSet; // Must be a Set to avoid duplicates.

    private int iWorker = 0; // Current worker for round robin allocation.

    /**
     * Creates a WorkerPool storing {@link Worker}s in the supplied List.
     *
     * @param initialWorkers  a list of workers which should initially
     *                        be contained in the pool.
     */
    WorkerPool(@NotNull Collection<Worker> initialWorkers) {
        this.workerList = new ArrayList<>(initialWorkers);
        this.workerSet = new HashSet<>(initialWorkers);
    }

    /**
     * Allocates the specified {@link Job} to a {@link Worker} and
     * delegates execution of the Job to the Worker.
     * <p/>
     * Only RUNNING Workers (not DISCONNECTED) may have jobs
     * allocated to them.
     * If there are no such workers, an exception is thrown.
     *
     * @param job  the job to be executed, not null.
     * @return the {@link Result} of the job.
     * @throws WorkerUnavailableException if there are no
     * which the Job may be allocated to.
     */
    Result allocateAndExecute(Job job) throws WorkerUnavailableException {
        final int size = this.workerSet.size();

        // **Loop** through the workers.
        // Throw an exception if there are no workers.
        if (this.iWorker >= size) {
            if (size == 0) {
                throw new WorkerUnavailableException(
                    "tried to allocate a job in an empty WorkerPool" );
            }
            this.iWorker = 0;
        }

        // Choose the first RUNNING worker.
        int i = this.iWorker;
        Worker chosenWorker = this.workerList.get(i);
        while (chosenWorker.status() != WorkerStatus.RUNNING) {
            i += 1;
            if (i >= size) {
                i = 0;
            }
            // Throw an exception if we tried all workers.
            if (i == this.iWorker) {
                throw new WorkerUnavailableException(
                    "tried to allocate a job when all workers are DISCONNECTED" );
            }
            chosenWorker = this.workerList.get(i);
        }
        this.iWorker = i + 1;

        // Allocate to the chosen worker (round robin).
        final Result result = chosenWorker.execute(job);

        return result;
    }

    /**
     * Adds a new {@link Worker} to the pool, if it isn't already included.
     *
     * @param worker  the Worker to be added, not null.
     * @return true if the Worker was added to the pool,
     * and false if the Worker was already present.
     */
    boolean add(@NotNull Worker worker) {
        // Don't allow duplicate workers.
        if (this.workerSet.contains(worker)) {
            return false;
        }

        assert(this.workerSet.add(worker));
        assert(this.workerList.add(worker));
        return true;
    }

    /**
     * Retrieves a list of all {@link Worker}s in the pool.
     * <p/>
     * Includes known Workers regardless of their status.
     * Changing the returned list will not affect this WorkerPool.
     *
     * @return a new {@link List<Worker>} containing all the workers
     * known by this WorkerPool.
     */
    @NotNull
    List<Worker> workerList() {
        return new ArrayList<>(this.workerList);
    }
}
