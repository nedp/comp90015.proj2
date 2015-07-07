package com.github.nedp.comp90015.proj2.job.worker.master.command;

import com.github.nedp.comp90015.proj2.job.worker.master.JobManager;
import com.github.nedp.comp90015.proj2.job.worker.master.Worker;
import com.github.nedp.comp90015.proj2.job.worker.master.WorkerPool;
import org.jetbrains.annotations.NotNull;

import java.io.PrintStream;
import java.util.Scanner;

/**
 * A Command for listing all Workers.
 *
 * @author nedp
 */
class ListCommand implements Command {

  private ListCommand() {
  }

  @Override
  public boolean runOn(@NotNull JobManager jobs,
                       @NotNull WorkerPool workers,
                       @NotNull PrintStream out) {
    // Identify and report the status of each worker.
    out.printf("ID - Hostname:Port is Status\n");
    for (Worker worker : workers.workerList()) {
      out.printf("%s is %s\n", worker.identifier(), worker.status().name());
    }
    return true;
  }

  static class Factory implements CommandFactory {
    /**
     * Attempts to build an instance of ListCommand from parameters.
     * <p>
     * No parameters are required.
     *
     * @param params the Scanner providing the parameters.
     * @return an instance of ListCommand built from the parameters
     * if possible, otherwise a UsageCommand specifying correct usage.
     */
    @NotNull
    @Override
    public Command fromParams(Scanner params) {
      return new ListCommand();
    }
  }
}
