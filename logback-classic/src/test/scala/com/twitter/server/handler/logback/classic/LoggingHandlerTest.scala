package com.twitter.server.handler.logback.classic

import ch.qos.logback.classic.{Level, Logger}
import com.twitter.conversions.time._
import com.twitter.finagle.http.Request
import com.twitter.util.Await
import com.twitter.util.logging.{Logger => UtilLogger}
import org.scalatest.FunSuite
import org.slf4j.LoggerFactory

class LoggingHandlerTest extends FunSuite {

  private val handler = new LoggingHandler()

  test("sees logger created by implicit ClassTag") {
    class ClassTagLogger()

    val log = UtilLogger[ClassTagLogger]

    assert(handler.loggers.map(_.getName).contains(classOf[ClassTagLogger].getName))
  }

  test("sees logger created for the given name") {
    val log = UtilLogger("name")

    assert(handler.loggers.map(_.getName).contains("name"))
  }

  test("sees logger created for given class") {
    class SomeClass()

    val log = UtilLogger(classOf[SomeClass])

    assert(handler.loggers.map(_.getName).contains(classOf[SomeClass].getName))
  }

  test("sees logger created to wrap an underlying org.slf4j.Logger") {
    val underlying = LoggerFactory.getLogger("name")
    val log = UtilLogger(underlying)

    assert(handler.loggers.map(_.getName).contains(underlying.getName))
  }

  test("can set logging levels for logger created by implicit ClassTag") {
    class ClassTagLogger()

    val log = UtilLogger[ClassTagLogger]

    val loggerName = classOf[ClassTagLogger].getName
    val logger = LoggerFactory.getLogger(loggerName).asInstanceOf[Logger]

    logger.setLevel(Level.WARN)
    assert(handler.loggers.filter(_.getName == loggerName).head.getLevel() == Level.WARN)

    logger.setLevel(Level.DEBUG)
    assert(handler.loggers.filter(_.getName == loggerName).head.getLevel() == Level.DEBUG)
  }

  test("override can be reset") {
    val req = Request(("logger", "foo"), ("level", "null"))
    val logger = LoggerFactory.getLogger("foo").asInstanceOf[Logger]

    assert(logger.getLevel == null)

    logger.setLevel(Level.DEBUG)
    Await.result(handler(req), 5.seconds)

    assert(logger.getLevel == null)
  }

}
