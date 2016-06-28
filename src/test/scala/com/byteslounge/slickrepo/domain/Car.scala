package com.byteslounge.slickrepo.domain

import com.byteslounge.slickrepo.meta.Entity

case class Car(override val id: Option[Int] = None, brand: String) extends Entity[Int]
