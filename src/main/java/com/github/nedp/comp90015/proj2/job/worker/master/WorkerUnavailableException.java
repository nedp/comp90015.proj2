package com.github.nedp.comp90015.proj2.job.worker.master;


import javax.resource.spi.UnavailableException;

/**
 * Indicates that there are no Worker available.
 *
 * @author nedp
 */
public class WorkerUnavailableException extends UnavailableException {
    WorkerUnavailableException(String message) {
        super(message);
    }

    WorkerUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
