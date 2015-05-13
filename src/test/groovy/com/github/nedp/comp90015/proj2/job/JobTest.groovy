package com.github.nedp.comp90015.proj2.job

import java.nio.file.Files

import spock.lang.Specification

import test_jobs.DoNothing

import static Status.*

/**
 * Created by nedp on 11/05/15.
 */
class JobTest extends Specification {
    static def PATH = "src/test/resources/test_jobs/do_nothing"
    static def FILES = new Job.Files(PATH);
    static def JAR = FILES.jar
    static def IN = FILES.in
    static def OUT = FILES.out
    static def LOG = FILES.log
    static def NONE = new File("")

    static def BAD_PATH = "src/test/resources/test_jobs/fail_nothing"
    static def BAD_FILES = new Job.Files(BAD_PATH);
    static def BAD_OUT = BAD_FILES.out
    static def BAD_LOG = BAD_FILES.log

    static def WORD_COUNT_JAR =  new File("src/test/resources/SampleJob/wordcount.jar")
    static def WORD_COUNT_IN =   new File("src/test/resources/SampleJob/sample-input.txt")
    static def WORD_COUNT_WANT = new File("src/test/resources/SampleJob/sample-output.txt")
    static def WORD_COUNT_OUT =  new File("src/test/resources/SampleJob/wordcount.out")
    static def WORD_COUNT_LOG =  new File("src/test/resources/SampleJob/wordcount.log")

    def setupSpec() {
        try { OUT.delete() } catch (_) {}
        try { LOG.delete() } catch (_) {}
        try { BAD_OUT.delete() } catch (_) {}
        try { BAD_LOG.delete() } catch (_) {}
        try { WORD_COUNT_OUT.delete() } catch (_) {}
        try { WORD_COUNT_LOG.delete() } catch (_) {}
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
        def job = new Job(FILES, tracker)

        expect: job.currentStatus() == want

        where:
        want << [WAITING, RUNNING, FINISHED, FAILED]
    }

    def "runs using the output files, and finishes"() {
        given:
        StatusTracker tracker = Mock()
        def job = new Job(FILES, tracker)

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
        def job = new Job(new Job.Files(jar, in_, out, log), tracker)

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
        def job = new Job(BAD_FILES, tracker)

        when: job.run()
        then:
        1 * tracker.start()
        1 * tracker.finish(false)
        0 * _
    }

    def "routes both stderr and stdout to _.log"() {
        given:
        StatusTracker tracker = Mock()
        def job = new Job(FILES, tracker)

        when:
        job.run()
        def lines = Files.readAllLines(LOG.toPath())

        then:
        1 * tracker.start()
        1 * tracker.finish(true)
        0 * _
        and:
        lines[0] == DoNothing.STDOUT
        lines[1] == DoNothing.STDERR
        lines.size() == 2
    }

    def "Files is correct"() {
        given:
        def jar = new File("1");
        def in_ = new File("2");
        def out = new File("3");
        def log = new File("4");
        def files = new Job.Files(jar, in_, out, log)

        expect:
        jar == files.jar
        in_ == files.in
        out == files.out
        log == files.log
    }

    def "Runs wordcount with expected output"() {
        given:
        StatusTracker tracker = Mock()
        def files = new Job.Files(WORD_COUNT_JAR, WORD_COUNT_IN, WORD_COUNT_OUT, WORD_COUNT_LOG)
        def job = new Job(files, tracker)

        when: job.run()
        then:
        1 * tracker.start()
        1 * tracker.finish(true)
        0 * _
        and:
        def want = Files.readAllLines(WORD_COUNT_WANT.toPath())
        def got = Files.readAllLines(WORD_COUNT_OUT.toPath())
        want.equals(got)
    }

    // TODO
    def "Jobs timeout correctly"() {

    }

    // TODO
    def "Jobs run out of memory correctly"() {

    }
}
