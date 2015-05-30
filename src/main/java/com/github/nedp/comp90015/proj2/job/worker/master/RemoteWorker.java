package com.github.nedp.comp90015.proj2.job.worker.master;

import com.github.nedp.comp90015.proj2.job.Job;
import org.jetbrains.annotations.NotNull;

/**
 * TODO
 * Created by nedp on 21/05/15.
 */
public class RemoteWorker implements Worker {
    private final String hostname;
    private final int port;

    public RemoteWorker(@NotNull String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
        // TODO
    }

    @NotNull
    @Override
    public Result execute(Job job) {
        return Result.DISCONNECTED; // TODO
    }

    @NotNull
    @Override
    public Status status() {
        return Status.DISCONNECTED; // TODO
    }

    @Override
    public double cpuLoad() {
        return 0; // TODO NOT NEEDED
    }

    @Override
    public long freeMemory() {
        return 0; // TODO
    }

    @NotNull
    @Override
    public String identifier() {
        return String.format("Worker Stub %s:%d", this.hostname, this.port); // TODO
    }
}
