package com.authzee.kotlinguice.binder

import com.authzee.kotlinguice.typeLiteral
import com.google.inject.binder.LinkedBindingBuilder
import javax.inject.Provider

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
abstract class KotlinLinkedBindingBuilder<T>(private val self: LinkedBindingBuilder<T>)
    : KotlinScopedBindingBuilder(self), LinkedBindingBuilder<T> by self {
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
