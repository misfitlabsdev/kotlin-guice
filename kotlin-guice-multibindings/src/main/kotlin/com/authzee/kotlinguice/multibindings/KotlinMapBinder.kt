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

import com.authzee.kotlinguice.KotlinModule
import com.authzee.kotlinguice.binder.KotlinLinkedBindingBuilder
import com.authzee.kotlinguice.multibindings.Element.Type.MAPBINDER
import com.authzee.kotlinguice.typeLiteral
import com.google.inject.Binder
import com.google.inject.Key
import com.google.inject.Provider
import com.google.inject.TypeLiteral
import com.google.inject.multibindings.MapBinder
import com.google.inject.util.Types
import kotlin.collections.Map.Entry

/**
 * A wrapper of [MapBinder] that enhances the binding DSL to allow binding using reified type
 * parameters.
 *
 * By using this class instead of [MapBinder] you can replace the following lines:
 * ```
 * val mapbinder = MapBinder.newMapBinder(binder(), String::class.java, Snack::class.java)
 * mapbinder.addBinding("twix").to(Twix::class.java)
 * ```
 * with
 * ```
 * val mapbinder = KotlinMapBinder.newMapBinder<String, Snack>(kotlinBinder)
 * mapbinder.addBinding("twix").to<Twix>()
 * ```
 *
 * @see MapBinder
 * @author John Leacox
 * @since 1.0
 */
interface KotlinMapBinder<K, V> {
    /**
     * Configures the bound set to silently discard duplicate elements.
     *
     * @see MapBinder.permitDuplicates
     */
    fun permitDuplicates(): KotlinMapBinder<K, V>

    /**
     * Returns a binding builder used to add a new element in the set.
     *
     * @see MapBinder.addBinding
     */
    fun addBinding(key: K): KotlinLinkedBindingBuilder<V>

    companion object {
        /**
         * Returns a new mapbinder that collects entries of [K]/[V] in a {@link Map}.
         */
        inline fun <reified K, reified V> newMapBinder(binder: Binder): KotlinMapBinder<K, V> {
            val keyType = typeLiteral<K>()
            val valueType = typeLiteral<V>()
            val mapBinder = MapBinder.newMapBinder(binder, keyType, valueType)
            return newRealMapBinder(binder,
                    mapBinder,
                    keyType,
                    valueType,
                    Key.get(mapOf(keyType, valueType)))
        }

        /**
         * Returns a new mapbinder that collects entries of [K]/[V] in a {@link Map} that is bound
         * with [TAnn].
         */
        inline fun <reified K, reified V, reified TAnn : Annotation>
                newAnnotatedMapBinder(binder: Binder): KotlinMapBinder<K, V> {
            val keyType = typeLiteral<K>()
            val valueType = typeLiteral<V>()
            val mapBinder = MapBinder.newMapBinder(binder, keyType, valueType, TAnn::class.java)
            return newRealMapBinder(binder,
                    mapBinder,
                    keyType,
                    valueType,
                    Key.get(mapOf(keyType, valueType), TAnn::class.java))
        }

        internal fun <K, V> newRealMapBinder(binder: Binder,
                                             keyType: TypeLiteral<K>,
                                             valueTypeAndAnnotation: Key<V>)
                : RealKotlinMapBinder<K, V> {
            val valueType = valueTypeAndAnnotation.typeLiteral
            val mapKey = valueTypeAndAnnotation.ofType(mapOf(keyType, valueType))
            val mapBinder = when {
                valueTypeAndAnnotation.annotation != null -> MapBinder.newMapBinder(binder,
                        keyType,
                        valueType,
                        valueTypeAndAnnotation.annotation)
                valueTypeAndAnnotation.annotationType != null -> MapBinder.newMapBinder(binder,
                        keyType,
                        valueType,
                        valueTypeAndAnnotation.annotationType)
                else -> MapBinder.newMapBinder(binder, keyType, valueType)
            }
            return newRealMapBinder(binder, mapBinder, keyType, valueType, mapKey)
        }

        @PublishedApi internal fun <K, V> newRealMapBinder(binder: Binder,
                                                           mapBinder: MapBinder<K, V>,
                                                           keyType: TypeLiteral<K>,
                                                           valueType: TypeLiteral<V>,
                                                           mapKey: Key<Map<K, V>>)
                : RealKotlinMapBinder<K, V> {
            val skippingBinder = binder.skipSources(RealKotlinMapBinder::class.java,
                    KotlinMapBinder::class.java,
                    Companion::class.java)
            val kotlinMapBinder = RealKotlinMapBinder(mapBinder,
                    skippingBinder,
                    keyType,
                    valueType,
                    mapKey)
            skippingBinder.install(kotlinMapBinder)
            return kotlinMapBinder
        }
    }
}

