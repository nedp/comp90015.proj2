package com.github.nedp.comp90015.proj2.job;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

/**
 * Runs a JAR in a separate JVM process.
 * <p/>
 * When {@link #run} is called, a new JVM process will be started,
 * running the specified JAR, passing in the specified input and
 * output files, and using the specified log file for stdout and
 * stderr.
 * The Job can be queried at any time for its current {@link Status}.
 *
 * @author nedp
 */
@SuppressWarnings("unused") // TODO Dependents aren't implemented yet.
public class Job implements Runnable {


    private static final String JAVA = "java";
    private static final String JAR_FLAG = "-jar";
    private static final int NO_LIMIT = -1;
    private static final int NO_TIMEOUT = -1;

    @NotNull
    private final Files files;

    // TODO add memory limit
    // private final int memoryLimit; // in MB
    // TODO add timeout
    // private final int timeout; // in seconds

    private final StatusTracker tracker;


    /**
     * Creates a new Job with no memory limit or timeout.
     * <p/>
     * The Job may be executed as
     * {@code java -jar jarFile inFile outFile >logFile 2>&1}.
     *
     * @param files  not null
     * @param tracker  not null, should be ready to {@link StatusTracker#start}
     */
    public Job(@NotNull Files files, @NotNull StatusTracker tracker) {
        this.files = files;
        this.tracker = tracker;
        // TODO
        // this.memoryLimit = NO_LIMIT;
        // this.timeout = NO_TIMEOUT;
    }

    @Override
    final public void run() {
        // TODO add
        final boolean FAILURE = false;

        // Enforce run-once semantics by checking and advancing state.
        this.tracker.start();

        // Run the JVM with the jar and input file.
        // Pipe stderr and stdout to a log file.
        final boolean ok;
        try {
            ProcessBuilder pb = new ProcessBuilder(JAVA, JAR_FLAG, this.files.jar.toString(),
                this.files.in.toString(), this.files.out.toString());
            pb.redirectErrorStream(true);
            pb.redirectOutput(this.files.log);

            final Process p = pb.start();
            ok = p.waitFor() == 0;

        } catch (IOException | InterruptedException e) {
            // TODO log this nicely
            System.out.printf("Job#run:  %s caught:  %s\n", e.getClass(), e.getMessage());

            this.tracker.finish(FAILURE);
            return;
        }

        // Advance the state based on whether everything is ok.
        this.tracker.finish(ok);
    }

    /**
     * Queries the injected {@link StatusTracker} for the current status.
     *
     * @return the status
     */
    @NotNull
    final public Status currentStatus() {
        return this.tracker.current();
    }

    /**
     * Retrieves the file used as the JAR for this Job.
     * @return the JAR's File object.
     */
    @NotNull
    final public File jarFile() {
        return this.files.jar;
    }

    /**
     * Retrieves the file used as the input file for this Job.
     * @return the input File object.
     */
    @NotNull
    final public File inFile() {
        return this.files.in;
    }

    /**
     * Retrieves the file used as the output file for this Job.
     * @return the output File object.
     */
    @NotNull
    final public File outFile() {
        return this.files.out;
    }

    /**
     * Retrieves the file used to collect stdout and stderr for this Job.
     * @return the log File object.
     */
    @NotNull
    final public File logFile() {
        return this.files.log;
    }

    /**
     * A record to store the files associated with a {@link Job}.
     */
    @SuppressWarnings("unused")
    public static final class Files {
        @NotNull public final File jar;
        @NotNull public final File in;
        @NotNull public final File out;
        @NotNull public final File log;

        /**
         * Creates a record by appending suffixes to the base name.
         * <p/>
         * eg: {@code new Files("name")} returns
         * {@code Files{"name.jar", "name.in", "name.out", "name.log"}}
         *
         * @param basename  not null, not empty
         *                  "$basename$.jar" and "$basename$.in should exist"
         */
        public Files(@NotNull String basename) {
            this.jar = new File(String.format("%s.jar", basename));
            this.in = new File(String.format("%s.in", basename));
            this.out = new File(String.format("%s.out", basename));
            this.log = new File(String.format("%s.log", basename));
        }

        /**
         * Creates a record with the specified files.
         */
        public Files(@NotNull File jar, @NotNull File in, @NotNull File out, @NotNull File log) {
            this.jar = jar;
            this.in = in;
            this.out = out;
            this.log = log;
        }
    }

    // TODO uncomment when timeouts and memory limits are implemented
    // /**
    //  * Creates a new Job which may executed as:
    //  * {@code java -jar jarFile inFile outFile >logFile 2>&1}.
    //  *
    //  * @param files  not null
    //  * @param tracker  not null, should be ready to {@link StatusTracker#start}
    //  * @param memoryLimit  maximum number of megabytes of RAM to allocate to the job,
    //  *                     no limit if non-positive
    //  * @param timeout  maximum number of seconds to wait for the job to finish,
    //  *                 no limit if non-positive
    //  */
    // public Job(@NotNull Files files, @NotNull StatusTracker tracker, int memoryLimit, int timeout) {
    //     if (memoryLimit < 1) {
    //         memoryLimit = NO_LIMIT;
    //     }
    //     if (timeout < 1) {
    //         timeout = NO_TIMEOUT;
    //     }
    //     this.files = files;
    //     this.tracker = tracker;
    //     this.memoryLimit = memoryLimit;
    //     this.timeout = timeout;
    // }
}
