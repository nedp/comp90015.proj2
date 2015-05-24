package com.github.nedp.comp90015.proj2.job.worker.master.command;

import com.github.nedp.comp90015.proj2.job.worker.master.*;
import org.jetbrains.annotations.NotNull;

import java.io.PrintStream;
import java.util.Scanner;

/**
 * Factory class for creating {@link Command}s.
 * <p/>
 * This implementation uses command line parameters
 * accessed via a scanner.
 *
 * @author nedp
 */
public class CommandFactory {

    public Command fromParams(Command.Type type, Scanner params) {
        // Parse params.
        switch (type) {
            case ADD_WORKER:
                return AddCommand.FromParams(params);
            case LIST_WORKERS:
                return ListCommand.FromParams(params);
            case SUBMIT_JOB:
                return SubmitCommand.FromParams(params);
            case JOB_STATUS:
                return StatusCommand.FromParams(params);
            default:
                throw new RuntimeException("incomplete switch case coverage.");
        }
    }
}
