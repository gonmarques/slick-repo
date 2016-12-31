/*
 * Copyright 2016 byteslounge.com (Gonçalo Marques).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.byteslounge.slickrepo.utils

import org.scalatest.FlatSpec
import org.scalatest.Matchers

class ClassUtilsTest extends FlatSpec with Matchers {

  "The Class Utils" should "assert that two instances are of the same class" in {
    val one: Int = 3
    val other: Int = 4
    val result = ClassUtils.sameClass(one, other)
    result should equal (true)
  }

  it should "assert that two instances are not of the same class" in {
    val one: Int = 3
    val other: Long = 4
    val result = ClassUtils.sameClass(one, other)
    result should equal (false)
  }

}