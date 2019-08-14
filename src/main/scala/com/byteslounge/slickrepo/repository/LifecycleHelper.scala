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

import com.byteslounge.slickrepo.exception.{DuplicatedHandlerException, ListenerInvocationException}
import com.byteslounge.slickrepo.meta.Entity

import scala.annotation.StaticAnnotation
import scala.collection.concurrent.Map
import scala.collection.JavaConverters._
import scala.reflect.runtime.universe._

/**
  * Aggregates entity lifecycle utility methods.
  */
class LifecycleHelper {

  private[repository] val lifecycleHandlerCache: Map[LifecycleHandlerCacheKey, Boolean] = new ConcurrentHashMap[LifecycleHandlerCacheKey, Boolean]().asScala

  /**
    * Checks if a given repository defines a lifecycle listener.
    */
  def isLifecycleHandlerDefined(clazz: Class[_ <: BaseRepository[_, _]], handlerType: Class[_ <: StaticAnnotation]): Boolean = {
    lifecycleHandlerCache.get(
      LifecycleHandlerCacheKey(clazz, handlerType)
    ).isDefined
  }

  /**
    * Creates the repository entity lifecycle handler listener
    * for a given lifecycle event type
    */
  def createLifecycleHandler[T <: Entity[T, ID], ID](repo: BaseRepository[T, ID], handlerType: Class[_ <: StaticAnnotation]): (T => T) = {
    val baseRepositoryClass = classOf[BaseRepository[_, _]]
    var methods: Seq[(Type, String)] = Seq()
    var repositoryClass: Class[_] = repo.getClass
    val mirror = runtimeMirror(repositoryClass.getClassLoader)

    while (baseRepositoryClass.isAssignableFrom(repositoryClass)) {
      methods = getHandlerMethods(mirror, repositoryClass, handlerType) ++: methods
      repositoryClass = repositoryClass.getSuperclass
    }

    val handlers = createHandlers[T, ID](mirror, repo, methods)
    if (handlers.nonEmpty) {
      lifecycleHandlerCache.put(LifecycleHandlerCacheKey(repo.getClass, handlerType), true)
    }
    createHandlerChain(handlers, handlerType)
  }

  private def createHandlerChain[T](handlers: Seq[(Type, MethodMirror)], handlerType: Class[_ <: StaticAnnotation]): T => T = {
    x =>
      handlers.foldLeft(x)((acc: T, m: (Type, MethodMirror)) => invokeHandler(acc, m, handlerType))
  }

  private def invokeHandler[T](acc: T, m: (Type, MethodMirror), handlerType: Class[_ <: StaticAnnotation]): T = {
    try {
      m._2(acc).asInstanceOf[T]
    } catch {
      case e @ (_ : IllegalArgumentException | _ : ArrayIndexOutOfBoundsException) =>
        throw new ListenerInvocationException("Error while invoking listener for event type " + handlerType.getSimpleName + " in class " + m._1.baseClasses.head.fullName + ". Confirm that the handler method accepts a single parameter which type is compatible with the repository entity type", e)
    }
  }

  private def createHandlers[T <: Entity[T, ID], ID](mirror: Mirror, repo: BaseRepository[T, ID], methods: Seq[(Type, String)]) = {
    val repoMirror = mirror.reflect(repo)
    methods.map(
      m => (
        m._1,
        repoMirror.reflectMethod(
          m._1.member(newTermName(m._2)).asMethod
        )
      )
    )
  }

  private def getHandlerMethods[R <: BaseRepository[_, _]](mirror: Mirror, repositoryClass: Class[_], handlerType: Class[_ <: StaticAnnotation]): Seq[(Type, String)] = {
    validateHandlerMethods(
      repositoryClass,
      handlerType,
      repositoryClass.getDeclaredMethods
        .map(m => (mirror.staticClass(repositoryClass.getName).selfType, m.getName))
        .filter(m => isEventHandler(m._1, m._2, handlerType))
    )
  }

  private def isEventHandler(repoType: Type, method: String, handlerType: Class[_ <: StaticAnnotation]): Boolean = {
    repoType.member(newTermName(method)) match {
      case m if !m.isMethod => false
      case m =>
        m.asMethod.annotations
          .map(a => a.tpe.baseClasses.head.fullName)
          .contains(handlerType.getName)
    }
  }

  private def validateHandlerMethods(repositoryClass: Class[_], handlerType: Class[_ <: StaticAnnotation], methods: Seq[(Type, String)]): Seq[(Type, String)] = {
    if (methods.size > 1) {
      throw new DuplicatedHandlerException(
        "Only a single event handler for a given event type is allowed in the same repository class. " +
          "Repository class: " + repositoryClass.getSimpleName + ", " +
          "eventType: " + handlerType.getSimpleName
      )
    }
    methods
  }
}

object LifecycleHelper extends LifecycleHelper

private case class LifecycleHandlerCacheKey(clazz: Class[_ <: BaseRepository[_, _]], handlerType: Class[_ <: StaticAnnotation])
