package com.github.nedp.comp90015.proj2.job

import org.junit.Test

import static groovy.test.GroovyAssert.shouldFail

/**
 * Created by nedp on 10/05/15.
 */
class StatusTest {

    @Test
    void testNextState() {
        def NO_FAILURE = true
        def FAILURE = false
        def cases = [
                [Status.WAITING, NO_FAILURE]:  Status.RUNNING,
                [Status.WAITING, FAILURE]:     Status.FAILED,
                [Status.RUNNING, NO_FAILURE]:  Status.FINISHED,
                [Status.RUNNING, FAILURE]:     Status.FAILED,
                [Status.FINISHED, NO_FAILURE]: new IllegalStateException("this: FINISHED, ok: true"),
                [Status.FINISHED, FAILURE]:    new IllegalStateException("this: FINISHED, ok: false"),
        ]

        def test = { status_ok, want ->
            def (status, ok) = status_ok
            assert status.nextState(ok) == want
        }

        cases.each{ status_ok, want ->
            if (want instanceof Throwable) {
                def e = shouldFail want.getClass(), {
                    test status_ok, want
                }
                assert e.getMessage() == want.getMessage()
            } else {
                test status_ok, want
            }
        }
    }
}
