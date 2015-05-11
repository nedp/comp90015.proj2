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
public class Job implements Runnable {
    // TODO add memory limit
    // TODO add timeout

    private static final String JAVA = "java";
    private static final String JAR_FLAG = "-jar";

    private final File logFile;
    private final File outFile;
    private final File inFile;
    private final File jarFile;

    private final StatusTracker tracker;

    /**
     * Creates a new Job which may executed as:
     * {@code java -jar jarFile inFile outFile >logFile 2>&1}.
     *
     * @param jarFile  not null, not empty
     * @param inFile  not null
     * @param outFile  not null
     * @param logFile  not null, not empty
     * @param tracker  not null, should be ready to start
     */
    Job(@NotNull File jarFile, @NotNull File inFile, @NotNull File outFile,
        @NotNull File logFile, @NotNull StatusTracker tracker)
    {
        this.jarFile = jarFile;
        this.inFile = inFile;
        this.outFile = outFile;
        this.logFile = logFile;
        this.tracker = tracker;
    }

    @Override
    final public void run() {
        final boolean FAILURE = false;

        // Enforce run-once semantics by checking and advancing state.
        this.tracker.start();

        // Run the JVM with the jar and input file.
        // Pipe stderr and stdout to a log file.
        final boolean ok;
        try {
            ProcessBuilder pb = new ProcessBuilder(JAVA, JAR_FLAG, this.jarFile.toString(),
                this.inFile.toString(), this.outFile.toString());
            pb.redirectErrorStream(true);
            pb.redirectOutput(this.logFile);

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
    final public Status currentStatus() {
        return this.tracker.current();
    }
}
