package com.github.nedp.comp90015.proj2.job.worker.master

import com.github.nedp.comp90015.proj2.job.Job
import spock.lang.Specification

import static com.github.nedp.comp90015.proj2.job.worker.master.Worker.Status.DOWN
import static com.github.nedp.comp90015.proj2.job.worker.master.Worker.Status.RUNNING


/**
 * Created by nedp on 19/05/15.
 */
class WorkerPoolTest extends Specification {
  WorkerPool underTest
  WorkerPool startedEmpty
  Job job
  Collection<Job> jobs
  Worker worker
  Collection<Worker> workers
  Collection<Result> results

  def setup() {
    job = Stub Job
    jobs = new ArrayList<>();
    worker = Mock Worker
    workers = new ArrayList<>();
    results = new ArrayList<>();
  }

  def "Delegates job execution to a Worker"() {
    0 * _
    given:
    workersAre RUNNING
    and:
    poolsStartEmpty()
    and:
    poolsAdd worker

    when:
    poolsAllocateAndExecute job
    then:
    interaction {workerExecutesPerPool job, expected}
    expect:
    resultsAre expected

    where:
    expected << [Result.FINISHED, Result.FAILED, Result.DISCONNECTED]
  }

  def "Uses round robin allocation"() {
    0 * _
    given:
    workerCountIs 5
    and:
    workersAre RUNNING
    and:
    poolsStartWith workers
    and:
    jobCountIs 13

    when:
    poolsAllocateAndExecuteJobs()
    then:
    interaction {allocationIsRoundRobinReturning expected}

    where:
    expected << [Result.FINISHED, Result.FAILED, Result.DISCONNECTED]
  }

  def "Throws WorkerUnavailableExceptions if the pool is empty"() {
    0 * _
    given:
    poolsStartEmpty()
    when:
    poolsAllocateAndExecute job
    then:
    thrown WorkerUnavailableException
  }

  def "Unique Workers may be added to the pool"() {
    0 * _
    given:
    poolsStartWith alreadyPresent ? [worker] : []

    expect:
    poolsAddIf worker, !alreadyPresent
    and:
    poolsCantAdd worker
    and:
    poolsAdd Stub(Worker)

    where:
    alreadyPresent << [true, false]
  }

  def "A list of known workers is available"() {
    0 * _
    given:
    workerCountIs 5
    and:
    poolsStartWith workers
    expect:
    poolsContainOnly workers
  }

  def "Disconnected workers don't have Jobs allocated to them"() {
    0 * _
    given:
    workerCountIs 4
    and:
    workersHaveStatuses statuses
    and:
    poolsStartWith workers
    and:
    jobCountIs 4

    when:
    poolsAllocateAndExecuteJobs()
    then:
    interaction {workersFinishJobsIfRunning()}
    expect:
    resultsAre Result.FINISHED

    where:
    statuses << [
        [DOWN, DOWN, DOWN, RUNNING],
        [DOWN, DOWN, RUNNING, DOWN],
        [DOWN, DOWN, RUNNING, RUNNING],
        [DOWN, RUNNING, DOWN, DOWN],
        [DOWN, RUNNING, DOWN, RUNNING],
        [DOWN, RUNNING, RUNNING, DOWN],
        [DOWN, RUNNING, RUNNING, RUNNING],
        [RUNNING, DOWN, DOWN, DOWN],
        [RUNNING, DOWN, DOWN, RUNNING],
        [RUNNING, DOWN, RUNNING, DOWN],
        [RUNNING, DOWN, RUNNING, RUNNING],
        [RUNNING, RUNNING, DOWN, DOWN],
        [RUNNING, RUNNING, DOWN, RUNNING],
        [RUNNING, RUNNING, RUNNING, DOWN],
        [RUNNING, RUNNING, RUNNING, RUNNING],
    ]
  }

  def "throw an exception if all workers are disconnected"() {
    0 * _
    given:
    workerCountIs 10
    and:
    workersAre DOWN
    and:
    poolsStartWith workers

    when:
    poolsAllocateAndExecute job
    then:
    thrown WorkerUnavailableException
  }

  /*
   * Helpers
   */

  def workersAre(Worker.Status status) {
    workers.each {it.status() >> status}
    worker.status() >> status
  }

  def workersHaveStatuses(ArrayList<Worker.Status> statuses) {
    workers.eachWithIndex {worker, i -> worker.status() >> statuses[i]}
  }

  def workerCountIs(int n) {
    workers.addAll((1..n).collect {Mock(Worker)})
  }

  def jobCountIs(int n) {
    jobs.addAll n.collect {Stub(Job)}
  }

  def poolsStartEmpty() {
    underTest = new WorkerPool(new ArrayList())
    startedEmpty = new WorkerPool()
  }

  def poolsStartWithJust(Worker worker) {
    underTest = new WorkerPool([worker])
    startedEmpty = new WorkerPool()
    assert startedEmpty.add(worker)
  }

  def poolsStartWith(Collection<Worker> workers) {
    underTest = new WorkerPool(workers)
    startedEmpty = new WorkerPool()
    workers.each {assert startedEmpty.add(it)}
  }

  def poolsAdd(Worker worker) {
    assert underTest.add(worker)
    assert startedEmpty.add(worker)
    true
  }

  def poolsAddIf(Worker worker, boolean condition) {
    assert underTest.add(worker) == condition
    assert startedEmpty.add(worker) == condition
    true
  }

  def poolsCantAdd(Worker worker) {
    assert !underTest.add(worker)
    assert !startedEmpty.add(worker)
    true
  }

  def poolsContainOnly(Collection<Worker> workers) {
    assert underTest.workerList().containsAll(workers)
    assert workers.containsAll(underTest.workerList())
    true
  }

  def resultsAre(Result expected) {
    results.each {assert it == expected}
    true
  }

  def poolsAllocateAndExecute(Job job) {
    results.add underTest.allocateAndExecute(job)
    results.add startedEmpty.allocateAndExecute(job)
  }

  def poolsAllocateAndExecuteJobs() {
    jobs.each {
      results.add underTest.allocateAndExecute(it)
      results.add startedEmpty.allocateAndExecute(it)
    }
  }

  def allocationIsRoundRobinReturning(Result result) {
    jobs.eachWithIndex {job, i -> 2 * workers[i % 5].execute(job) >> result}
  }

  def workerExecutesPerPool(Job job, Result result) {
    2 * worker.execute(job) >> result
  }

  def workersFinishJobsIfRunning() {
    workers.each {
      if (it.status() == RUNNING) {
        it.execute(_ as Job) >> Result.FINISHED
      }
    }
  }
}
