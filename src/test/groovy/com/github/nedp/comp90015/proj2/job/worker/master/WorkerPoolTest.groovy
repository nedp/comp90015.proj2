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

    def "Sends Jobs to a RemoteWorker"() {
        given:
        def job = Mock Job
        def worker = Mock Worker
        1 * workers.get(_) >> worker

        when: wp.allocateAndExecute(job)
        then: 1 * worker.execute(job)
    }

    def "Uses round robin allocation"() {
        0 * _
        given:
        def workerList = (1..5).collect { Mock(Worker) }
        def jobs = (1..13).collect { Mock(Job) }

        when: jobs.each { job -> wp.allocateAndExecute(job) }
        then:
        13 * workers.size() >> 5
        workerList.eachWithIndex { worker, i ->
            def n = 1 + (12-i).intdiv(5)
            n * workers.get(i % 5) >> workerList[i % 5]
        }
        jobs.eachWithIndex { job, i -> 1 * workerList[i % 5].execute(job) }
    }
}
