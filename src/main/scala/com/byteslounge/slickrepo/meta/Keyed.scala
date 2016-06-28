package com.byteslounge.slickrepo.meta

import slick.lifted.Rep

trait Keyed[ID] {
  def id: Rep[ID]
}