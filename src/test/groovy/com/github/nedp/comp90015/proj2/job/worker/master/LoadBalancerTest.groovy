package com.github.nedp.comp90015.proj2.job.worker.master

import com.github.nedp.comp90015.proj2.job.Job
import spock.lang.Specification

import static com.github.nedp.comp90015.proj2.job.worker.master.Worker.Status.*

/**
 * Created by nedp on 29/05/15.
 */
class LoadBalancerTest extends Specification {
    LoadBalancer underTest

    Collection<Worker> workers
    Worker worker
    Job job

    def setup() {
        job = Stub Job
        worker = Stub Worker
        workers = []
    }

    def "should not choose a worker when there are no workers"() {
        given: noWorkers()
        and: aStandardSystemUnderTest()

        expect: thatNoWorkerIsChosen()
    }

    def "should not choose a worker if all workers have no free memory"() {
        given: interaction { aFullWorker() }
        and: aStandardSystemUnderTest()
        expect: thatNoWorkerIsChosen()
    }

    def "should not choose a worker if all workers are disconnected"() {
        given: interaction { aDisconnectedWorker() }
        and: aStandardSystemUnderTest()
        expect: thatNoWorkerIsChosen()
    }

    def "should allocate to the RUNNING worker with the most free memory"() {
        given: interaction {
            workersWith freeMemories, statuses
        }
        and: aStandardSystemUnderTest()
        expect: thatTheChosenWorkerIs workers[expected]

        where:
        freeMemories  | statuses                                       || expected
        [0, 1, 2, 3]  | [RUNNING, RUNNING, RUNNING, RUNNING]           || 3
        [0, 4, 3, 2]  | [RUNNING, DISCONNECTED, RUNNING, RUNNING]      || 2
        [10, 1, 0, 0] | [RUNNING, RUNNING, DISCONNECTED, DISCONNECTED] || 0
    }

    def aStandardSystemUnderTest() {
        underTest = new LoadBalancer(workers);
    }

    def workersWith(List<Long> freeMemories, List<Worker.Status> statuses) {
        workers = (0..freeMemories.size()-1).collect {
            def worker = Mock Worker
            worker.status() >> statuses[it]
            worker.freeMemory() >> freeMemories[it]
            worker
        }
    }

    def noWorkers() {
        workers = []
    }

    def aFullWorker() {
        worker.freeMemory() >> 0
        worker.status() >> RUNNING
        workers = [worker]
    }

    def aDisconnectedWorker() {
        worker.status() >> DISCONNECTED
        workers = [worker]
    }

    def thatNoWorkerIsChosen() {
        Optional.empty() == underTest.workerFor(job)
    }

    def thatTheChosenWorkerIs(Worker expected) {
        underTest.workerFor(job).get() == expected
    }
}
