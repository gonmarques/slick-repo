package com.byteslounge.slickrepo.meta

import java.time.Instant

import com.byteslounge.slickrepo.datetime.DateTimeHelper


trait VersionGenerator[T]{
  def init() : T
  def next(current : T): T
}

object VersionGenerator {

  implicit object intVersionGenerator extends VersionGenerator[Int]{
    override def init(): Int = 1
    override def next(current: Int): Int = current + 1
  }

  implicit object longVersionGenerator extends  VersionGenerator[Long]{
    override def init(): Long = 1
    override def next(current: Long): Long = current + 1
  }

  implicit object instantVersionGenerator extends  VersionGenerator[Instant]{
    override def init(): Instant = currentInstant()
    override def next(current: Instant): Instant = currentInstant()
  }

  private def currentInstant(): Instant = {
    DateTimeHelper.currentInstant
  }

}
