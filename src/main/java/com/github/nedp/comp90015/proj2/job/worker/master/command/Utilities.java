package com.github.nedp.comp90015.proj2.job.worker.master.command;

import com.github.nedp.comp90015.proj2.job.Job;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Provides utilities used by this package.
 *
 * @author nedp
 */
public interface Utilities {

    /**
     * Creates a Files object from the next four parameters.
     *
     * @param params  a {@link Scanner} to read parameters from, not null.
     * @return a {@link Job.Files} object built from the next four strings.
     */
    @NotNull
    static Job.Files GetFiles(@NotNull Scanner params) {
        final File jarFilename = new File(params.next());
        final File inFilename = new File(params.next());
        final File outFilename = new File(params.next());
        final File logFilename = new File(params.next());
        return new Job.Files(jarFilename, inFilename, outFilename, logFilename);
    }

    /**
     * Gets the next parameter, if it exists, as an integer.
     *
     * @param params  a {@link Scanner} to read parameters from, not null.
     * @param def  default value to use if there is no next value.
     * @return the next integer from the Scanner, or or the default
     * if there is no next value.
     *
     * @throws NoSuchElementException if the next parameter is
     * not an integer.
     */
    static int NextIntOr(@NotNull Scanner params, int def) {
        if (params.hasNext()) {
            return params.nextInt();
        } else {
            return def;
        }
    }
}
