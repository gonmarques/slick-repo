package com.byteslounge.slickrepo.meta


abstract class VersionGenerator[T](){
  def init() : T
  def next(current : T): T
}