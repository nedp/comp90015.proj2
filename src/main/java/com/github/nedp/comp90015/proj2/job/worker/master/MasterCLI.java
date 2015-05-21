package com.github.nedp.comp90015.proj2.job.worker.master;

import java.io.*;
import java.util.HashMap;
import java.util.Optional;
import java.util.Scanner;

/**
 * Created by nedp on 21/05/15.
 */
class MasterCLI {
    private static final int EXIT_SUCCESS = 0;
    private static final int EXIT_FAILURE = 1;
    private static final String PROMPT = "Master System > ";

    private static void main(String[] args) {
        final Console console = System.console();
        final WorkerPool workers = new WorkerPool();
        final JobManager jobs = new JobManager(workers, new HashMap<>());

        {
            String lineString;
            // For each scanned line:
            while ((lineString = console.readLine(PROMPT)) != null) {
                final Scanner line = new Scanner(lineString);
                // Skip empty lines.
                if (!line.hasNext()) {
                    continue;
                }

                // Parse the command from the console, reporting bad commands.
                final String commandName = line.next();
                final Optional<Command> command_opt = Command.fromWord(commandName);
                if (!command_opt.isPresent()) {
                    console.printf("command not recognised: %s\n", commandName);
                }

                // Execute the command using the rest of the line.
                assert(command_opt.isPresent());
                final boolean ok = command_opt.get().runFor(jobs, workers, line, console);
                if (!ok) {
                    console.printf("^~~~ Failed to execute command.");
                }
            }
        }
    }

}
