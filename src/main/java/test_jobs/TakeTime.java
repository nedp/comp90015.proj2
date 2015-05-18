package test_jobs;

/**
 * Sleeps for 5 seconds then exits with exit code 0.
 * <p/>
 * If interrupted, exits with exit code 1.
 *
 * @author nedp
 */
class TakeTime {
    private static final int EXIT_SUCCESS = 0;
    private static final int EXIT_FAILURE = 1;
    private static final int FIVE_SECONDS = 5000;

    public static void main(String[] args) {
        try {
            Thread.sleep(FIVE_SECONDS);
        } catch (InterruptedException e) {
            System.exit(EXIT_FAILURE);
        }
        System.exit(EXIT_SUCCESS);
    }
}

