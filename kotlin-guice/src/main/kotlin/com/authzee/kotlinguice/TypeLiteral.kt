package com.authzee.kotlinguice

import com.google.inject.TypeLiteral

/**
 * Creates a new [TypeLiteral] for the specified generic type [T].
 *
 * @see TypeLiteral
 * @author John Leacox
 * @since 1.0
 */
inline fun <reified T> typeLiteral() = object : TypeLiteral<T>() {}
