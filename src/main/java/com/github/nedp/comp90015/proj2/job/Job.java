package com.github.nedp.comp90015.proj2.job;

import java.io.File;
import java.io.IOException;

/**
 * Class Job
 * runs a JAR in a separate JVM process.
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

    private Status status;

    /**
     * creates a new Job which will may executed with:
     * {@code java -jar jarFile inFile outFile >logFile 2>&1}.
     *
     * @param jarFile not null
     * @param inFile not null
     * @param outFile not null
     * @param logFile not null
     */
    Job(File jarFile, File inFile, File outFile, File logFile) {
        this.jarFile = jarFile;
        this.inFile = inFile;
        this.outFile = outFile;
        this.logFile = logFile;
        this.status = Status.WAITING;
    }

    @Override
    public void run() {
        final boolean NO_FAILURE = true;
        final boolean FAILURE = false;

        // Advance the state, everything is ok.
        this.status = this.status.nextState(NO_FAILURE);

        // Run the JVM with the jar and input file.
        // Pipe stderr and stdout to a log file.
        final boolean ok;
        try {
            final String cmd = String.format("java -jar %s %s %s >%s 2>&1",
                this.jarFile, this.inFile, this.outFile, this.logFile);
            final Process p = Runtime.getRuntime().exec(cmd);
            ok = p.waitFor() == 0;
        } catch (IOException | InterruptedException e) {
            this.status = this.status.nextState(FAILURE);
            return;
        }

        // Advance the state based on whether everything is ok.
        this.status = this.status.nextState(ok);
    }
}
