/*
 * Copyright (C) 2017 John Leacox
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

package com.authzee.kotlinguice.multibindings

import com.google.inject.util.Types
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * Utility methods for building kotlin specific types.
 *
 * @author John Leacox
 * @since 1.0
 */
internal object KotlinTypes {
    internal fun collectionOf(elementType: Type): ParameterizedType {
        return Types.newParameterizedType(
                kotlin.collections.Collection::class.java,
                Types.subtypeOf(elementType))
    }

    internal fun mutableCollectionOf(elementType: Type): ParameterizedType {
        return Types.newParameterizedType(
                kotlin.collections.MutableCollection::class.java,
                elementType)
    }

    internal fun setOf(elementType: Type): ParameterizedType {
        return Types.newParameterizedType(
                kotlin.collections.Set::class.java,
                Types.subtypeOf(elementType))
    }

    internal fun mutableSetOf(elementType: Type): ParameterizedType {
        return Types.newParameterizedType(
                kotlin.collections.MutableSet::class.java,
                elementType)
    }

    internal fun mapOf(keyType: Type, valueType: Type): ParameterizedType {
        return Types.newParameterizedType(
                Map::class.java,
                keyType,
                Types.subtypeOf(valueType))
    }

    internal fun mutableMapOf(keyType: Type, valueType: Type): ParameterizedType {
        return Types.newParameterizedType(
                MutableMap::class.java,
                keyType,
                valueType)
    }
}

