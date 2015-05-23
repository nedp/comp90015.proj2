package com.github.nedp.comp90015.proj2.job.worker.master;

import java.io.*;
import java.util.Optional;
import java.util.Scanner;

/**
 * Provides a CLI interface for the Master (client) side of the system.
 *
 * @author nedp
 */
class MasterCLI {
    static final int EXIT_SUCCESS = 0;
    static final int EXIT_FAILURE = 1;
    static final String PROMPT = "\nMaster System > ";

    public static void main(String[] args) throws IOException {
        final BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
        final WorkerPool workers = new WorkerPool();
        final JobManager jobs = new JobManager(workers);

        // For each input line:
        String lineString;
        while ((lineString = nextInputLine(PROMPT, console)) != null) {
            final Scanner line = new Scanner(lineString);
            // Skip empty lines.
            if (!line.hasNext()) {
                continue;
            }

            // Parse the command from the System.out, reporting bad commands.
            final String commandName = line.next();
            final Optional<Command> command = Command.fromWord(commandName);

            if (!command.isPresent()) {
                System.out.printf("command not recognised: %s\n", commandName);
                continue;
            }

            // Execute the command using the rest of the line.
            final boolean ok = command.get().runFor(jobs, workers, line, PROMPT);
            if (!ok) {
                System.out.printf("^~~~ Failed to execute command.\n");
            }
        }
    }

    // Convenience method for reading console intput.
    private static String nextInputLine(String prompt, BufferedReader console) throws IOException {
        System.out.print(prompt);
        System.out.flush();
        return console.readLine();
    }
}
