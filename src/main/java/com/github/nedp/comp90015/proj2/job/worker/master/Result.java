package com.github.nedp.comp90015.proj2.job.worker.master;

/**
 * Identifies the manner in which a Job terminated.
 *
 * @author nedp
 */
@SuppressWarnings("unused") // TODO dependents not implemented
public enum Result {
  DISCONNECTED,
  FINISHED,
  FAILED,
}
