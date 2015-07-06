package com.github.nedp.comp90015.proj2.job.worker.master;

/**
 * Indicates that there are no Worker available.
 *
 * @author nedp
 */
public class WorkerUnavailableException extends Exception {
  WorkerUnavailableException() {
    super("no suitable Workers present");
  }
}
