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

package com.authzee.kotlinguice

import com.google.inject.Binding
import com.google.inject.Injector
import com.google.inject.MembersInjector
import com.google.inject.Provider

/**
 * Returns the member injector used to inject dependencies into methods and fields on instances of
 * the given type [T].
 *
 * @param T the type to get the [MembersInjector] for
 * @see com.google.inject.Injector.getMembersInjector
 * @author John Leacox
 * @since 1.0
 */
inline fun <reified T> Injector.getMembersInjector(): MembersInjector<T> {
    return getMembersInjector(typeLiteral<T>())
}

/**
 * Returns the binding for the given type [T].
 *
 * This method is part of the Guice SPI and is intended for use by tools and extensions.
 *
 * @param T the type to get the [Binding] for
 * @throws com.google.inject.ConfigurationException if this injector cannot find or create the
 *     binding
 * @see com.google.inject.Injector.getBinding
 * @author John Leacox
 * @since 1.0
 */
inline fun <reified T> Injector.getBinding(): Binding<T> {
    return getBinding(key<T>())
}

/**
 * Returns all explicit bindings for type [T].
 *
 * This method is part of the Guice SPI and is intended for use by tools and extensions.
 *
 * @param T the type to get the [Bindings][Binding] for
 * @see com.google.inject.Injector.findBindingsByType
 * @author John Leacox
 * @since 1.0
 */
inline fun <reified T> Injector.findBindingsByType(): List<Binding<T>> {
    return findBindingsByType(typeLiteral<T>())
}

/**
 * Returns the provider used to obtain instances of the given type [T].
 *
 * @param T the type to get the [Provider] for
 * @throws com.google.inject.ConfigurationException if this injector cannot find or create the
 *     provider
 * @see com.google.inject.Injector.getProvider
 * @author John Leacox
 * @since 1.0
 */
inline fun <reified T> Injector.getProvider(): Provider<T> {
    return getProvider(key<T>())
}

/**
 * Returns the appropriate instance for the given injection type [T].
 *
 * @param T the type to get an instance of
 * @throws com.google.inject.ConfigurationException if this injector cannot find or create the
 *     instance
 * @see com.google.inject.Injector.getInstance
 * @author John Leacox
 * @since 1.0
 */
inline fun <reified T> Injector.getInstance(): T {
    return getInstance(key<T>())
}
