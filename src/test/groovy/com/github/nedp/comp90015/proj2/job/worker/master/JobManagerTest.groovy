package com.github.nedp.comp90015.proj2.job.worker.master

import com.github.nedp.comp90015.proj2.job.Job
import static com.github.nedp.comp90015.proj2.job.worker.master.Result.*;

import spock.lang.Specification
/**
 * Created by nedp on 18/05/15.
 */
class JobManagerTest extends Specification {
    def pool
    def jobResults
    def jm

    def setup() {
        pool = Mock(WorkerPool)
        jobResults = Mock(Map)
        jm = new JobManager(pool, jobResults)
    }

    def "#execute(job) stores and returns the correct Result"() {
        given:
        def job = Mock(Job)
        0 * _
        and: "the job should be added with no Result"
        1 * jobResults.put(job, Optional.empty())
        and: "allocation and execution should be delegated to the WorkerPool"
        1 * pool.allocateAndSend(job) >> result
        and: "the correct Result should be set for the job"
        1 * jobResults.replace(job, Optional.of(result)) >> Optional.empty()

        expect: result == jm.execute(job)

        where:
        result << [DISCONNECTED, FAILED, FINISHED]
    }

    def "#execute(job) propogates exceptions correctly"() {
        given:
        def job = Mock(Job)
        0 * _
        and: "allocation throws an exception"
        1 * pool.allocateAndSend(job) >> { throw new WorkerUnavailableException("intended") }
        and: "the job should be added with no result and not have a result set afterwards."
        1 * jobResults.put(job, Optional.empty())

        when: jm.execute(job)
        then: thrown(WorkerUnavailableException)
    }
}
