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

import com.google.inject.binder.AnnotatedElementBuilder
import kotlin.reflect.KClass

/**
 * An extension of [AnnotatedElementBuilder] that enhances the binding DSL to allow binding using
 * reified type parameters.
 *
 * @param delegate The underlying Guice builder to be extended
 * @see com.google.inject.Binder
 * @author John Leacox
 * @since 1.0
 */
class KotlinAnnotatedElementBuilder(val delegate: AnnotatedElementBuilder) :
    AnnotatedElementBuilder by delegate {
    /** Binds with the annotation specified by the type parameter. */
    inline fun <reified TAnn : Annotation> annotatedWith() {
        delegate.annotatedWith(TAnn::class.java)
    }

    /** Binds with the annotation specified by the class. */
    fun annotatedWith(annotation: KClass<out Annotation>) {
        delegate.annotatedWith(annotation.java)
    }
}
