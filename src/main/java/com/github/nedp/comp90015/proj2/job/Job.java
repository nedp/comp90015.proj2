package java.com.github.nedp.comp90015.proj2.job;

import java.nio.file.Path;

/**
 * Class Job
 * <p/>
 * Runs a JAR in a separate JVM process.
 *
 * @author nedp
 */
public class Job implements Runnable {
    private final Path jarPath;
    private final Path inPath;
    private final Path outPath;

    private Status status;

    Job(Path jarPath, Path inPath, Path outPath) {
        this.jarPath = jarPath;
        this.inPath = inPath;
        this.outPath = outPath;

        this.status = Status.WAITING;
    }

    @Override
    public void run() {
        // Advance the state, everything is ok.
        this.status = this.status.nextState(true);

        // Run the JVM with the jar and input file.
        // Pipe stderr and stdout to a log file.
        final boolean ok = false; // TODO

        // Advance the state based on whether there
        // has been a failure.
        this.status = this.status.nextState(ok);
    }
}
