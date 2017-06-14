package com.authzee.kotlinguice

import com.google.inject.BindingAnnotation
import com.google.inject.Key
import com.google.inject.Provider
import com.google.inject.Scope
import com.google.inject.ScopeAnnotation
import java.util.concurrent.Callable
import javax.inject.Inject

interface A {
    fun get(): String
}

class ACallable : Callable<A> {
    override fun call(): A {
        return AImpl()
    }
}

class TCallable<T> : Callable<T> {
    override fun call(): T? {
        return null
    }

}

open class AImpl : A {
    override fun get(): String {
        return "Impl of A"
    }
}

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION,
        AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.EXPRESSION, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@BindingAnnotation
annotation class Annotated

open class B : AImpl() {
    override fun get(): String {
        return "This is B"
    }
}

@Retention(AnnotationRetention.RUNTIME)
@ScopeAnnotation
annotation class TestScoped

@Suppress("UNCHECKED_CAST")
class TestScope : Scope {
    private var scopedObjectsMap = HashMap<Key<*>, Any>()

    override fun <T> scope(key: Key<T>, unscoped: Provider<T>): Provider<T> {
        return Provider { scopedObjectsMap.getOrPut(key) { unscoped.get() as Any } as T }
    }

    fun reset() {
        scopedObjectsMap = HashMap<Key<*>, Any>()
    }
}

class BProvider : Provider<B> {
    override fun get(): B {
        return B()
    }
}

class TProvider<T> : Provider<T> {
    override fun get(): T? {
        return null
    }
}

object StaticInjectionObj {
    @Inject var staticInjectionSite: String = ""
}
