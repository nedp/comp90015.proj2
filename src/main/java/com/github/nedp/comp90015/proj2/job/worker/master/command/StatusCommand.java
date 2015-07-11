package com.github.nedp.comp90015.proj2.job.worker.master.command;

import com.github.nedp.comp90015.proj2.job.Job;
import com.github.nedp.comp90015.proj2.job.worker.master.JobManager;
import com.github.nedp.comp90015.proj2.job.worker.master.Result;
import com.github.nedp.comp90015.proj2.job.worker.master.WorkerPool;
import org.jetbrains.annotations.NotNull;

import java.io.PrintStream;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Scanner;

/**
 * A Command for reporting the status of a Job.
 *
 * @author nedp
 */
class StatusCommand implements Command {

  private static final String USAGE = "status jobID";

  private final int id;

  private StatusCommand(int id) {
    this.id = id;
  }

  /**
   * Manually reports on the status of the specified Job.
   * <p>
   * The Job reported on is the one with the id submitted
   * as a parameter to this Command.
   *
   * @param jobs the {@link JobManager} which is managing the {@link Job}.
   * @param workers not used.
   * @param out the {@link PrintStream} to which output
   * should be reported.
   * @return true if the status is able to be reported,
   * and false if the job with the specified id is not tracked.
   */
  @Override
  public boolean runOn(@NotNull JobManager jobs,
                       @NotNull WorkerPool workers,
                       @NotNull PrintStream out) {
    // Print the status of the job.
    final String jobName;
    final Optional<Result> result;
    try {
      jobName = jobs.nameOf(id);
      result = jobs.resultOf(id);
    } catch (IndexOutOfBoundsException e) {
      out.printf("No Job is tracked with id %d\n", id);
      return false;
    }

    if (result.isPresent()) {
      out.printf("Job %d (%s) is %s\n", id, jobName, result.get());
    } else {
      if (jobs.hasAllocated(id)) {
        out.printf("Job %d (%s) is RUNNING\n", id, jobName);
      } else {
        out.printf("Job %d (%s) is UNALLOCATED\n", id, jobName);
      }
    }
    return true;
  }

  static class Factory implements CommandFactory {
    /**
     * Attempts to build an instance of StatusCommand from parameters.
     * <p>
     * Required parameters are:
     * <ol>
     * <li>id: integer</li>
     * </ol>
     *
     * @param params the Scanner providing the parameters.
     * @return an instance of StatusCommand built from the parameters
     * if possible, otherwise a UsageCommand specifying correct usage.
     */
    @NotNull
    @Override
    public Command fromParams(Scanner params) {
      final int id;
      try {
        id = params.nextInt();
      } catch (NoSuchElementException e) {
        return new UsageCommand(StatusCommand.USAGE);
      }
      return new StatusCommand(id);
    }

  }
}
