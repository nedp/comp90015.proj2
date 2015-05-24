package com.github.nedp.comp90015.proj2.job.worker.master.command;

import com.github.nedp.comp90015.proj2.job.worker.master.JobManager;
import com.github.nedp.comp90015.proj2.job.worker.master.WorkerPool;
import org.jetbrains.annotations.NotNull;

import java.io.PrintStream;

/**
 * Command for printing a usage message.
 *
 * @author nedp
 */
class UsageCommand implements Command {
    @NotNull
    private final String message;

    UsageCommand(@NotNull String message) {
        this.message = message;
    }

    @Override
    public boolean runOn(@NotNull JobManager jobs,
                         @NotNull WorkerPool workers,
                         @NotNull PrintStream out) {
        out.printf("Invalid arguments. Correct usage is:\n\t> %s\n", message);
        return true;
    }
}
