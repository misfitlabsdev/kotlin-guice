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

import com.google.inject.Binder
import com.google.inject.Key
import com.google.inject.Provider
import com.google.inject.TypeLiteral
import com.google.inject.multibindings.Multibinder
import com.google.inject.util.Types
import dev.misfitlabs.kotlinguice4.KotlinModule
import dev.misfitlabs.kotlinguice4.annotatedKey
import dev.misfitlabs.kotlinguice4.binder.KotlinLinkedBindingBuilder
import dev.misfitlabs.kotlinguice4.key
import dev.misfitlabs.kotlinguice4.multibindings.Element.Type.MULTIBINDER

/**
 * A wrapper of [Multibinder] that enhances the binding DSL to allow binding using reified type
 * parameters.
 *
 * By using this class instead of [Multibinder] you can replace the following lines:
 * ```
 * val multibinder = Multibinder.newSetBinder(binder(), Snack::class)
 * multibinder.addBinding().to(Twix::class.java)
 * ```
 * with
 * ```
 * val multibinder = KotlinMultibinder.newSetBinder<Snack>(kotlinBinder)
 * multibinder.addBinding().to<Twix>()
 * ```
 *
 * @see Multibinder
 * @author John Leacox
 * @since 1.0
 */
interface KotlinMultibinder<T> {
    /**
     * Configures the bound set to silently discard duplicate elements.
     *
     * @see Multibinder.permitDuplicates
     */
    fun permitDuplicates(): KotlinMultibinder<T>

    /**
     * Returns a binding builder used to add a new element in the set.
     *
     * @see Multibinder.addBinding
     */
    fun addBinding(): KotlinLinkedBindingBuilder<T>

    companion object {
        /**
         * Returns a new multibinder that collects entries of [T] in a {@link Set}.
         */
        inline fun <reified T> newSetBinder(binder: Binder): KotlinMultibinder<T> {
            return newRealSetBinder(binder, key<T>())
        }

        /**
         * Returns a new multibinder that collects entries of [T] in a {@link set} that is bound
         * with [annotation].
         */
        inline fun <reified T> newAnnotatedSetBinder(binder: Binder, annotation: Annotation): KotlinMultibinder<T> {
            return newRealSetBinder(binder, annotatedKey(annotation))
        }

        /**
         * Returns a new multibinder that collects entries of [T] in a {@link Set} that is bound
         * with [TAnn].
         */
        inline fun <reified T, reified TAnn : Annotation> newAnnotatedSetBinder(binder: Binder):
            KotlinMultibinder<T> {
            return newRealSetBinder(binder, annotatedKey<T, TAnn>())
        }

        @PublishedApi internal fun <T> newRealSetBinder(binder: Binder, key: Key<T>):
            RealKotlinMultibinder<T> {
            val skippingBinder = binder.skipSources(RealKotlinMultibinder::class.java,
                    KotlinMultibinder::class.java,
                    Companion::class.java)
            val multibinder = Multibinder.newSetBinder(skippingBinder, key)
            val kotlinMultibinder = RealKotlinMultibinder<T>(multibinder, key)
            skippingBinder.install(kotlinMultibinder)
            return kotlinMultibinder
        }
    }
}

internal class RealKotlinMultibinder<T>(private val delegate: Multibinder<T>, key: Key<T>) :
    KotlinMultibinder<T>, KotlinModule() {
    private val elementType = key.typeLiteral
    private val setKey = setKeyFor(key)
    private val collectionOfProvidersKey = setKey.ofType(collectionOfProvidersOf(elementType))
    private val collectionOfJavaxProvidersKey = setKey
            .ofType(collectionOfJavaxProvidersOf(elementType))
    private val mutableCollectionOfProvidersKey = setKey
            .ofType(mutableCollectionOfProvidersOf(elementType))
    private val mutableCollectionOfJavaxProvidersKey = setKey
            .ofType(mutableCollectionOfJavaxProvidersOf(elementType))
    private val setName = RealElement.nameOf(key)

    private fun setKeyFor(key: Key<T>): Key<Set<T>> {
        return if (key.annotation == null && key.annotationType == null) {
            Key.get(setOf(elementType))
        } else if (key.annotation != null) {
            Key.get(setOf(elementType), key.annotation)
        } else {
            Key.get(setOf(elementType), key.annotationType)
        }
    }

    override fun configure() {
        bind(collectionOfProvidersKey).to(mutableCollectionOfProvidersKey)
        bind(collectionOfJavaxProvidersKey).to(mutableCollectionOfJavaxProvidersKey)
    }

    override fun addBinding(): KotlinLinkedBindingBuilder<T> {
        return KotlinLinkedBindingBuilderImpl<T>(delegate.addBinding())
    }

    override fun permitDuplicates(): KotlinMultibinder<T> {
        delegate.permitDuplicates()
        return this
    }

    fun getKeyForNewItem(): Key<T> {
        return Key.get<T>(elementType, RealElement(setName, MULTIBINDER, ""))
    }

    // Prevents the module from being installed multiple times.
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as RealKotlinMultibinder<*>

        if (setKey != other.setKey) return false

        return true
    }

    override fun hashCode(): Int {
        return setKey.hashCode()
    }

    override fun toString(): String {
        return (if (setName.isEmpty()) "" else setName + " ") +
                "KotlinMultibinder<" + elementType + ">"
    }
}

