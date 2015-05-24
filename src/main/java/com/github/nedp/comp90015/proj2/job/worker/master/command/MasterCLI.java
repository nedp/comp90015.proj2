package com.github.nedp.comp90015.proj2.job.worker.master.command;

import com.github.nedp.comp90015.proj2.job.worker.master.JobManager;
import com.github.nedp.comp90015.proj2.job.worker.master.WorkerPool;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.Optional;
import java.util.Scanner;

/**
 * Provides a CLI interface for the Master (client) side of the system.
 *
 * @author nedp
 */
class MasterCLI implements Runnable {
    private static final String PROMPT = "\nMaster System > ";
    private static final int EXIT_FAILURE = -1;

    public static void main(String[] args) throws IOException {
        final BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        final WorkerPool workers = new WorkerPool();
        final JobManager jobs = new JobManager(workers);
        final CommandFactoryProducer commandFactoryProducer =
            new CommandFactoryProducer(new AddCommand.Factory(), new ListCommand.Factory(),
                new StatusCommand.Factory(), new SubmitCommand.Factory());

        try {
            new MasterCLI(workers, jobs, commandFactoryProducer, in).run();
        } catch (RuntimeException e) {
            e.printStackTrace();
            System.exit(EXIT_FAILURE);
        }
    }

    @NotNull
    private final BufferedReader in;
    @NotNull
    private final WorkerPool workers;
    @NotNull
    private final JobManager jobs;
    @NotNull
    private final CommandFactoryProducer factoryProducer;
    @NotNull
    private final String prompt;
    @NotNull
    private final PrintStream out;

    private MasterCLI(@NotNull WorkerPool workers,
                      @NotNull JobManager jobs,
                      @NotNull CommandFactoryProducer factoryProducer,
                      @NotNull BufferedReader in) {
        this.in = in;
        this.workers = workers;
        this.jobs = jobs;
        this.factoryProducer = factoryProducer;
        this.prompt = MasterCLI.PROMPT;
        this.out = System.out;
    }

    /**
     * Continuously accepts input from the in.
     * <p/>
     * Continues until the
     */
    @Override
    public void run() {
        try {
            // For each input line:
            String lineString;
            while ((lineString = this.nextInputLine()) != null) {
                final Scanner line = new Scanner(lineString);
                // Skip empty lines.
                if (!line.hasNext()) {
                    continue;
                }

                // Parse the command from the System.out, reporting bad commands.
                final String commandName = line.next();
                final Optional<Command.Type> commandType = Command.Type.FromName(commandName);
                if (!commandType.isPresent()) {
                    this.out.printf("command not recognised: %s\n", commandName);
                    continue;
                }
                final Command command =
                    this.factoryProducer.fromType(commandType.get()).fromParams(line);

                // Execute the command using the rest of the line.
                final boolean ok = command.runOn(jobs, workers, out);
                if (!ok) {
                    this.out.printf("^~~~ Failed to execute command.\n");
                }
            }
            this.out.print("End of input.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Convenience method for reading in input.
    private String nextInputLine() throws IOException {
        this.out.printf(this.prompt);
        this.out.flush();
        return in.readLine();
    }
}
