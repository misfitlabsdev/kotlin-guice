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

package dev.misfitlabs.kotlinguice4.multibindings

import com.google.inject.Key
import com.google.inject.TypeLiteral

/**
 * A container of the binding keys necessary for map bindings.
 *
 * @author John Leacox
 * @since 1.0
 */
@Suppress("HasPlatformType")
internal class MapBindingSelection<K, V>(
    private val keyType: TypeLiteral<K>,
    private val valueType: TypeLiteral<V>,
    val mapKey: Key<Map<K, V>>
) {
    val providerMapKey by lazy {
        mapKey.ofType(mapOfProviderOf(keyType, valueType))
    }
    val mutableProviderMapKey by lazy { mapKey.ofType(mutableMapOfProviderOf(keyType, valueType)) }

    val javaxProviderMapKey by lazy {
        mapKey.ofType(mapOfJavaxProviderOf(keyType, valueType))
    }
    val mutableJavaxProviderMapKey by lazy {
        mapKey.ofType(mutableMapOfJavaxProviderOf(keyType, valueType))
    }

    val setOfEntryOfJavaxProviderKey by lazy {
        mapKey.ofType(setOf(entryOfJavaxProviderOf(keyType, valueType)))
    }

    val mutableSetOfEntryOfJavaxProviderKey by lazy {
        mapKey.ofType(mutableSetOf(entryOfJavaxProviderOf(keyType, valueType)))
    }

    val collectionOfProviderOfEntryOfProviderKey by lazy {
        mapKey.ofType(collectionOf(providerOf(entryOfProviderOf(keyType, valueType))))
    }
    val mutableCollectionOfProviderOfEntryOfProviderKey by lazy {
        mapKey.ofType(mutableCollectionOf(providerOf(entryOfProviderOf(keyType, valueType))))
    }

    val collectionOfJavaxProviderOfEntryOfProviderKey by lazy {
        mapKey.ofType(collectionOf(javaxProviderOf(entryOfProviderOf(keyType, valueType))))
    }
    val mutableCollectionOfJavaxProviderOfEntryOfProviderKey by lazy {
        mapKey.ofType(mutableCollectionOf(javaxProviderOf(entryOfProviderOf(keyType, valueType))))
    }

    val multimapKey by lazy { mapKey.ofType(mapOf(keyType, setOf(valueType))) }
    val mutableMultimapKey by lazy {
        mapKey.ofType(mutableMapOf(keyType, mutableSetOf(valueType)))
    }

    val providerSetMultimapKey by lazy {
        mapKey.ofType(mapOfSetOfProviderOf(keyType, valueType))
    }
    val mutableProviderSetMultimapKey by lazy {
        mapKey.ofType(mutableMapOfMutableSetOfProviderOf(keyType, valueType))
    }

    val javaxProviderSetMultimapKey by lazy {
        mapKey.ofType(mapOfSetOfJavaxProviderOf(keyType, valueType))
    }
    val mutableJavaxProviderSetMultimapKey by lazy {
        mapKey.ofType(mutableMapOfMutableSetOfJavaxProviderOf(keyType, valueType))
    }

    val providerCollectionMultimapKey by lazy {
        mapKey.ofType(mapOfCollectionOfProviderOf(keyType, valueType))
    }
    val mutableProviderCollectionMultimapKey by lazy {
        mapKey.ofType(mutableMapOfMutableCollectionOfProviderOf(keyType, valueType))
    }

    val javaxProviderCollectionMultimapKey by lazy {
        mapKey.ofType(mapOfCollectionOfJavaxProviderOf(keyType, valueType))
    }
    val mutableJavaxProviderCollectionMultimapKey by lazy {
        mapKey.ofType(mutableMapOfMutableCollectionOfJavaxProviderOf(keyType, valueType))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as MapBindingSelection<*, *>

        if (mapKey != other.mapKey) return false

        return true
    }

    override fun hashCode(): Int {
        return mapKey.hashCode()
    }
}
