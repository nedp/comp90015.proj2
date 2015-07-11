package com.github.nedp.comp90015.proj2.job.worker.master.command

import com.github.nedp.comp90015.proj2.job.worker.master.JobManager
import com.github.nedp.comp90015.proj2.job.worker.master.WorkerPool
import spock.lang.Specification

/**
 * Created by nedp on 28/05/15.
 */
class MasterCLITest extends Specification {
  WorkerPool workers
  JobManager jobs
  CommandFactoryProducer factoryProducer
  BufferedReader in_
  PrintStream out

  static def iList = 0
  static def iAdd = 1
  static def iSubmit = 2
  static def iStatus = 3

  CommandFactory listFactory
  CommandFactory addFactory
  CommandFactory submitFactory
  CommandFactory statusFactory
  List<CommandFactory> factories

  Command command

  MasterCLI underTest

  def setup() {
    in_ = Mock BufferedReader
    workers = Mock WorkerPool
    jobs = Mock JobManager
    factoryProducer = Mock CommandFactoryProducer
    out = Mock PrintStream

    listFactory = Mock CommandFactory
    addFactory = Mock CommandFactory
    submitFactory = Mock CommandFactory
    statusFactory = Mock CommandFactory

    factories = [listFactory, addFactory, submitFactory, statusFactory]

    command = Mock Command
  }

  def "should skip empty lines"() {
    given:
    aStandardTestObject()

    when:
    underTest.run()
    then:
    interaction {
      inputIsJust ""
      anythingAllowedWith out
      0 * _
    }
  }

  def "should pass command names to the factory producer"() {
    given:
    aStandardTestObject()
    and:
    interaction {inputIsJust input}

    when:
    underTest.run()
    then:
    1 * factoryProducer.fromName(name) >> Optional.empty()

    where:
    input       || name
    "abc"       || "abc"
    "zxy abc"   || "zxy"
    "zxy 1 2 3" || "zxy"
    "0 12 3"    || "0"
    "0 a"       || "0"
    "list"      || "list"
    "status 0"  || "status"
  }

  def "should pass param scanners to command factories"() {
    given:
    aStandardTestObject()
    and:
    def factory = Mock factoryClass
    and:
    interaction {
      inputIsJust "asdasd"
      producerProduces Optional.of(factory)
    }

    when:
    underTest.run()
    then:
    factory.fromParams(_ as Scanner) >> command

    where:
    factoryClass << [
        ListCommand.Factory,
        AddCommand.Factory,
        SubmitCommand.Factory,
        StatusCommand.Factory,
    ]
  }

  def "should report bad commands"() {
    given:
    aStandardTestObject()
    and:
    interaction {
      producerProduces Optional.empty()
      inputIsJust "asd"
      0 * _ // Ensures that the command is cancelled on empty factory return
    }

    when:
    underTest.run()
    then:
    interaction {anythingAllowedWith out}
  }

  def "should run commands on the job manager, worker manager, and output"() {
    given:
    aStandardTestObject()
    and:
    def factory = Mock factoryClass
    and:
    interaction {
      inputIsJust "asd"
      producerProduces Optional.of(factory)
      factory.fromParams(_ as Scanner) >> command
    }

    when:
    underTest.run()
    then:
    interaction {1 * command.runOn(jobs, workers, out)}

    where:
    factoryClass << [
        ListCommand.Factory,
        AddCommand.Factory,
        SubmitCommand.Factory,
        StatusCommand.Factory,
    ]
  }

  def anythingAllowedWith(def collaborator) {
    collaborator._ >> _
  }

  def aStandardTestObject() {
    underTest = new MasterCLI(workers, jobs, factoryProducer, in_, out, MasterCLI.PROMPT)
  }

  def producerProduces(Optional<CommandFactory> factory) {
    factoryProducer.fromName(_) >> factory
  }

  def inputIsJust(String input) {
    1 * in_.readLine() >> input
    endInput()
  }

  def endInput() {
    1 * in_.readLine() >> null
  }
}
