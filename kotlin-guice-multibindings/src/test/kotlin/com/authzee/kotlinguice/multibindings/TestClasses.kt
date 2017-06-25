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

package com.authzee.kotlinguice.multibindings

import com.google.inject.BindingAnnotation
import java.util.concurrent.Callable
import javax.inject.Inject

interface A {
    fun get(): String
}

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

class BCallable : Callable<A> {
    override fun call(): B {
        return B()
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

// Nested out parameters don't play well with Java. It results in ? extends ? extends...
// To avoid this issue and allow Guice (written in Java) to work well with these
// we must suppress the wildcards.
@JvmSuppressWildcards
class SetContainer<out T> @Inject constructor(val set: Set<T>)
