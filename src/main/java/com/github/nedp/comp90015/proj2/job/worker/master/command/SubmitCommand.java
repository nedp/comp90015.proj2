package com.github.nedp.comp90015.proj2.job.worker.master.command;

import com.github.nedp.comp90015.proj2.job.Job;
import com.github.nedp.comp90015.proj2.job.StatusTracker;
import com.github.nedp.comp90015.proj2.job.worker.master.JobManager;
import com.github.nedp.comp90015.proj2.job.worker.master.Result;
import com.github.nedp.comp90015.proj2.job.worker.master.WorkerPool;
import com.github.nedp.comp90015.proj2.job.worker.master.WorkerUnavailableException;
import org.jetbrains.annotations.NotNull;

import java.io.PrintStream;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * A Command for submitting a new Job.
 *
 * @author nedp
 */
class SubmitCommand implements Command {

    private static final String USAGE =
        "submit jarFile inFile outFile logFile memLimit timeout"
            + "\n\tfiles: string path to file"
            + "\n\tmemLimit: integer number of MBs"
            + "\n\ttimeout: integer number of seconds";

    @NotNull
    private final Job.Files files;
    private final int memoryLimit;
    private final int timeout;

    private SubmitCommand(@NotNull Job.Files files, int memoryLimit, int timeout) {
        this.files = files;
        this.memoryLimit = memoryLimit;
        this.timeout = timeout;
    }

    /**
     * Submits and attempts to run add a new {@link Job}.
     * <p/>
     * A new {@link Thread} will be created for Job execution.
     *
     * @param jobs  the {@link JobManager} to which to submit the new Job.
     * @param workers  not used.
     * @param out  the {@link PrintStream} to which output
     *             should be reported.
     * @return true
     */
    @Override
    public boolean runOn(@NotNull JobManager jobs,
                         @NotNull WorkerPool workers,
                         @NotNull PrintStream out) {
        // Submit the job.
        final StatusTracker tracker = new StatusTracker();
        final int id = jobs.submit(new Job(this.files, tracker, this.memoryLimit, this.timeout));
        out.printf("Submitted the job with id %d\n", id);

        // Execute the job in a new thread.
        final Thread thread = new Thread(() -> ExecuteJob(jobs, id, out));
        thread.setDaemon(true);
        thread.start();
        return true;
    }

    private static void ExecuteJob(@NotNull JobManager jobs,
                                   int id,
                                   @NotNull PrintStream out) {
        final Result result;
        try {
            result = jobs.execute(id);
        } catch (WorkerUnavailableException e) {
            out.printf("No Workers available to execute job %d (%s).\n", id, jobs.nameOf(id));
            return;
        }
        final Job.Files files = jobs.filesOf(id);
        out.printf("Job %d (%s) terminated with status: %s\noutput file: %s\nlog file: %s\n",
            id, jobs.nameOf(id), result.name(), files.out, files.log);
    }

    static class Factory implements CommandFactory  {
        /**
         * Attempts to build an instance of SubmitCommand from parameters.
         * <p/>
         * Required parameters are:
         * <ol>
         *     <li>jarFile: string containing path to file</li>
         *     <li>inFile: string containing path to file</li>
         *     <li>outFile: string containing path to file</li>
         *     <li>logFile: string containing path to file</li>
         * </ol>
         * Additional optional parameters are:
         * <ol>
         *     <li>memoryLimit: integer number of MB</li>
         *     <li>timeout: integer number of seconds</li>
         * </ol>
         *
         * @param params  the Scanner providing the parameters.
         * @return an instance of SubmitCommand built from the parameters
         * if possible, otherwise a UsageCommand specifying correct usage.
         */
        @NotNull
        @Override
        public Command fromParams(Scanner params) {
            final Job.Files files;
            final int memoryLimit;
            final int timeout;
            try {
                files = Utilities.GetFiles(params);
                memoryLimit = Utilities.NextIntOr(params, Job.NO_LIMIT);
                timeout = Utilities.NextIntOr(params, Job.NO_TIMEOUT);
            } catch (NoSuchElementException e) {
                return new UsageCommand(SubmitCommand.USAGE);
            }
            return new SubmitCommand(files, memoryLimit, timeout);
        }
    }
}