internal class RealKotlinMapBinder<K, V>(private val delegate: MapBinder<K, V>,
                                         private val binder: Binder,
                                         private val keyType: TypeLiteral<K>,
                                         private val valueType: TypeLiteral<V>,
                                         mapKey: Key<Map<K, V>>)
    : KotlinMapBinder<K, V>, KotlinModule() {
    private val bindingSelection = MapBindingSelection(keyType, valueType, mapKey)
    private val setName = RealElement.nameOf(mapKey)

    override fun configure() {
        bind(bindingSelection.mapKey).to(bindingSelection.mutableMapKey)
        bind(bindingSelection.providerMapKey).to(bindingSelection.mutableProviderMapKey)
        bind(bindingSelection.javaxProviderMapKey).to(bindingSelection.mutableJavaxProviderMapKey)

        bind(bindingSelection.setOfEntryOfProviderKey)
                .to(bindingSelection.mutableSetOfEntryOfProviderKey)
        bind(bindingSelection.setOfEntryOfJavaxProviderKey)
                .to(bindingSelection.mutableSetOfEntryOfJavaxProviderKey)

        bind(bindingSelection.collectionOfProviderOfEntryOfProviderKey)
                .to(bindingSelection.mutableCollectionOfProviderOfEntryOfProviderKey)
        bind(bindingSelection.collectionOfJavaxProviderOfEntryOfProviderKey)
                .to(bindingSelection.mutableCollectionOfJavaxProviderOfEntryOfProviderKey)
    }

    override fun permitDuplicates(): KotlinMapBinder<K, V> {
        delegate.permitDuplicates()
        binder.install(KotlinMultimapModule(bindingSelection))
        return this
    }

    override fun addBinding(key: K): KotlinLinkedBindingBuilder<V> {
        return KotlinLinkedBindingBuilderImpl<V>(delegate.addBinding(key))
    }

    @Suppress("UNUSED_PARAMETER")
    fun getKeyForNewValue(key: K): Key<V> {
        return Key.get<V>(valueType, RealElement(setName, MAPBINDER, keyType.toString()))
    }

    // Prevents the module from being installed multiple times.
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as RealKotlinMapBinder<*, *>

        if (bindingSelection != other.bindingSelection) return false

        return true
    }

    override fun hashCode(): Int {
        return bindingSelection.hashCode()
    }

    override fun toString(): String {
        return (if (setName.isEmpty()) "" else setName + " ") +
                "KotlinMapBinder<" + keyType + ", " + valueType + ">"
    }
}

@Suppress("UNCHECKED_CAST")
@PublishedApi
internal fun <K, V> mapOf(keyType: TypeLiteral<K>, valueType: TypeLiteral<V>)
        : TypeLiteral<Map<K, V>> {
    return TypeLiteral
            .get(KotlinTypes.mapOf(keyType.type, valueType.type)) as TypeLiteral<Map<K, V>>
}

@Suppress("UNCHECKED_CAST")
internal fun <K, V> mapOfProviderOf(
        keyType: TypeLiteral<K>, valueType: TypeLiteral<V>): TypeLiteral<Map<K, Provider<V>>> {
    return TypeLiteral.get(KotlinTypes.mapOf(keyType.type, Types.providerOf(valueType.type)))
            as TypeLiteral<Map<K, Provider<V>>>
}

@Suppress("UNCHECKED_CAST")
internal fun <K, V> mapOfJavaxProviderOf(keyType: TypeLiteral<K>, valueType: TypeLiteral<V>)
        : TypeLiteral<Map<K, javax.inject.Provider<V>>> {
    val providerType = Types.javaxProviderOf(valueType.type)
    val type = KotlinTypes.mapOf(keyType.type, providerType)
    return TypeLiteral.get(type) as TypeLiteral<Map<K, javax.inject.Provider<V>>>
}

@Suppress("UNCHECKED_CAST")
internal fun <K, V> mapOfSetOfProviderOf(keyType: TypeLiteral<K>, valueType: TypeLiteral<V>)
        : TypeLiteral<Map<K, Set<Provider<V>>>> {
    return TypeLiteral.get(KotlinTypes.mapOf(keyType.type,
            KotlinTypes.setOf(Types.providerOf(valueType.type))))
            as TypeLiteral<Map<K, Set<Provider<V>>>>
}

@Suppress("UNCHECKED_CAST")
internal fun <K, V> mapOfSetOfJavaxProviderOf(keyType: TypeLiteral<K>, valueType: TypeLiteral<V>)
        : TypeLiteral<Map<K, Set<javax.inject.Provider<V>>>> {
    return TypeLiteral.get(KotlinTypes.mapOf(keyType.type,
            KotlinTypes.setOf(Types.javaxProviderOf(valueType.type))))
            as TypeLiteral<Map<K, Set<javax.inject.Provider<V>>>>
}

@Suppress("UNCHECKED_CAST")
internal fun <K, V> mapOfCollectionOfProviderOf(keyType: TypeLiteral<K>, valueType: TypeLiteral<V>)
        : TypeLiteral<Map<K, Collection<Provider<V>>>> {
    return TypeLiteral.get(KotlinTypes.mapOf(keyType.type,
            KotlinTypes.collectionOf(Types.providerOf(valueType.type))))
            as TypeLiteral<Map<K, Collection<Provider<V>>>>
}

