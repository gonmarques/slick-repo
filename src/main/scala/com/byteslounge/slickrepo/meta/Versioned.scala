package com.byteslounge.slickrepo.meta

import slick.lifted.Rep

trait Versioned[ID, V] extends Keyed[ID] {
  def version: Rep[V]
}