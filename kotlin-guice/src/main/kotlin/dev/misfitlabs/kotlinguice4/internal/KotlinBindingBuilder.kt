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

package dev.misfitlabs.kotlinguice4.internal

import com.google.inject.binder.AnnotatedBindingBuilder
import dev.misfitlabs.kotlinguice4.binder.KotlinAnnotatedBindingBuilder
import dev.misfitlabs.kotlinguice4.binder.KotlinLinkedBindingBuilder

/**
 * An internal implementation of the Kotlin enhanced binding builders.
 *
 * @suppress
 * @param self The underlying Guice builder to be extended
 * @author John Leacox
 * @since 1.0
 */
@Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE")
class KotlinBindingBuilder<T>(private val self: AnnotatedBindingBuilder<T>) :
    KotlinAnnotatedBindingBuilder<T>(self), AnnotatedBindingBuilder<T> by self {
    override fun annotatedWith(annotation: Annotation): KotlinLinkedBindingBuilder<T> {
        return super.annotatedWith(annotation)
    }
}
