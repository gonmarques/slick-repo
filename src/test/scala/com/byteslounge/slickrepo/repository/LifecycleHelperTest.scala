/*
 * MIT License
 *
 * Copyright (c) 2017 Gonçalo Marques
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.byteslounge.slickrepo.repository

import com.byteslounge.slickrepo.annotation.postLoad
import com.byteslounge.slickrepo.test.{H2Config, LifecycleEntityRepositoryPostLoad}
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}

class LifecycleHelperTest extends FlatSpec with BeforeAndAfter with Matchers {

  "The LifecycleHelper" should "detect that an entity does not define a handler" in {
    new PersonRepository(H2Config.config.driver)
    LifecycleHelper.isLifecycleHandlerDefined(classOf[PersonRepository], classOf[postLoad]) should equal(false)
  }

  it should "detect that an entity defines a handler" in {
    new LifecycleEntityRepositoryPostLoad(H2Config.config.driver)
    LifecycleHelper.isLifecycleHandlerDefined(classOf[LifecycleEntityRepositoryPostLoad], classOf[postLoad]) should equal(true)
  }
}
