package com.github.nedp.comp90015.proj2.job

import spock.lang.Specification

/**
 * Created by nedp on 10/05/15.
 */
class StatusTest extends Specification {
  def "good transitions work"() {
    expect:
    status.nextState(ok) == want

    where:
    status         | ok    || want
    Status.WAITING | true  || Status.RUNNING
    Status.WAITING | false || Status.FAILED
    Status.RUNNING | true  || Status.FINISHED
    Status.RUNNING | false || Status.FAILED
  }

  def "bad transitions fail"() {
    when:
    status.nextState(ok)

    then:
    def e = thrown(want.class)

    expect:
    e.getMessage() == want.getMessage()

    where:
    status          | ok    || want
    Status.FAILED   | true  || new IllegalStateException("this: FAILED, ok: true")
    Status.FAILED   | false || new IllegalStateException("this: FAILED, ok: false")
    Status.FINISHED | true  || new IllegalStateException("this: FINISHED, ok: true")
    Status.FINISHED | false || new IllegalStateException("this: FINISHED, ok: false")
  }
}
