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

import java.util.concurrent.ConcurrentHashMap

import com.byteslounge.slickrepo.meta.Entity

import scala.collection.concurrent.Map
import scala.collection.convert.decorateAsScala._

/**
 * Aggregates entity lifecycle utility methods.
 */
class LifecycleHelper {

  private[repository] val lifecycleHandlerCache: Map[LifecycleHandlerCacheKey, Boolean] = new ConcurrentHashMap[LifecycleHandlerCacheKey, Boolean]().asScala

  /**
  * Checks if a given repository defines a lifecycle listener.
  */
  def isLifecycleHandlerDefined(clazz: Class[_ <: BaseRepository[_, _]], event: LifecycleEvent): Boolean = {
    val key = LifecycleHandlerCacheKey(clazz, event)
    val entry = lifecycleHandlerCache.get(key)
    if(entry.isDefined) {
      entry.get
    } else {
      val overridden = isHandlerOverridden(clazz, event)
      lifecycleHandlerCache.put(key, overridden)
      overridden
    }
  }

  private def isHandlerOverridden(clazz: Class[_ <: BaseRepository[_, _]], event: LifecycleEvent): Boolean = {
    clazz.getMethod(event.functionName, classOf[Entity[_, _]]).getDeclaringClass != classOf[BaseRepository[_, _]]
  }

}

object LifecycleHelper extends LifecycleHelper

private case class LifecycleHandlerCacheKey(clazz: Class[_ <: BaseRepository[_, _]], event: LifecycleEvent)

sealed abstract class LifecycleEvent(val functionName : String)
case object POSTLOAD extends LifecycleEvent("postLoad")
case object PREPERSIST extends LifecycleEvent("prePersist")
