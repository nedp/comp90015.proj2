package com.github.nedp.comp90015.proj2.job.worker.master.command;

import com.github.nedp.comp90015.proj2.job.worker.master.JobManager;
import com.github.nedp.comp90015.proj2.job.worker.master.WorkerPool;
import org.jetbrains.annotations.NotNull;

import java.io.PrintStream;
import java.util.*;

/**
 * Provides the various commands a user may issue via the command line.
 * <p/>
 * Provided commands include:
 * <ul>
 *     <li>add - add a new worker</li>
 *     <li>list - list known workers</li>
 *     <li>submit - submit and attempt to run a job</li>
 *     <li>status - retrieve the status of a particular job</li>
 * </ul>
 *
 * @author nedp
 */
interface Command {

    boolean runOn(@NotNull JobManager jobs,
                  @NotNull WorkerPool workers,
                  @NotNull PrintStream out);


    enum Type {
        ADD_WORKER,
        LIST_WORKERS,
        SUBMIT_JOB,
        JOB_STATUS,
        ;
        @NotNull
        static Optional<Type> FromName(@NotNull String word) {
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

}
