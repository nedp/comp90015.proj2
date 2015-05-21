package com.github.nedp.comp90015.proj2.job.worker.master

import com.github.nedp.comp90015.proj2.job.Job
import spock.lang.Specification

import static com.github.nedp.comp90015.proj2.job.worker.master.WorkerStatus.*

/**
 * Created by nedp on 19/05/15.
 */
class WorkerPoolTest extends Specification {
    List<Worker> workerList

    def setup() {
        workerList = new ArrayList<Worker>();
    }

    def "Delegates job execution to a Worker"() {
        given: "there is a mock job and worker producing a given result"
        def job = Mock Job
        def worker = Mock Worker
        1 * worker.execute(job) >> result
        worker.status() >> RUNNING

        and: "the worker list 'contains' the mock worker"
        workerList.add(worker);
        def wp = new WorkerPool(workerList);

        expect: result == wp.allocateAndExecute(job)

        where:
        result << [Result.FINISHED, Result.FAILED, Result.DISCONNECTED]
    }

    def "Uses round robin allocation"() {
        0 * _
        given: "There are 13 jobs for 5 RUNNING Workers"
        workerList = (1..5).collect {
            def worker = Mock Worker
            worker.status() >> RUNNING
            worker
        }
        def jobs = (1..13).collect { Mock(Job) }
        def wp = new WorkerPool(workerList)

        when: jobs.each { job -> wp.allocateAndExecute(job) }
        then: "each job should have been allocated to the appropriate worker"
        jobs.eachWithIndex { job, i -> 1 * workerList[i % 5].execute(job) >> Result.FINISHED }
    }

    def "Throws WorkerUnavailableExceptions if the pool is empty"() {
        0 * _
        assert workerList.isEmpty()
        given: def wp = new WorkerPool(workerList)
        when: wp.allocateAndExecute(Mock(Job))
        then: thrown(WorkerUnavailableException)
    }

    def "Unique Workers may be added to the pool"() {
        0 * _
        given:
        def worker = Mock Worker
        if (alreadyPresent) {
            workerList.add(worker);
        }
        def wp = new WorkerPool(workerList)

        expect: "can only add a worker if not present"
        wp.add(worker) != alreadyPresent
        and: "can't add the same worker twice"
        !wp.add(worker)
        and: "can always add a completely new worker"
        wp.add(Mock(Worker))

        where: alreadyPresent << [true, false]
    }

    def "A list of known workers is available"() {
        0 * _
        given:
        workerList = (1..5).collect { Mock(Worker) }
        def wp = new WorkerPool(workerList)

        expect: wp.workerList() == workerList
    }

    def "Disconnected workers don't have Jobs allocated to them"() {
        0 * _
        given:
        workerList = eachIsRunning.collect { isRunning ->
            def worker = Mock Worker
            worker.status() >> (isRunning ? RUNNING : DISCONNECTED)
            worker
        }
        def wp = new WorkerPool(workerList)
        def jobs = (0..5).collect { Mock(Job) }

        when: jobs.each { job -> wp.allocateAndExecute(job) }
        then: "only workers which are running may recieve jobs"
        workerList.eachWithIndex { worker, i ->
            if (eachIsRunning[i]) {
                worker.execute(_) >> Result.FINISHED
            }
        }

        where:
        eachIsRunning << [
            [false, false, false, false, true],
            [false, false, false, true, false],
            [false, false, true, false, false],
            [false, true, false, false, false],
            [true, false, false, false, false],
            [true, true, true, true, true],
            [true, true, true, true, false],
        ]
    }

    def "throw an exception if all workers are disconnected"() {
        0 * _
        given:
        workerList = (0..10).collect { Mock(Worker) }
        _.status() >> DISCONNECTED
        def wp = new WorkerPool(workerList)
        def job = Mock Job

        when: wp.allocateAndExecute(job)
        then: thrown(WorkerUnavailableException)
    }
}
