package test_jobs;

import java.util.ArrayList;

/**
 * Allocates a large amount of memory for an ArrayList and exits with 0.
 * <p/>
 * Should require slightly more than 4MB heap space.
 *
 * @author nedp
 */
class UseMemory {
    private static final int EXIT_SUCCESS = 0;
    private static final int ONE_MILLION = 1000000;

    public static void main(String[] args) {
        new ArrayList<Integer>(ONE_MILLION);
        System.exit(EXIT_SUCCESS);
    }
}
