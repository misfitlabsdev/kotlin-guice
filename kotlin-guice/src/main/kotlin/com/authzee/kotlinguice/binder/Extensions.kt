package com.authzee.kotlinguice.binder

import com.google.inject.binder.AnnotatedConstantBindingBuilder
import com.google.inject.binder.ConstantBindingBuilder

/**
 * Binds a constant to the given class using a type parameter.
 *
 * @see com.google.inject.Binder
 * @author John Leacox
 * @since 1.0
 */
inline fun <reified T> ConstantBindingBuilder.to() {
    this.to(T::class.java)
}

/**
 * Binds a constant with an annotation using a type parameter.
 *
 * @see com.google.inject.Binder
 * @author John Leacox
 * @since 1.0
 */
inline fun <reified TAnn : Annotation> AnnotatedConstantBindingBuilder.annotatedWith()
        : ConstantBindingBuilder {
    return this.annotatedWith(TAnn::class.java)
}
