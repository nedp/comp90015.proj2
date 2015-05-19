package com.github.nedp.comp90015.proj2.job.worker.master;

import com.github.nedp.comp90015.proj2.job.Job;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * TODO Created by nedp on 18/05/15.
 */
public class WorkerPool {
    @NotNull
    private final List<Worker> workers; // Must be a List for round robin.

    private int iWorker = 0;

    WorkerPool(List<Worker> initialWorkers) {
        this.workers = initialWorkers;
    }

    // TODO
    Result allocateAndExecute(Job job) throws WorkerUnavailableException {
        this.workers.get(iWorker).execute(job);
        this.iWorker += 1;
        if (this.iWorker >= this.workers.size()) {
            this.iWorker = 0;
        }
        return Result.FINISHED;
    }
}
