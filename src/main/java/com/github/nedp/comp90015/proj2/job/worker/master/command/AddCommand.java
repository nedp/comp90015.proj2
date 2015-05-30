package com.github.nedp.comp90015.proj2.job.worker.master.command;

import com.github.nedp.comp90015.proj2.job.worker.master.JobManager;
import com.github.nedp.comp90015.proj2.job.worker.master.RemoteWorker;
import com.github.nedp.comp90015.proj2.job.worker.master.Worker;
import com.github.nedp.comp90015.proj2.job.worker.master.WorkerPool;
import org.jetbrains.annotations.NotNull;

import java.io.PrintStream;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * A Command for adding a new Worker.
 *
 * @author nedp
 */
class AddCommand implements Command {

    private static final String USAGE = "add hostname port";

    @NotNull
    private final String hostname;
    private final int port;

    private AddCommand(@NotNull String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }


    /**
     * (Attempts to) add a new {@link Worker} to the pool.
     * @param jobs  unused
     * @param workers  the {@link WorkerPool} to which the
     *                 Worker should be added.
     * @param out  the {@link PrintStream} to which output
     *             should be reported.
     * @return true if the Worker was successfully added,
     * and false if it already existed (according to #equals).
     */
    @Override
    public boolean runOn(@NotNull JobManager jobs,
                         @NotNull WorkerPool workers,
                         @NotNull PrintStream out) {

        // Add the worker and report success.
        final Worker worker = new RemoteWorker(this.hostname, this.port);
        final boolean ok = workers.add(worker);
        if (ok) {
            out.printf("Worker (%s) added.\n", worker.identifier());
            return true;
        } else {
            out.printf("Worker (%s) already added.\n", worker.identifier());
            return false;
        }
    }

    static class Factory implements CommandFactory {
        /**
         * Attempts to build an instance of AddCommand form parameters.
         * <p/>
         * Required parameters are:
         * <ol>
         *     <li>hostname: string specifying the Worker host address</li>
         *     <li>port: integer specifying the Worker port</li>
         * </ol>
         *
         * @param params  the Scanner providing the parameters.
         * @return an instance of AddCommand built from the parameters
         * if possible, otherwise a UsageCommand specifying correct usage.
         */
        @NotNull
        @Override
        public Command fromParams(Scanner params) {
            final String hostname;
            final int port;
            try {
                hostname = params.next();
                port = params.nextInt();
            } catch (NoSuchElementException e) {
                return new UsageCommand(AddCommand.USAGE);
            }
            return new AddCommand(hostname, port);
        }
    }
}
