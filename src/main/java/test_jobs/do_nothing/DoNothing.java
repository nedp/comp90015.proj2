package test_jobs.do_nothing;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Validates that it was called correctly
 * then exits with 0 for success and 1 for failure.
 *
 * @author nedp
 */
class DoNothing {
    private static final int EXIT_SUCCESS = 0;
    private static final int EXIT_FAILURE = 1;

    public static void main(String[] args) {
        final Path in = Paths.get(args[0]);
        final Path out = Paths.get(args[1]);

        System.out.println("Doing nothing...");

        // Check that we were called correctly.
        try {
            if (!in.toFile().canRead() || !out.toFile().createNewFile()) {
                System.exit(EXIT_FAILURE);
            }
        } catch (IOException e) {
            System.exit(EXIT_FAILURE);
        }

        // Do nothing
        
        System.exit(EXIT_SUCCESS);
    }
}
