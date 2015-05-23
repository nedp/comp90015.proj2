package com.github.nedp.comp90015.proj2.job.worker.master

import com.github.nedp.comp90015.proj2.job.Job
import static com.github.nedp.comp90015.proj2.job.worker.master.Result.*;

import spock.lang.Specification

/**
 * Created by nedp on 18/05/15.
 */
class JobManagerTest extends Specification {
    JobManager testTarget
    WorkerPool pool
    Job job
    int id
    Result result

    def setup() {
        job = Stub Job
        pool = Mock WorkerPool
        testTarget = new JobManager(pool)
    }

    def "#submit stores the job with no result"() {
        given: jobIsSubmitted()
        expect: resultIsNotPresent()
    }

    def "#execute stores and returns the result of the job"() {
        given: jobIsSubmitted()

        when: jobIsExecuted()
        then: interaction { poolAllocatesProducing expected }

        expect: executionResultWas expected
        and: storedResultIs expected

        where: expected << [DISCONNECTED, FAILED, FINISHED]
    }

    def "#execute(job) propogates exceptions correctly"() {
        given: jobIsSubmitted()

        when: jobIsExecuted()
        then: interaction { emptyPoolAllocationFails() }
        then: thrown WorkerUnavailableException

        expect: resultIsNotPresent()
    }

    /*
     * Non-contract behaviour:
     */
    def "#execute, #resultOf, and #hasAllocated throw an exception for untracked Jobs"() {
        given: "the job was not submitted"; id = 0

        when: testTarget.execute(id)
        then: thrown IndexOutOfBoundsException

        when: testTarget.resultOf(id)
        then: thrown IndexOutOfBoundsException

        when: testTarget.hasAllocated(id)
        then: thrown IndexOutOfBoundsException
    }

    def "#execute throws an exception for already-allocated Jobs"() {
        given: jobIsSubmitted()
        and: jobIsAllocated()

        when: testTarget.execute(id)
        then: thrown IllegalStateException
    }

    /*
     * Helpers
     */

    def jobIsSubmitted() {
        id = testTarget.submit(job)
    }

    def jobIsExecuted() {
        result = testTarget.execute(id)
    }

    def jobIsAllocated() {
        testTarget.jobResults.get(id).hasBeenAllocated = true
    }

    def poolAllocatesProducing(Result result) {
        1 * pool.allocateAndExecute(job) >> result
    }

    def emptyPoolAllocationFails() {
        1 * pool.allocateAndExecute(job) >> { throw new WorkerUnavailableException("expected") }
    }

    def resultIsNotPresent() {
        !testTarget.resultOf(id).isPresent()
    }

    def executionResultWas(Result expected) {
        result == expected
    }

    def storedResultIs(Result expected) {
        testTarget.resultOf(id) == Optional.of(expected)
    }

    def reportedNameIs(String expected) {
        testTarget.nameOf(id) == expected
    }
}
