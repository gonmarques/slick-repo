package com.byteslounge.slickrepo.datetime
import java.time.Instant

object MockDateTimeHelper {
  def start(): Unit = {
    MockDateTimeProvider.reset()
    DateTimeHelper.setDateTimeProvider(MockDateTimeProvider)
  }
  def mock(instants: Instant*): Unit = {
    MockDateTimeProvider.mock(instants)
  }
  def restore(): Unit = {
    DateTimeHelper.restore()
  }
}

private object MockDateTimeProvider extends DateTimeProvider {
  var instants: Seq[Instant] = _
  def reset(): Unit = {
    instants = Seq()
  }
  override def currentInstant: Instant = {
    val result: Instant = instants.head
    instants = instants.tail
    result
  }
  def mock(instants: Seq[Instant]): Unit = {
    instants.foreach(instant => this.instants = this.instants :+ instant)
  }
}
