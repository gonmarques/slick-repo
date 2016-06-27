package com.byteslounge.slickrepo.domain

import com.byteslounge.slickrepo.meta.Entity

case class Parent(override val id: Option[Int] = None, name: String) extends Entity[Int] {
  
}