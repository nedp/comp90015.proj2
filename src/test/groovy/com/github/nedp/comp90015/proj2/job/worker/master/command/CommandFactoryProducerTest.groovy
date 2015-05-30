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

    def "FromName works correctly"() {
        expect: underTest.fromName(name) == expected

        where:
        name         || expected
        "add"        || Optional.of(addFactory)
        "addw"       || Optional.of(addFactory)
        "aw"         || Optional.of(addFactory)
        "a"          || Optional.of(addFactory)
        "w"          || Optional.of(addFactory)

        "list"       || Optional.of(listFactory)
        "ls"         || Optional.of(listFactory)
        "listw"      || Optional.of(listFactory)
        "lsw"        || Optional.of(listFactory)
        "lw"         || Optional.of(listFactory)
        "l"          || Optional.of(listFactory)

        "submit"     || Optional.of(submitFactory)
        "job"        || Optional.of(submitFactory)
        "sjob"       || Optional.of(submitFactory)
        "j"          || Optional.of(submitFactory)

        "status"     || Optional.of(statusFactory)
        "jobstatus"  || Optional.of(statusFactory)
        "stat"       || Optional.of(statusFactory)
        "jobstat"    || Optional.of(statusFactory)
        "s"          || Optional.of(statusFactory)

        "foo"        || Optional.empty()
        "foobar"     || Optional.empty()
        "bar"        || Optional.empty()
    }

    def producedFactoryIs(CommandFactory expected) {
        result == expected
    }
}
