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

import com.google.inject.MembersInjector
import com.google.inject.PrivateModule
import com.google.inject.Provider
import com.google.inject.Scope
import dev.misfitlabs.kotlinguice4.binder.KotlinAnnotatedBindingBuilder
import dev.misfitlabs.kotlinguice4.binder.KotlinAnnotatedElementBuilder
import dev.misfitlabs.kotlinguice4.binder.KotlinLinkedBindingBuilder
import dev.misfitlabs.kotlinguice4.binder.KotlinScopedBindingBuilder
import dev.misfitlabs.kotlinguice4.internal.KotlinBindingBuilder

/**
 * An extension of [KotlinPrivateModule] that enhances the binding DSL to allow binding using reified
 * type parameters.
 *
 * By using this class instead of [KotlinPrivateModule] you can replace:
 * ```
 * class MyModule : KotlinPrivateModule() {
 *     override fun configure() {
 *         bind(Service::class.java).to(ServiceImpl::class.java).`in`(Singleton::class.java)
 *         bind(object : TypeLiteral<PaymentService<CreditCard>>() {})
 *                 .to(CreditCardPaymentService::class.java)
 *
 *         expose(object : TypeLiteral<PaymentService<CreditCard>>() {}
 *     }
 * }
 * ```
 * with
 * ```
 * class MyModule : KotlinPrivateModule() {
 *     override fun configure() {
 *         bind<Service>().to<ServiceImpl>().`in`<Singleton>()
 *         bind<PaymentService<CreditCard>>().to<CreditCardPaymentService>()
 *
 *         expose<PaymentService<CreditCard>>()
 *     }
 * }
 * ```
 *
 * @see KotlinPrivateBinder
 * @see PrivateModule
 * @author Brian van de Boogaard
 * @since 1.0
 */
@ExperimentalStdlibApi
abstract class KotlinPrivateModule : PrivateModule() {
    /** Gets direct access to the underlying [KotlinPrivateBinder]. */
    protected val kotlinBinder: KotlinPrivateBinder by lazy {
        KotlinPrivateBinder(binder().skipSources(
            KotlinAnnotatedBindingBuilder::class.java,
            KotlinAnnotatedElementBuilder::class.java,
            KotlinBinder::class.java,
            KotlinBindingBuilder::class.java,
            KotlinLinkedBindingBuilder::class.java,
            KotlinScopedBindingBuilder::class.java
        ))
    }

    /** Makes the binding for [T] available to enclosing modules and the injector. */
    protected inline fun <reified T> expose(): KotlinAnnotatedElementBuilder {
        return kotlinBinder.expose<T>()
    }

    // Everything below is copied from KotlinModule.

    /** @see KotlinPrivateBinder.bindScope */
    protected inline fun <reified TAnn : Annotation> bindScope(scope: Scope) {
        kotlinBinder.bindScope<TAnn>(scope)
    }

    /** @see KotlinPrivateBinder.bind */
    protected inline fun <reified T> bind(): KotlinAnnotatedBindingBuilder<T> {
        return kotlinBinder.bind<T>()
    }

    /** @see KotlinPrivateBinder.requestStaticInjection */
    protected inline fun <reified T> requestStaticInjection() {
        kotlinBinder.requestStaticInjection<T>()
    }

    /** @see PrivateModule.requireBinding */
    protected inline fun <reified T> requireBinding() {
        requireBinding(key<T>())
    }

    /** @see KotlinPrivateBinder.getProvider */
    protected inline fun <reified T> getProvider(): Provider<T> {
        return kotlinBinder.getProvider<T>()
    }

    /** @see KotlinPrivateBinder.getMembersInjector */
    protected inline fun <reified T> getMembersInjector(): MembersInjector<T> {
        return kotlinBinder.getMembersInjector<T>()
    }
}
