/*
 * Copyright (C) 2017 John Leacox
 * Copyright (C) 2017 Brian van de Boogaard
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.misfitlabs.kotlinguice4

import com.google.inject.BindingAnnotation
import com.google.inject.Key
import com.google.inject.Provider
import com.google.inject.Scope
import com.google.inject.ScopeAnnotation
import jakarta.inject.Inject
import java.util.concurrent.Callable

interface A {
    fun get(): String
}

class AContainer @Inject constructor(val a: A)

class ACallable : Callable<A> {
    override fun call(): A {
        return AImpl()
    }
}

class TCallable<T> : Callable<T> {
    override fun call(): T? {
        return null
    }
}

open class AImpl : A {
    override fun get(): String {
        return "Impl of A"
    }
}

@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@BindingAnnotation
annotation class Annotated

open class B : AImpl() {
    override fun get(): String {
        return "This is B"
    }
}

@Retention(AnnotationRetention.RUNTIME)
@ScopeAnnotation
annotation class TestScoped

@Suppress("UNCHECKED_CAST")
class TestScope : Scope {
    private var scopedObjectsMap = HashMap<Key<*>, Any>()

    override fun <T> scope(key: Key<T>, unscoped: Provider<T>): Provider<T> {
        return Provider { scopedObjectsMap.getOrPut(key) { unscoped.get() as Any } as T }
    }

    fun reset() {
        scopedObjectsMap = HashMap<Key<*>, Any>()
    }
}

class BProvider : Provider<B> {
    override fun get(): B {
        return B()
    }
}

class TProvider<T> : Provider<T> {
    override fun get(): T? {
        return null
    }
}

object StaticInjectionObj {
    @Inject var staticInjectionSite: String? = null

    fun reset() {
        staticInjectionSite = null
    }
}

class MembersInjection {
    @Inject var memberInjectionSite: String? = null
    @Inject @Annotated var annotatedMemberInjectionSite: String? = null
}
