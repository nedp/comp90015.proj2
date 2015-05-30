package com.github.nedp.comp90015.proj2.job.worker.master;

import com.github.nedp.comp90015.proj2.job.Job;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;

/**
 * A Worker Pool which prefers to allocate to low-load Workers.
 * <p/>
 * TODO refactor to use composition instead of inheritance.
 * Allocation should be handled by separate object, which
 * should be a member of WorkerPool.
 *
 * @author nedp
 */
public class LoadBalancer extends WorkerPool {
    /**
     * Populates a new LoadBalancer with the supplied {@link Worker}s.
     * <p/>
     * The supplied list will be forwarded to
     * {@link WorkerPool}'s constructor.
     *
     * @param initialWorkers  a list of workers which should initially
     *                        be contained in the pool.
     */
    public LoadBalancer(@NotNull Collection<Worker> initialWorkers) {
        super(initialWorkers);
    }

    /**
     * Creates a WorkerPool with no initial {@link Worker}s.
     */
    public LoadBalancer() {
        super();
    }

    /**
     * Chooses a suitable {@link Worker} for {@link Job} allocation.
     * <p/>
     * This implementation uses load balancing allocation.
     * Workers with more free memory are allocated new Jobs first.
     *
     * @param job  the Job to be allocated.
     * @return Optional.of(the chosen Worker),
     * or Optional.empty() if there is no such worker.
     */
    protected synchronized Optional<Worker> workerFor(@NotNull Job job) {
        final int size = this.workerSet.size();
        if (size == 0) {
            return Optional.empty();
        }
        Optional<Worker> chosenWorker = Optional.empty();
        long mostFreeMemory = 0;

        // Find the worker with the greatest amount of free memory.
        // Only allow RUNNING workers.
        for (Worker worker : this.workerSet) {
            if (worker.status() != Worker.Status.RUNNING) {
                continue;
            }
            final long freeMemory = worker.freeMemory();
            if (freeMemory > mostFreeMemory) {
                chosenWorker = Optional.of(worker);
                mostFreeMemory = freeMemory;
            }
        }
        return chosenWorker;
    }
}
