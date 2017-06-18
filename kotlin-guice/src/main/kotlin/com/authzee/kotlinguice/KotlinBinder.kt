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
        return getProvider(T::class.java)
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
