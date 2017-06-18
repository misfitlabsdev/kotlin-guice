package com.authzee.kotlinguice.binder

import com.google.inject.binder.AnnotatedElementBuilder

/**
 * An extension of [AnnotatedElementBuilder] that enhances the binding DSL to allow binding using
 * reified type parameters.
 *
 * @param delegate The underlying Guice builder to be extended
 * @see com.google.inject.Binder
 * @author John Leacox
 * @since 1.0
 */
class KotlinAnnotatedElementBuilder(val delegate: AnnotatedElementBuilder)
    : AnnotatedElementBuilder by delegate {
    /** Binds with the annotation specified by the type parameter. */
    inline fun <reified TAnn : Annotation> annotatedWith() {
        delegate.annotatedWith(TAnn::class.java)
    }
}
