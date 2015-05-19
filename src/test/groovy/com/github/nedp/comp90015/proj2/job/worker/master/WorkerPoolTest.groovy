package com.github.nedp.comp90015.proj2.job.worker.master

import com.github.nedp.comp90015.proj2.job.Job
import spock.lang.Specification

/**
 * Created by nedp on 19/05/15.
 */
class WorkerPoolTest extends Specification {
    WorkerPool wp
    List<Worker> workers

    def setup() {
        workers = Mock List
        wp = new WorkerPool(workers)
    }

    def "Delegates job execution to a Worker"() {
        given: "there is a mock job and worker producing a given result"
        def job = Mock Job
        def worker = Mock Worker
        1 * worker.execute(job) >> result
        and: "the mock worker list 'contains' the mock worker"
        1 * workers.get(_) >> worker
        workers.size() >> 1

        expect: result == wp.allocateAndExecute(job)

        where:
        result << [Result.FINISHED, Result.FAILED, Result.DISCONNECTED]
    }

    def "Uses round robin allocation"() {
        0 * _
        given:
        def workerList = (1..5).collect { Mock(Worker) }
        def jobs = (1..13).collect { Mock(Job) }
        workers.size() >> 5

        when: jobs.each { job -> wp.allocateAndExecute(job) }
        then:
        workerList.eachWithIndex { worker, i ->
            def n = 1 + (12-i).intdiv(5)
            n * workers.get(i % 5) >> workerList[i % 5]
        }
        jobs.eachWithIndex { job, i -> 1 * workerList[i % 5].execute(job) }
    }

    def "Throws WorkerUnavailableExceptions if the pool is empty"() {
        0 * _
        workers.size() >> 0
        when: wp.allocateAndExecute(Mock(Job))
        then: thrown(WorkerUnavailableException)
    }

    def "Throws WorkerUnavailableExceptions if the pool is empty"() {
        0 * _
        workers.size() >> 0
        when: wp.allocateAndExecute(Mock(Job))
        then: thrown(WorkerUnavailableException)
    }
}
