package com.github.nedp.comp90015.proj2.job

import spock.lang.Specification

import static com.github.nedp.comp90015.proj2.job.Status.*

/**
 * Created by nedp on 11/05/15.
 */
class StatusTrackerTest extends Specification {
  StatusTracker statusTracker;

  def setup() {
    statusTracker = new StatusTracker();
  }

  def "new -> WAITING"() {
    expect:
    statusTracker.current() == WAITING
  }

  def "start -> RUNNING"() {
    setup:
    statusTracker.start()
    expect:
    statusTracker.current() == RUNNING
  }

  def "finish(_) -> assertion error"() {
    when:
    statusTracker.finish(ok)
    then:
    thrown(AssertionError)

    where:
    ok << [true, false]
  }

  def "start->start -> assertion error"() {
    setup:
    statusTracker.start()

    when:
    statusTracker.start()
    then:
    thrown(AssertionError)
  }

  def "start->finish(ok) -> depends on ok"() {
    setup:
    statusTracker.start()
    statusTracker.finish(ok)

    expect:
    statusTracker.current() == want

    where:
    ok    || want
    true  || FINISHED
    false || FAILED
  }

  def "start->finish(_)->finish(_) -> assertion error"() {
    setup:
    statusTracker.start()
    statusTracker.finish(ok1)

    when:
    statusTracker.finish(ok2)
    then:
    thrown(AssertionError)

    where:
    [ok1, ok2] << GroovyCollections.combinations([true, false], [true, false])
  }
}
