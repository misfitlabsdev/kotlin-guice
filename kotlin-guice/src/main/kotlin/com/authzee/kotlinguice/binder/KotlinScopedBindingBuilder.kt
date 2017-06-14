package com.authzee.kotlinguice.binder

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
abstract class KotlinScopedBindingBuilder(private val self: ScopedBindingBuilder)
    : ScopedBindingBuilder by self {
    /** The underlying Guice builder that is being extended. */
    open val delegate = self

    /** Places the binding into the scope specified by the annotation type parameter. */
    inline fun <reified TAnn : Annotation> `in`() = delegate.`in`(TAnn::class.java)
}
