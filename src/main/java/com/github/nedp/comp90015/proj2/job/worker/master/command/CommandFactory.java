package com.github.nedp.comp90015.proj2.job.worker.master.command;

import org.jetbrains.annotations.NotNull;

import java.util.Scanner;

/**
 * Factory interface for creating Commands.
 *
 * @author nedp
 */
public interface CommandFactory {
  /**
   * Builds a concrete Command from parameters.
   *
   * @param params the Scanner providing the parameters.
   * @return an instance of Command.
   */
  @NotNull
  Command fromParams(Scanner params);
}
