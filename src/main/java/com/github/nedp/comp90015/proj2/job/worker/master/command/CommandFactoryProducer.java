package com.github.nedp.comp90015.proj2.job.worker.master.command;

import org.jetbrains.annotations.NotNull;

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
     * @param addFactory  responsible for Command.Type.ADD_WORKERS, not null.
     * @param listFactory  responsible for Command.Type.LIST_WORKERS, not null.
     * @param submitFactory  responsible for Command.Type.SUBMIT_JOB, not null.
     * @param statusFactory  responsible for Command.Type.JOB_STATUS, not null.
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

    /**
     * Retrieves the correct {@link CommandFactory} for the {@link Command.Type}.
     *
     * @param type  the Command.Type desired for production.
     * @return the CommandFactory responsible for producing Commands
     * with the desired Type.
     */
    public CommandFactory fromType(Command.Type type) {
        // Parse params.
        switch (type) {
            case ADD_WORKER:
                return this.addFactory;
            case LIST_WORKERS:
                return this.listFactory;
            case SUBMIT_JOB:
                return this.submitFactory;
            case JOB_STATUS:
                return this.statusFactory;
            default:
                throw new RuntimeException("incomplete switch case coverage.");
        }
    }
}
