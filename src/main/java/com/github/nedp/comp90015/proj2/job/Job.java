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

    private final File logFile;
    private final File outFile;
    private final File inFile;
    private final File jarFile;

    private final StatusTracker status;

    /**
     * Creates a new Job which may executed as:
     * {@code java -jar jarFile inFile outFile >logFile 2>&1}.
     *
     * @param jarFile  not null, not empty
     * @param inFile  not null
     * @param outFile  not null
     * @param logFile  not null, not empty
     * @param status  not null, should be ready to start
     */
    Job(@NotNull File jarFile, @NotNull File inFile, @NotNull File outFile, @NotNull File logFile, @NotNull StatusTracker status) {
        this.jarFile = jarFile;
        this.inFile = inFile;
        this.outFile = outFile;
        this.logFile = logFile;
        this.status = status;
    }

    @Override
    final public void run() {
        final boolean FAILURE = false;

        // Enforce run-once semantics by checking and advancing state.
        this.status.start();

        // Run the JVM with the jar and input file.
        // Pipe stderr and stdout to a log file.
        final boolean ok;
        try {
            final String cmd = String.format("java -jar %s %s %s >%s 2>&1",
                this.jarFile, this.inFile, this.outFile, this.logFile);
            final Process p = Runtime.getRuntime().exec(cmd);
            ok = p.waitFor() == 0;
        } catch (IOException | InterruptedException e) {
            this.status.finish(FAILURE);
            return;
        }

        // Advance the state based on whether everything is ok.
        this.status.finish(ok);
    }

    /**
     * Queries the injected {@link StatusTracker} for the current status.
     *
     * @return the status
     */
    final public Status currentStatus() {
        return this.status.current();
    }
}
