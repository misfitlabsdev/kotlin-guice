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
import com.google.inject.multibindings.OptionalBinder
import dev.misfitlabs.kotlinguice4.annotatedKey
import dev.misfitlabs.kotlinguice4.binder.KotlinLinkedBindingBuilder
import dev.misfitlabs.kotlinguice4.key

/**
 * A wrapper of [OptionalBinder] that enhances the binding DSL to allow binding using reified type
 * parameters.
 *
 * By using this class instead of [OptionalBinder] you can replace the following lines:
 * ```
 * val optionalBinder = OptionalBinder.newOptionalBinder(binder(), Renamer::class.java)
 * optionalBinder.setBinding().to(ReplacingRenamer::class.java);
 * ```
 * with
 * ```
 * val optionalBinder = OptionalBinder.newOptionalBinder<Renamer>(kotlinBinder)
 * optionalBinder.setBinding().to<ReplacingRenamer>()
 * ```
 *
 * @see OptionalBinder
 * @author John Leacox
 * @since 1.0
 */
interface KotlinOptionalBinder<T> {
    /**
     * Returns a binding builder used to set the default value.
     *
     * @see OptionalBinder.setDefault
     */
    fun setDefault(): KotlinLinkedBindingBuilder<T>

    /**
     * Returns a binding builder used to set the actual value.
     *
     * @see OptionalBinder.setDefault
     */
    fun setBinding(): KotlinLinkedBindingBuilder<T>

    companion object {
        /**
         * Returns a new optional binder for configuring default and actual values of [T].
         */
        inline fun <reified T> newOptionalBinder(binder: Binder): KotlinOptionalBinder<T> {
            return newRealOptionalBinder(binder, key<T>())
        }

        /**
         * Returns a new optional binder for configuring default and actual values of [T] that is
         * bound with [TAnn].
         */
        inline fun <reified T, reified TAnn : Annotation>
                newAnnotatedOptionalBinder(binder: Binder): KotlinOptionalBinder<T> {
            return newRealOptionalBinder(binder, annotatedKey<T, TAnn>())
        }

        @PublishedApi internal fun <T> newRealOptionalBinder(
            binder: Binder,
            optionalKey: Key<T>
        ): RealKotlinOptionalBinder<T> {
            val skippingBinder = binder.skipSources(RealKotlinOptionalBinder::class.java,
                    KotlinOptionalBinder::class.java,
                    Companion::class.java)
            val optionalBinder = OptionalBinder.newOptionalBinder(skippingBinder, optionalKey)
            return RealKotlinOptionalBinder(optionalBinder, optionalKey)
        }
    }
}

internal class RealKotlinOptionalBinder<T>(
    private val delegate: OptionalBinder<T>,
    private val optionalKey: Key<T>
) : KotlinOptionalBinder<T> {
    private val elementType = optionalKey.typeLiteral
    private val name = dev.misfitlabs.kotlinguice4.multibindings.RealElement.nameOf(optionalKey)

    override fun setDefault(): KotlinLinkedBindingBuilder<T> {
        return KotlinLinkedBindingBuilderImpl<T>(delegate.setDefault())
    }

    override fun setBinding(): KotlinLinkedBindingBuilder<T> {
        return KotlinLinkedBindingBuilderImpl<T>(delegate.setBinding())
    }

    fun getKeyForDefaultBinding(): Key<T> {
        return Key.get<T>(optionalKey.typeLiteral,
            dev.misfitlabs.kotlinguice4.multibindings.RealElement(defaultNameOf(optionalKey), dev.misfitlabs.kotlinguice4.multibindings.Element.Type.OPTIONALBINDER, ""))
    }

    fun getKeyForActualBinding(): Key<T> {
        return Key.get<T>(optionalKey.typeLiteral,
            dev.misfitlabs.kotlinguice4.multibindings.RealElement(actualNameOf(optionalKey), dev.misfitlabs.kotlinguice4.multibindings.Element.Type.OPTIONALBINDER, ""))
    }

    private fun defaultNameOf(key: Key<*>): String {
        val value = dev.misfitlabs.kotlinguice4.multibindings.RealElement.nameOf(key)
        return "@Default" + if (value.isEmpty()) "" else "(value=$value)"
    }

    private fun actualNameOf(key: Key<*>): String {
        val value = dev.misfitlabs.kotlinguice4.multibindings.RealElement.nameOf(key)
        return "@Actual" + if (value.isEmpty()) "" else "(value=$value)"
    }

    // Prevents the module from being installed multiple times.
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as RealKotlinOptionalBinder<*>

        if (optionalKey != other.optionalKey) return false

        return true
    }

    override fun hashCode(): Int {
        return optionalKey.hashCode()
    }

    override fun toString(): String {
        return (if (name.isEmpty()) "" else name + " ") +
                "KotlinOptionalBinder<" + elementType + ">"
    }
}
