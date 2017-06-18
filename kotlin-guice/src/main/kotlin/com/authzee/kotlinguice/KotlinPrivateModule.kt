package com.authzee.kotlinguice

import com.authzee.kotlinguice.binder.KotlinAnnotatedBindingBuilder
import com.authzee.kotlinguice.binder.KotlinAnnotatedElementBuilder
import com.google.inject.MembersInjector
import com.google.inject.PrivateModule
import com.google.inject.Provider
import com.google.inject.Scope

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
abstract class KotlinPrivateModule : PrivateModule() {
    /** Gets direct access to the underlying [KotlinPrivateBinder]. */
    protected val kotlinBinder: KotlinPrivateBinder by lazy {
        KotlinPrivateBinder(binder().skipSources(KotlinPrivateModule::class.java))
    }

    /** Makes the binding for [T] available to enclosing modules and the injector. */
    protected inline fun <reified T> expose() : KotlinAnnotatedElementBuilder {
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
        requireBinding(T::class.java)
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
