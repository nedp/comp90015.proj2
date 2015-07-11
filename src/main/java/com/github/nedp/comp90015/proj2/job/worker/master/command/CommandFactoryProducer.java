package com.github.nedp.comp90015.proj2.job.worker.master.command;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Factory Producer for creating CommandFactories.
 *
 * @author nedp
 */
class CommandFactoryProducer {

  private final CommandFactory addFactory;
  private final CommandFactory listFactory;
  private final CommandFactory submitFactory;
  private final CommandFactory statusFactory;

  /**
   * Creates a CommandFactoryProducer which returns the specified {@link CommandFactory}s.
   *
   * @param addFactory responsible for Command.Type.ADD_WORKERS, not null.
   * @param listFactory responsible for Command.Type.LIST_WORKERS, not null.
   * @param submitFactory responsible for Command.Type.SUBMIT_JOB, not null.
   * @param statusFactory responsible for Command.Type.JOB_STATUS, not null.
   */
  public CommandFactoryProducer(@NotNull CommandFactory addFactory,
                                @NotNull CommandFactory listFactory,
                                @NotNull CommandFactory submitFactory,
                                @NotNull CommandFactory statusFactory) {
    this.addFactory = addFactory;
    this.listFactory = listFactory;
    this.submitFactory = submitFactory;
    this.statusFactory = statusFactory;
  }

  @NotNull
  Optional<CommandFactory> fromName(@NotNull String word) {
    switch (word) {
      case "add":
      case "addw":
      case "aw":
      case "a":
      case "w":
        return Optional.of(this.addFactory);

      case "list":
      case "ls":
      case "listw":
      case "lsw":
      case "lw":
      case "l":
        return Optional.of(this.listFactory);

      case "submit":
      case "job":
      case "sjob":
      case "j":
        return Optional.of(this.submitFactory);

      case "status":
      case "jobstatus":
      case "stat":
      case "jobstat":
      case "s":
        return Optional.of(this.statusFactory);

      default:
        return Optional.empty();
    }
  }
}
