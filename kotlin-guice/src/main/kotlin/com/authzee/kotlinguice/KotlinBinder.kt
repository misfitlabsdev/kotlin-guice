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

import com.authzee.kotlinguice.binder.KotlinAnnotatedBindingBuilder
import com.authzee.kotlinguice.internal.KotlinBindingBuilder
import com.google.inject.Binder
import com.google.inject.MembersInjector
import com.google.inject.Provider
import com.google.inject.Scope

/**
 * A wrapper of [Binder] that enhances the binding DSL to allow binding using reified type
 * parameters.
 *
 * By using this class instead of [Binder] you can replace the following DSL lines:
 * ```
 * bind(Service::class.java).to(ServiceImpl::class.java).`in`(Singleton::class.java)
 * bind(object : TypeLiteral<PaymentService<CreditCard>>() {})
 *         .to(CreditCardPaymentService::class.java)
 * ```
 * with
 * ```
 * bind<Service>().to<ServiceImpl>().`in`<Singleton>()
 * bind<PaymentService<CreditCard>>().to<CreditCardPaymentService>()
 * ```
 *
 * @see Binder
 * @author John Leacox
 * @since 1.0
 */
open class KotlinBinder(open val delegate: Binder) : Binder by delegate {
    /**
     * Binds a [Scope] to an [Annotation] using an annotation type parameter.
     *
     * @see Binder
     */
    inline fun <reified TAnn : Annotation> bindScope(scope: Scope) {
        bindScope(TAnn::class.java, scope)
    }

    /**
     * Binds using a type parameter.
     *
     * @see Binder
     */
    inline fun <reified T> bind() : KotlinAnnotatedBindingBuilder<T> {
        return KotlinBindingBuilder<T>(bind(typeLiteral<T>()))
    }

    /**
     * Requests static injection using a type parameter.
     *
     * @see Binder
     */
    inline fun <reified T> requestStaticInjection() {
        requestStaticInjection(T::class.java)
    }

    /**
     * Gets a provider using a type parameter.
     *
     * @see Binder
     */
    inline fun <reified T> getProvider() : Provider<T> {
        return getProvider(key<T>())
    }

    /**
     * Gets a members injector using a type parameter.
     *
     * @see Binder
     */
    inline fun <reified T> getMembersInjector() : MembersInjector<T> {
        return getMembersInjector(typeLiteral<T>())
    }
}
