package com.github.nedp.comp90015.proj2.job.worker.master.command;

import com.github.nedp.comp90015.proj2.job.worker.master.JobManager;
import com.github.nedp.comp90015.proj2.job.worker.master.WorkerPool;
import org.jetbrains.annotations.NotNull;

import java.io.PrintStream;

/**
 * Provides the various commands a user may issue via the command line.
 * <p>
 * Provided commands include:
 * <ul>
 * <li>add - add a new worker</li>
 * <li>list - list known workers</li>
 * <li>submit - submit and attempt to run a job</li>
 * <li>status - retrieve the status of a particular job</li>
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
    JOB_STATUS,;
  }
}
