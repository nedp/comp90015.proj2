package com.github.nedp.comp90015.proj2.job.worker.master.command

import spock.lang.Shared
import spock.lang.Specification

import static com.github.nedp.comp90015.proj2.job.worker.master.command.Command.Type.*

/**
 * Created by nedp on 24/05/15.
 */
class CommandFactoryProducerTest extends Specification {
    CommandFactoryProducer underTest
    CommandFactory result

    @Shared
    CommandFactory addFactory = Mock CommandFactory
    @Shared
    CommandFactory listFactory = Mock CommandFactory
    @Shared
    CommandFactory submitFactory = Mock CommandFactory
    @Shared
    CommandFactory statusFactory = Mock CommandFactory

    def setup() {

        underTest =
            new CommandFactoryProducer(addFactory, listFactory, submitFactory, statusFactory);
    }

    def "fromType produces the correct factory"() {
        given: factoryProducedFor type
        expect: producedFactoryIs expected

        where:
        type         || expected
        ADD_WORKER   || addFactory
        LIST_WORKERS || listFactory
        SUBMIT_JOB   || submitFactory
        JOB_STATUS   || statusFactory
    }

    def factoryProducedFor(Command.Type type) {
        result = underTest.fromType(type)
    }

    def producedFactoryIs(CommandFactory expected) {
        result == expected
    }
}
