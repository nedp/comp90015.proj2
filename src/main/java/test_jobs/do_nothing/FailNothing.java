package test_jobs.do_nothing;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Exits with 0 for failure and 1 for success,
 * opposite to standard.
 *
 * Created by nedp on 10/05/15.
 */
public class FailNothing {
    private static final int EXIT_SUCCESS = 1;
    private static final int EXIT_FAILURE = 0;

    public static void main(String[] args) {
        final Path in = Paths.get(args[0]);
        final Path out = Paths.get(args[1]);

        System.out.println("Failing at nothing...");

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