@Suppress("UNCHECKED_CAST")
internal fun <K, V> mapOfCollectionOfJavaxProviderOf(
        keyType: TypeLiteral<K>, valueType: TypeLiteral<V>)
        : TypeLiteral<Map<K, Collection<javax.inject.Provider<V>>>> {
    return TypeLiteral.get(KotlinTypes.mapOf(keyType.type,
            KotlinTypes.collectionOf(Types.javaxProviderOf(valueType.type))))
            as TypeLiteral<Map<K, Collection<javax.inject.Provider<V>>>>
}

@Suppress("UNCHECKED_CAST")
internal fun <K, V> entryOfProviderOf(
        keyType: TypeLiteral<K>, valueType: TypeLiteral<V>): TypeLiteral<Entry<K, Provider<V>>> {
    return TypeLiteral.get(
            Types.newParameterizedTypeWithOwner(
                    Map::class.java,
                    Entry::class.java,
                    keyType.type,
                    Types.providerOf(valueType.type))) as TypeLiteral<Entry<K, Provider<V>>>
}

@Suppress("UNCHECKED_CAST")
internal fun <K, V> entryOfJavaxProviderOf(keyType: TypeLiteral<K>, valueType: TypeLiteral<V>)
        : TypeLiteral<Entry<K, javax.inject.Provider<V>>> {
    return TypeLiteral.get(
            Types.newParameterizedTypeWithOwner(
                    Map::class.java,
                    Entry::class.java,
                    keyType.type,
                    Types.javaxProviderOf(valueType.type)))
            as TypeLiteral<Entry<K, javax.inject.Provider<V>>>
}

@Suppress("UNCHECKED_CAST")
internal fun <K, V> mutableMapOf(keyType: TypeLiteral<K>, valueType: TypeLiteral<V>)
        : TypeLiteral<MutableMap<K, V>> {
    return TypeLiteral.get(KotlinTypes.mutableMapOf(keyType.type, valueType.type))
            as TypeLiteral<MutableMap<K, V>>
}

@Suppress("UNCHECKED_CAST")
internal fun <K, V> mutableMapOfProviderOf(keyType: TypeLiteral<K>, valueType: TypeLiteral<V>)
        : TypeLiteral<MutableMap<K, Provider<V>>> {
    return TypeLiteral.get(KotlinTypes.mutableMapOf(keyType.type,
            Types.providerOf(valueType.type))) as TypeLiteral<MutableMap<K, Provider<V>>>
}

@Suppress("UNCHECKED_CAST")
internal fun <K, V> mutableMapOfJavaxProviderOf(keyType: TypeLiteral<K>, valueType: TypeLiteral<V>)
        : TypeLiteral<MutableMap<K, javax.inject.Provider<V>>> {
    return TypeLiteral.get(KotlinTypes.mutableMapOf(keyType.type,
            Types.javaxProviderOf(valueType.type)))
            as TypeLiteral<MutableMap<K, javax.inject.Provider<V>>>
}

@Suppress("UNCHECKED_CAST")
internal fun <K, V> mutableMapOfMutableSetOfProviderOf(
        keyType: TypeLiteral<K>, valueType: TypeLiteral<V>)
        : TypeLiteral<MutableMap<K, MutableSet<Provider<V>>>> {
    return TypeLiteral.get(KotlinTypes.mutableMapOf(keyType.type,
            KotlinTypes.mutableSetOf(Types.providerOf(valueType.type))))
            as TypeLiteral<MutableMap<K, MutableSet<Provider<V>>>>
}

@Suppress("UNCHECKED_CAST")
internal fun <K, V> mutableMapOfMutableSetOfJavaxProviderOf(keyType: TypeLiteral<K>,
                                                            valueType: TypeLiteral<V>)
        : TypeLiteral<MutableMap<K, MutableSet<javax.inject.Provider<V>>>> {
    return TypeLiteral.get(KotlinTypes.mutableMapOf(keyType.type,
            KotlinTypes.mutableSetOf(Types.javaxProviderOf(valueType.type))))
            as TypeLiteral<MutableMap<K, MutableSet<javax.inject.Provider<V>>>>
}

@Suppress("UNCHECKED_CAST")
internal fun <K, V> mutableMapOfMutableCollectionOfProviderOf(keyType: TypeLiteral<K>,
                                                              valueType: TypeLiteral<V>)
        : TypeLiteral<MutableMap<K, MutableCollection<Provider<V>>>> {
    return TypeLiteral.get(KotlinTypes.mutableMapOf(keyType.type,
            KotlinTypes.mutableCollectionOf(Types.providerOf(valueType.type))))
            as TypeLiteral<MutableMap<K, MutableCollection<Provider<V>>>>
}

@Suppress("UNCHECKED_CAST")
internal fun <K, V> mutableMapOfMutableCollectionOfJavaxProviderOf(keyType: TypeLiteral<K>,
                                                                   valueType: TypeLiteral<V>)
        : TypeLiteral<MutableMap<K, MutableCollection<javax.inject.Provider<V>>>> {
    return TypeLiteral.get(KotlinTypes.mutableMapOf(keyType.type,
            KotlinTypes.mutableCollectionOf(Types.javaxProviderOf(valueType.type))))
            as TypeLiteral<MutableMap<K, MutableCollection<javax.inject.Provider<V>>>>
}
