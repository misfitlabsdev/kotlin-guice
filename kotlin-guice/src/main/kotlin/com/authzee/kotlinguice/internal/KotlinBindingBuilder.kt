package com.authzee.kotlinguice.internal

import com.authzee.kotlinguice.binder.KotlinAnnotatedBindingBuilder
import com.google.inject.binder.AnnotatedBindingBuilder

/**
 * An internal implementation of the Kotlin enhanced binding builders.
 *
 * @suppress
 * @param self The underlying Guice builder to be extended
 * @author John Leacox
 * @since 1.0
 */
@Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE")
class KotlinBindingBuilder<T>(private val self: AnnotatedBindingBuilder<T>)
    : KotlinAnnotatedBindingBuilder<T>(self), AnnotatedBindingBuilder<T> by self {
}
