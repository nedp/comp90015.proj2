package com.github.nedp.comp90015.proj2.job.worker.master;

import org.jetbrains.annotations.NotNull;

import java.io.Console;
import java.util.Optional;
import java.util.Scanner;

/**
 * Created by nedp on 21/05/15.
 */
enum Command {
    ADD_WORKER {
        @Override
        boolean runFor(@NotNull JobManager jobs,
                       @NotNull WorkerPool workers,
                       @NotNull Scanner params,
                       @NotNull Console console) {
            if (!params.hasNext()) {
                return false;
            }
            final String hostname = params.next();
            if (!params.hasNext()) {
                return false;
            }
            final int port = params.nextInt();
            workers.add(new RemoteWorker(hostname, port));
            console.printf("Worker (%s:%d) added.\n", hostname, port);
            return true;
        }
    },
    LIST_WORKERS {
        @Override
        boolean runFor(@NotNull JobManager jobs,
                       @NotNull WorkerPool workers,
                       @NotNull Scanner params,
                       @NotNull Console console) {
            console.printf("ID - Hostname:Port is Status\n");
            for (Worker worker : workers.workerList()) {
                console.printf("%s is %s", worker.identifier(), worker.status().name());
            }
            return true;
        }
    },
    SUBMIT_JOB {
        @Override
        boolean runFor(@NotNull JobManager jobs,
                       @NotNull WorkerPool workers,
                       @NotNull Scanner params,
                       @NotNull Console console) {
            // TODO
            return false;
        }
    },
    JOB_STATUS {
        @Override
        boolean runFor(@NotNull JobManager jobs,
                       @NotNull WorkerPool workers,
                       @NotNull Scanner params,
                       @NotNull Console console) {
            // TODO
            return false;
        }
    },;

    abstract boolean runFor(@NotNull JobManager jobs,
                            @NotNull WorkerPool workers,
                            @NotNull Scanner params,
                            @NotNull Console console);

    @NotNull
    static Optional<Command> fromWord(@NotNull String word) {
        switch (word) {
            case "add":
            case "addw":
            case "aw":
            case "a":
            case "w":
                return Optional.of(ADD_WORKER);

            case "list":
            case "ls":
            case "listw":
            case "lsw":
            case "lw":
            case "l":
                return Optional.of(LIST_WORKERS);

            case "submit":
            case "job":
            case "sjob":
            case "j":
                return Optional.of(SUBMIT_JOB);

            case "status":
            case "jobstatus":
            case "stat":
            case "jobstat":
            case "s":
                return Optional.of(JOB_STATUS);

            default:
                return Optional.empty();
        }
    }
}
