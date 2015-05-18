package com.github.nedp.comp90015.proj2.job.worker.master;

import com.github.nedp.comp90015.proj2.job.Job;

import java.io.IOException;

/**
 * Interface for reporting Results to the user.
 *
 * @author nedp
 */
public interface JobReporter {
    /**
     * Reports to the user a {@link Result} and a {@link Job}.
     * <p/>
     * This may be done via CLI, GUI, or another user interface.
     * The output in the {@link Job.Files} associated with the input
     * {@link Job} should also be made available.
     *
     * @param job the job associated with the report, not null.
     * @param result the result to be reported, not null.
     * @throws IOException if the report is unable to be displayed.
     */
    void report(Job job, Result result) throws IOException;
}
