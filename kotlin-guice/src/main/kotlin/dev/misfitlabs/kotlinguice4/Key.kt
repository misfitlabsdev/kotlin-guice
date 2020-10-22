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

package dev.misfitlabs.kotlinguice4

import com.google.inject.Key
import java.lang.reflect.Type
import kotlin.reflect.KClass
import kotlin.reflect.jvm.javaType
import kotlin.reflect.typeOf

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

@ExperimentalStdlibApi
inline fun <reified T> kotlinTypeKey(
    annotation: Annotation? = null,
    annotationType: KClass<out Annotation>? = null
): Key<T> {
    if (annotation != null) require(annotationType == null)
    if (annotationType != null) require(annotation == null)

    val ktype = typeOf<T>()
    val baseType = ktype.javaType

    val result = when {
        annotation != null -> Key.get(baseType, annotation)
        annotationType != null -> Key.get(baseType, annotationType.java)
        else -> Key.get(baseType)
    }

    @Suppress("UNCHECKED_CAST")
    return result as Key<T>
}

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
