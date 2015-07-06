package com.github.nedp.comp90015.proj2.job.worker.master

import com.github.nedp.comp90015.proj2.job.Job
import spock.lang.Specification

import static com.github.nedp.comp90015.proj2.job.worker.master.Result.*

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
    0 * _
    given:
    jobIsSubmitted()
    expect:
    resultIsNotPresent()
  }

  def "#execute stores and returns the result of the job"() {
    0 * _
    given:
    jobIsSubmitted()

    when:
    jobIsExecuted()
    then:
    interaction {poolAllocatesProducing expected}

    expect:
    executionResultWas expected
    and:
    storedResultIs expected

    where:
    expected << [DISCONNECTED, FAILED, FINISHED]
  }

  def "#execute propogates exceptions correctly"() {
    0 * _
    given:
    jobIsSubmitted()

    when:
    jobIsExecuted()
    then:
    interaction {emptyPoolAllocationFails()}
    then:
    thrown WorkerUnavailableException

    expect:
    resultIsNotPresent()
  }

  def "#nameOf returns the name of the job"() {
    0 * _
    given:
    jobIsSubmitted()
    and:
    jobHasName expected
    expect:
    reportedNameIs expected
    where:
    expected << ["", " ", "abc", "acasd.jar", "AKSJDHAKJSDHASKJDHASLKJDGASLKDJHASD"]
  }

  def "#hasAllocated returns whether the job has been allocated"() {
    0 * _
    given:
    jobIsSubmitted()
    and:
    interaction {
      if (allocate) {
        poolAllocatesProducing FINISHED
        jobIsExecuted()
      }
    }

    expect:
    testTarget.hasAllocated(id) == allocate

    where:
    allocate << [true, false]
  }

  /*
   * Non-contract behaviour:
   */

  def "#execute, #resultOf, #nameOf, and #hasAllocated throw an exception for untracked Jobs"() {
    0 * _
    given: "the job was not submitted"; id = 0

    when:
    testTarget.execute(id)
    then:
    thrown IndexOutOfBoundsException

    when:
    testTarget.nameOf(id)
    then:
    thrown IndexOutOfBoundsException

    when:
    testTarget.resultOf(id)
    then:
    thrown IndexOutOfBoundsException

    when:
    testTarget.hasAllocated(id)
    then:
    thrown IndexOutOfBoundsException
  }

  def "#execute throws an exception for already-allocated Jobs"() {
    0 * _
    given:
    jobIsSubmitted()
    and:
    poolAllocatesProducing expected
    and:
    jobIsExecuted()

    expect:
    executionResultWas expected

    when:
    jobIsExecuted()
    then:
    thrown IllegalStateException

    where:
    expected << [FINISHED, FAILED, DISCONNECTED]
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

  def jobHasName(String name) {
    job.name() >> name
  }

  def poolAllocatesProducing(Result result) {
    1 * pool.allocateAndExecute(job) >> result
  }

  def emptyPoolAllocationFails() {
    1 * pool.allocateAndExecute(job) >> {throw new WorkerUnavailableException()}
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
