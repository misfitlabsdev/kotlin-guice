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

package dev.misfitlabs.kotlinguice4.binder

import com.google.inject.binder.LinkedBindingBuilder
import dev.misfitlabs.kotlinguice4.typeLiteral
import jakarta.inject.Provider

/**
 * An extension of [LinkedBindingBuilder] that enhances the binding DSL to allow binding using reified
 * type parameters.
 *
 * @param self The underlying Guice builder to be extended
 * @see com.google.inject.Binder
 * @author John Leacox
 * @since 1.0
 */
@Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE")
abstract class KotlinLinkedBindingBuilder<T>(private val self: LinkedBindingBuilder<T>) :
    KotlinScopedBindingBuilder(self), LinkedBindingBuilder<T> by self {
    /** The underlying Guice builder that is being extended. */
    override val delegate = self

    /** Binds to the implementation class specified by the type parameter. */
    inline fun <reified TImpl : T> to(): KotlinScopedBindingBuilder {
        delegate.to(typeLiteral<TImpl>())
        return this
    }

    /** Binds to the provider class specified by the type parameter. */
    inline fun <reified TProvider : Provider<out T>> toProvider(): KotlinScopedBindingBuilder {
        delegate.toProvider(typeLiteral<TProvider>())
        return this
    }
}
