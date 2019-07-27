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

import com.google.inject.binder.ScopedBindingBuilder

/**
 * An extension of [ScopedBindingBuilder] that enhances the binding DSL to allow binding using reified
 * type parameters.
 *
 * @param self The underlying Guice builder to be extended
 * @see com.google.inject.Binder
 * @author John Leacox
 * @since 1.0
 */
abstract class KotlinScopedBindingBuilder(private val self: ScopedBindingBuilder) :
    ScopedBindingBuilder by self {
    /** The underlying Guice builder that is being extended. */
    open val delegate = self

    /** Places the binding into the scope specified by the annotation type parameter. */
    inline fun <reified TAnn : Annotation> `in`() = delegate.`in`(TAnn::class.java)
}
