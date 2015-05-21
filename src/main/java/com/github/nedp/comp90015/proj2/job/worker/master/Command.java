package com.github.nedp.comp90015.proj2.job.worker.master;

import com.github.nedp.comp90015.proj2.job.Job;
import com.github.nedp.comp90015.proj2.job.StatusTracker;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Scanner;

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
enum Command {
    ADD_WORKER {
        static final String USAGE =
            "Invalid arguments. Correct usage is:\n"
            + "> add hostname port\n";

        @Override
        boolean runFor(@NotNull JobManager jobs,
                       @NotNull WorkerPool workers,
                       @NotNull Scanner params,
                       @NotNull String prompt) {
            // Parse params.
            final String hostname;
            final int port;
            try {
                hostname = params.next();
                port = params.nextInt();
            } catch (NoSuchElementException e) {
                System.out.printf(USAGE);
                return false;
            }

            // Add the worker and report success.
            final boolean ok = workers.add(new RemoteWorker(hostname, port));
            if (ok) {
                System.out.printf("Worker (%s:%d) added.\n", hostname, port);
                return true;
            } else {
                System.out.printf("Failed to add Worker (%s:%d).\n", hostname, port);
                return false;
            }
        }
    },
    LIST_WORKERS {
        @Override
        boolean runFor(@NotNull JobManager jobs,
                       @NotNull WorkerPool workers,
                       @NotNull Scanner params,
                       @NotNull String prompt) {
            // Identify and report the status of each worker.
            System.out.printf("ID - Hostname:Port is Status\n");
            for (Worker worker : workers.workerList()) {
                System.out.printf("%s is %s\n", worker.identifier(), worker.status().name());
            }
            return true;
        }
    },
    SUBMIT_JOB {
        static final String USAGE =
            "Invalid arguments. Correct usage is:\n"
            + "> submit jarFile inFile outFile logFile memLimit timeout\n"
            + "^~~(memLimit MBs, timeout in seconds; both whole numbers)\n";

        @Override
        boolean runFor(@NotNull JobManager jobs,
                       @NotNull WorkerPool workers,
                       @NotNull Scanner params,
                       @NotNull String prompt) {
            // Parse params.
            final Job.Files files;
            final int memLimit;
            final int timeout;
            try {
                final File jarFilename = new File(params.next());
                final File inFilename = new File(params.next());
                final File outFilename = new File(params.next());
                final File logFilename = new File(params.next());
                files = new Job.Files(jarFilename, inFilename, outFilename, logFilename);
                memLimit = params.nextInt();
                timeout = params.nextInt();
            } catch (NoSuchElementException e) {
                System.out.printf(USAGE);
                return false;
            }
            // Submit the job.
            final StatusTracker tracker = new StatusTracker();
            final int id = jobs.submit(new Job(files, tracker, memLimit, timeout));
            System.out.printf("Submitted the job with id %d\n", id);

            // Execute the job in a new thread.
            final Thread thread = new Thread(() -> executeJob(jobs, files, id, prompt));
            thread.setDaemon(true);
            thread.start();
            return true;
        }
    },
    JOB_STATUS {
        static final String USAGE =
            "Invalid arguments. Correct usage is:\n"
            + "> status jobID\n";
        @Override
        boolean runFor(@NotNull JobManager jobs,
                       @NotNull WorkerPool workers,
                       @NotNull Scanner params,
                       @NotNull String prompt) {
            // Parse params.
            final int id;
            try {
                id = params.nextInt();
            } catch (NoSuchElementException e) {
                System.out.printf(USAGE);
                return false;
            }

            // Print the status of the job.
            final Job job;
            final Optional<Result> result;
            try {
                job = jobs.get(id);
                result = jobs.resultOf(id);
            } catch (IndexOutOfBoundsException e) {
                System.out.printf("Invalid index (%d)\n", id);
                return false;
            }

            if (result.isPresent()) {
                System.out.printf("Job %d (%s) is %s\n", id, job.jarFile(), result.get());
            } else {
                System.out.printf("Job %d (%s) is RUNNING\n", id, job.jarFile());
            }
            return true;
        }
    },
    ;

    abstract boolean runFor(@NotNull JobManager jobs,
                            @NotNull WorkerPool workers,
                            @NotNull Scanner params,
                            @NotNull String prompt);

    static void executeJob(@NotNull JobManager jobs,
                           @NotNull Job.Files files,
                           int id,
                           @NotNull String prompt) {
        final Result result;
        try {
            result = jobs.execute(id);
        } catch (WorkerUnavailableException e) {
            System.out.printf("No Workers available to execute job %d (%s).\n", id, files.jar);
            System.out.printf(prompt);
            System.out.flush();
            return;
        }
        System.out.printf("Job %d (%s) terminated with status: %s\noutput file: %s\nlog file: %s\n",
            id, files.jar, result.name(), files.out, files.log);
        System.out.printf(MasterCLI.PROMPT);
        System.out.flush();
    }

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
