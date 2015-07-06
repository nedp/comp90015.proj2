package test_jobs;

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
  private static final String STDOUT = "stdout: Doing nothing...";
  private static final String STDERR = "stderr: Done";

  private static final int EXIT_SUCCESS = 0;
  private static final int EXIT_FAILURE = 1;

  public static void main(String[] args) {
    final Path in = Paths.get(args[0]);
    final Path out = Paths.get(args[1]);

    System.out.println(STDOUT);

    // Check that we were called correctly.
    try {
      if (!in.toFile().canRead() || !out.toFile().createNewFile()) {
        System.err.println(STDERR);
        System.exit(EXIT_FAILURE);
      }
    } catch (IOException e) {
      System.err.println(STDERR);
      System.exit(EXIT_FAILURE);
    }

    // Do nothing

    System.err.println(STDERR);
    System.exit(EXIT_SUCCESS);
  }
}
