package com.authzee.kotlinguice.binder

import com.google.inject.binder.AnnotatedBindingBuilder

/**
 * An extension of [AnnotatedBindingBuilder] that enhances the binding DSL to allow binding using
 * reified type parameters.
 *
 * @param self The underlying Guice builder to be extended
 * @see com.google.inject.Binder
 * @author John Leacox
 * @since 1.0
 */
@Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE")
abstract class KotlinAnnotatedBindingBuilder<T>(private val self: AnnotatedBindingBuilder<T>)
    : KotlinLinkedBindingBuilder<T>(self), AnnotatedBindingBuilder<T> by self {
    /** The underlying Guice builder that is being extended. */
    override val delegate = self

    /** Binds with the annotation specified by the type parameter. */
    inline fun <reified TAnn : Annotation> annotatedWith(): KotlinLinkedBindingBuilder<T> {
        delegate.annotatedWith(TAnn::class.java)
        return this
    }
}
