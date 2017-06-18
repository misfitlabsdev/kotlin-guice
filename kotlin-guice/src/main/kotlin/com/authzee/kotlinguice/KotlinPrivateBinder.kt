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
