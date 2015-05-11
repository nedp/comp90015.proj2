package com.github.nedp.comp90015.proj2.job

import java.io.File

import spock.lang.Specification

import static Status.*

/**
 * Created by nedp on 11/05/15.
 */
class JobTest extends Specification {
    static def PATH = "src/test/resources/do_nothing"
    static def JAR = new File("${PATH}.jar")
    static def IN = new File("${PATH}.in")
    static def OUT = new File("${PATH}.out")
    static def LOG = new File("${PATH}.log")
    static def NONE = new File("")

    static def BAD_PATH = "src/test/resources/fail_nothing"
    static def BAD_JAR = new File("${BAD_PATH}.jar")
    static def BAD_IN = new File("${BAD_PATH}.in")
    static def BAD_OUT = new File("${BAD_PATH}.out")
    static def BAD_LOG = new File("${BAD_PATH}.log")

    def setupSpec() {
        try { OUT.delete() } catch (_) {}
        try { LOG.delete() } catch (_) {}
        try { BAD_OUT.delete() } catch (_) {}
        try { BAD_LOG.delete() } catch (_) {}
    }

    def cleanup() {
        try { OUT.delete() } catch (_) {}
        try { LOG.delete() } catch (_) {}
        try { BAD_OUT.delete() } catch (_) {}
        try { BAD_LOG.delete() } catch (_) {}
    }

    def "delegates #currentStatus to tracker#current"() {
        given:
        StatusTracker tracker = Mock()
        1 * tracker.current() >> want
        0 * _
        def job = new Job(JAR, IN, OUT, LOG, tracker)

        expect: job.currentStatus() == want

        where:
        want << [WAITING, RUNNING, FINISHED, FAILED]
    }

    def "runs using the output files, and finishes"() {
        given:
        StatusTracker tracker = Mock()
        def job = new Job(JAR, IN, OUT, LOG, tracker)

        when: job.run()
        then:
        OUT.exists()
        LOG.exists()
        1 * tracker.start()
        1 * tracker.finish(true)
        0 * _
    }

    def "detects failure to invoke"() {
        given:
        StatusTracker tracker = Mock()
        def job = new Job(jar, in_, out, log, tracker)

        when: job.run()
        then:
        1 * tracker.start()
        1 * tracker.finish(false)
        0 * _

        where:
        jar  | in_  | out  | log
        NONE | IN   | OUT  | LOG
        JAR  | NONE | OUT  | LOG
        JAR  | IN   | NONE | LOG
        JAR  | IN   | OUT  | NONE
    }

    def "detects non-zero exit code failure"() {
        given:
        StatusTracker tracker = Mock()
        def job = new Job(BAD_JAR, BAD_IN, BAD_OUT, BAD_LOG, tracker)

        when: job.run()
        then:
        1 * tracker.start()
        1 * tracker.finish(false)
        0 * _
    }
}
