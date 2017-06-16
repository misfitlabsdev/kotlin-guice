package com.authzee.kotlinguice

import com.google.inject.Key
import java.lang.reflect.Type

/**
 * Gets a key for an injection of type [T].
 *
 * @author John Leacox
 * @since 1.0
 */
inline fun <reified T> key(): Key<T> = Key.get(typeLiteral<T>())

/**
 * Gets a key for an injection of type [T] with annotation type [TAnn].
 *
 * @author John Leacox
 * @since 1.0
 */
inline fun <reified T, reified TAnn : Annotation> annotatedKey(): Key<T> = Key.get(typeLiteral<T>(), TAnn::class.java)

/**
 * Gets a key for an injection of type [T] and the specified annotation.
 *
 * @author John Leacox
 * @since 1.0
 */
inline fun <reified T> annotatedKey(annotation: Annotation): Key<T> = Key.get(typeLiteral<T>(), annotation)

/**
 * Gets a key for an injection of the specified type and annotation type [TAnn].
 *
 * @author John Leacox
 * @since 1.0
 */
inline fun <reified TAnn : Annotation> annotatedKey(type: Type): Key<*> = Key.get(type, TAnn::class.java)

/**
 * Returns a new key of the specified type [T] with the same annotation as this key.
 *
 * @author John Leacox
 * @since 1.0
 */
inline fun <reified T> Key<*>.ofType(): Key<T> = this.ofType(typeLiteral<T>())