@Suppress("UNCHECKED_CAST")
internal fun <T> setOf(elementType: TypeLiteral<T>): TypeLiteral<Set<T>> {
    return TypeLiteral.get(KotlinTypes.setOf(elementType.type)) as TypeLiteral<Set<T>>
}

@Suppress("UNCHECKED_CAST")
internal fun <T> collectionOf(elementType: TypeLiteral<T>): TypeLiteral<Collection<T>> {
    return TypeLiteral.get(KotlinTypes.collectionOf(elementType.type))
            as TypeLiteral<Collection<T>>
}

@Suppress("UNCHECKED_CAST")
internal fun <T> providerOf(elementType: TypeLiteral<T>): TypeLiteral<Provider<T>> {
    return TypeLiteral.get(Types.providerOf(elementType.type))
            as TypeLiteral<Provider<T>>
}

@Suppress("UNCHECKED_CAST")
internal fun <T> collectionOfProvidersOf(elementType: TypeLiteral<T>):
        TypeLiteral<Collection<Provider<T>>> {
    return TypeLiteral.get(KotlinTypes.collectionOf(Types.providerOf(elementType.type)))
            as TypeLiteral<Collection<Provider<T>>>
}

@Suppress("UNCHECKED_CAST")
internal fun <T> collectionOfJavaxProvidersOf(elementType: TypeLiteral<T>):
        TypeLiteral<Collection<jakarta.inject.Provider<T>>> {
    return TypeLiteral.get(KotlinTypes.collectionOf(Types.jakartaProviderOf(elementType.type)))
            as TypeLiteral<Collection<jakarta.inject.Provider<T>>>
}

@Suppress("UNCHECKED_CAST")
internal fun <T> mutableSetOf(elementType: TypeLiteral<T>): TypeLiteral<MutableSet<T>> {
    return TypeLiteral.get(KotlinTypes.mutableSetOf(elementType.type))
            as TypeLiteral<MutableSet<T>>
}

@Suppress("UNCHECKED_CAST")
internal fun <T> mutableCollectionOf(elementType: TypeLiteral<T>): TypeLiteral<MutableCollection<T>> {
    return TypeLiteral.get(KotlinTypes.mutableCollectionOf(elementType.type))
            as TypeLiteral<MutableCollection<T>>
}

@Suppress("UNCHECKED_CAST")
internal fun <T> javaxProviderOf(elementType: TypeLiteral<T>): TypeLiteral<jakarta.inject.Provider<T>> {
    return TypeLiteral.get(Types.jakartaProviderOf(elementType.type))
            as TypeLiteral<jakarta.inject.Provider<T>>
}

@Suppress("UNCHECKED_CAST")
internal fun <T> mutableCollectionOfProvidersOf(elementType: TypeLiteral<T>):
        TypeLiteral<MutableCollection<Provider<T>>> {
    return TypeLiteral.get(KotlinTypes.mutableCollectionOf(Types.providerOf(elementType.type)))
            as TypeLiteral<MutableCollection<Provider<T>>>
}

@Suppress("UNCHECKED_CAST")
internal fun <T> mutableCollectionOfJavaxProvidersOf(elementType: TypeLiteral<T>):
        TypeLiteral<MutableCollection<jakarta.inject.Provider<T>>> {
    return TypeLiteral.get(KotlinTypes.mutableCollectionOf(Types.jakartaProviderOf(elementType.type)))
            as TypeLiteral<MutableCollection<jakarta.inject.Provider<T>>>
}
