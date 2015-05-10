package java.com.github.nedp.comp90015.proj2.job;

/**
 * Class Job
 * <p/>
 * Runs a JAR in a separate JVM process.
 *
 * @author nedp
 */
public class Job implements Runnable {
    private final String jarFilename;
    private final String inFilename;
    private final String outFilename;

    private Status status;

    Job(String jarFilename, String inFilename, String outFilename) {
        this.jarFilename = jarFilename;
        this.inFilename = inFilename;
        this.outFilename = outFilename;

        this.status = Status.WAITING;
    }

    @Override
    public void run() {
        // Verify that the required files exist.
        final boolean filesExist = true; // TODO

        // Advance the state.
        this.status = this.status.nextState(!filesExist);

        // Run the JVM with the jar and input file.
        // Pipe stderr and stdout to a log file.
        final boolean didFail = false; // TODO

        // Advance the state.
        this.status = this.status.nextState(didFail);
    }
}
