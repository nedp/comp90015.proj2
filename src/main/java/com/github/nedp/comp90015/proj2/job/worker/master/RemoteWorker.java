package com.github.nedp.comp90015.proj2.job.worker.master;

import com.github.nedp.comp90015.proj2.job.Job;
import org.jetbrains.annotations.NotNull;

/**
 * TODO
 * Created by nedp on 21/05/15.
 */
public class RemoteWorker implements Worker {
    public RemoteWorker(@NotNull String hostname, int port) {
        // TODO
    }

    @NotNull
    @Override
    public Result execute(Job job) {
        return null; // TODO
    }

    @NotNull
    @Override
    public WorkerStatus status() {
        return null; // TODO
    }

    @Override
    public double cpuLoad() {
        return 0; // TODO
    }

    @Override
    public long freeMemory() {
        return 0; // TODO
    }

    @Override
    public String identifier() {
        return null; // TODO
    }
}
