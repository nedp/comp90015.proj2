package com.github.nedp.comp90015.proj2.job.worker.master

import spock.lang.Specification

import static com.github.nedp.comp90015.proj2.job.worker.master.Command.*

/**
 * Created by nedp on 24/05/15.
 */
class CommandTest extends Specification {
    def "RunFor"() {
        // TODO not sure how to test concurrent things.
    }

    def "FromWord works correctly"() {
        expect: FromWord(word) == expected

        where:
        word         || expected
        "add"        || Optional.of(ADD_WORKER)
        "addw"       || Optional.of(ADD_WORKER)
        "aw"         || Optional.of(ADD_WORKER)
        "a"          || Optional.of(ADD_WORKER)
        "w"          || Optional.of(ADD_WORKER)

        "list"       || Optional.of(LIST_WORKERS)
        "ls"         || Optional.of(LIST_WORKERS)
        "listw"      || Optional.of(LIST_WORKERS)
        "lsw"        || Optional.of(LIST_WORKERS)
        "lw"         || Optional.of(LIST_WORKERS)
        "l"          || Optional.of(LIST_WORKERS)

        "submit"     || Optional.of(SUBMIT_JOB)
        "job"        || Optional.of(SUBMIT_JOB)
        "sjob"       || Optional.of(SUBMIT_JOB)
        "j"          || Optional.of(SUBMIT_JOB)

        "status"     || Optional.of(JOB_STATUS)
        "jobstatus"  || Optional.of(JOB_STATUS)
        "stat"       || Optional.of(JOB_STATUS)
        "jobstat"    || Optional.of(JOB_STATUS)
        "s"          || Optional.of(JOB_STATUS)

        "foo"        || Optional.empty()
        "foobar"     || Optional.empty()
        "bar"        || Optional.empty()
    }
}
