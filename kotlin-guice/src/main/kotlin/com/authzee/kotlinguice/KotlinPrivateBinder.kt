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

import com.authzee.kotlinguice.binder.KotlinAnnotatedElementBuilder
import com.google.inject.PrivateBinder

/**
 * A wrapper of [PrivateBinder] that enhances the binding DSL to allow binding using reified type
 * parameters.
 *
 * By using this class instead of [PrivateBinder] you can replace the following DSL lines:
 * ```
 * bind(Service::class.java).to(ServiceImpl::class.java).`in`(Singleton::class.java)
 * bind(object : TypeLiteral<PaymentService<CreditCard>>() {})
 *         .to(CreditCardPaymentService::class.java)
 *
 * expose(object : TypeLiteral<PaymentService<CreditCard>>() {})
 * ```
 * with
 * ```
 * bind<Service>().to<ServiceImpl>().`in`<Singleton>()
 * bind<PaymentService<CreditCard>>().to<CreditCardPaymentService>()
 *
 * expose<PaymentService<CreditCard>>()
 * ```
 *
 * @see PrivateBinder
 * @author Brian van de Boogaard
 * @since 1.0
 */
@Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE")
class KotlinPrivateBinder(override val delegate: PrivateBinder)
    : KotlinBinder(delegate), PrivateBinder by delegate {
    /**
     * Makes a binding for [T] available to the enclosing environment. Use
     * [KotlinAnnotatedElementBuilder.annotatedWith] to expose [T] with a binding annotation.
     */
    inline fun <reified T> expose(): KotlinAnnotatedElementBuilder {
        return KotlinAnnotatedElementBuilder(delegate.expose(typeLiteral<T>()))
    }
}
