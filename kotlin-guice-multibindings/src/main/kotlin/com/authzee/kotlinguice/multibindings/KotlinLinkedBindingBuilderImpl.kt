package com.authzee.kotlinguice.multibindings

import com.authzee.kotlinguice.binder.KotlinLinkedBindingBuilder
import com.google.inject.binder.LinkedBindingBuilder

/**
 * An internal concrete implementation of [KotlinLinkedBindingBuilder] for usage in the multibinding
 * factory methods.
 *
 * @author John Leacox
 * @since 1.0
 */
internal class KotlinLinkedBindingBuilderImpl<T>(delegate: LinkedBindingBuilder<T>)
    : KotlinLinkedBindingBuilder<T>(delegate)
