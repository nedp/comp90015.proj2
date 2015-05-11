package com.github.nedp.comp90015.proj2.job

import java.io.File

import spock.lang.Specification

import static Status.*

/**
 * Created by nedp on 11/05/15.
 */
class JobTest extends Specification {
    def PATH = "src/test/resources/do_nothing"
    def JAR = new File("${PATH}.jar")
    def IN = new File("${PATH}.in")
    def OUT = new File("${PATH}.out")
    def LOG = new File("${PATH}.log")

    def cleanup() {
        try { OUT.delete() } catch (_) {}
        try { LOG.delete() } catch (_) {}
    }

    def "starts WAITING"() {
        given:
        StatusTracker tracker = Mock()
        1 * tracker.current() >> want
        0 * _

        def job = new Job(JAR, IN, OUT, LOG, tracker)

        expect: job.currentStatus() == want

        where:
        want << [WAITING, RUNNING, FINISHED, FAILED]
    }

    def "runs the jar to completion"() {
        given:
        StatusTracker tracker = Mock()
        1 * tracker.start()
        1 * tracker.finish(true)

        def job = new Job(JAR, IN, OUT, LOG, tracker)

        when: job.run()

        then:
        OUT.exists()
        LOG.exists()
    }
}
