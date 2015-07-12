package com.github.nedp.comp90015.proj2.job.worker.master;

import com.github.nedp.comp90015.proj2.job.Job;
import org.jetbrains.annotations.NotNull;

/**
 * Interface for a remote Worker.
 * <p>
 * Each implementor instance should connect
 * to a single remote worker.
 *
 * @author nedp
 */
public interface Worker {
  /**
   * Executes a {@link Job} at the remote Worker.
   * <p>
   * This method must return only when either:
   * <ul>
   * <li>The worker reports that the {@link Job} has finished or
   * failed, and sends the appropriate output files.
   * In this case, the {@link Job}'s `out` and `log` files must
   * match those produced by the worker.</li>
   * <li>The worker disconnects.
   * In this case, no files must be synchronised.</li>
   * </ul>
   *
   * @param job the {@link Job} to be run by the worker
   * @return the {@link Result} of the Job execution: <ul>
   * <li>DISCONNECTED if the Worker disconnected before finishing.</li>
   * <li>FAILED if the Job failed to complete without disconnecting.</li>
   * <li>FINISHED if the Job finished successfully.</li>
   * </ul>
   */
  @NotNull
  Result execute(Job job);

  /**
   * Performs any work necessary to keep the Worker operational.
   * <p>
   * The Worker is guaranteed to be operational until this method returns.
   */
  void maintain();

  /**
   * Retrieves the last known status of the remote Worker.
   * <p>
   * May request an update but doesn't have to.
   */
  @NotNull
  Status status();

  /**
   * Retrieves the last known amount of free memory of the remote Worker.
   * <p>
   * May request an update but doesn't have to.
   *
   * @return the number of bytes of free memory.
   */
  long freeMemory();

  /**
   * Retrieves the identifier of the worker.
   * <p>
   * The identifier must uniquely identify a particular Worker.
   * It could be, for example, "hostname:port".
   *
   * @return the identifier as a String.
   */
  @NotNull
  String identifier();

  /**
   * Indicates the current status of a Worker.
   */
  enum Status {
    RUNNING,
    DOWN,;
  }
}
