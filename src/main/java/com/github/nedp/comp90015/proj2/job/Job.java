package java.com.github.nedp.comp90015.proj2.job;

/**
 * Class Job
 *
 * @author nedp
 */
public class Job implements Runnable {
    final private String jarFilename;
    final private String inFilename;
    final private String outFilename;

    Job(String jarFilename, String inFilename, String outFilename) {
        this.jarFilename = jarFilename;
        this.inFilename = inFilename;
        this.outFilename = outFilename;
    }

    @Override
    public void run() {
        // Verify that the required files exist.
        // TODO

        // Run the JVM with the jar and input files,
        // piping to the output file.
        // TODO
    }
}
