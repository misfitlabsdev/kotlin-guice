package com.authzee.kotlinguice

import com.authzee.kotlinguice.binder.annotatedWith
import com.authzee.kotlinguice.binder.to
import com.google.inject.CreationException
import com.google.inject.Guice
import com.google.inject.Key
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldNotBe
import org.amshove.kluent.shouldThrow
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import java.util.concurrent.Callable
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @author John Leacox
 */
object KotlinModuleSpec : Spek({
    beforeEachTest {
        StaticInjectionObj.reset()
    }

    describe("KotlinModule") {
        describe("#bindScope") {
            it("should bind a custom scope using a scope annotation type parameter") {
                val scope = TestScope()
                val injector = Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        bindScope<TestScoped>(scope)
                        bind<A>().to<AImpl>().`in`<TestScoped>()
                    }
                })

                val a = injector.getInstance(A::class.java)
                a shouldBe injector.getInstance(A::class.java)

                scope.reset()

                a shouldNotBe injector.getInstance(A::class.java)
            }
        }

        describe("#bind") {
            it("should bind source using a type parameter") {
                val injector = Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        bind<A>().to(AImpl::class.java)
                    }
                })

                val a = injector.getInstance(A::class.java)

                a.get() shouldEqual "Impl of A"
            }

            it("should bind a complex source using a type parameter") {
                val injector = Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        bind<Callable<A>>().to(ACallable::class.java)
                    }
                })

                val a = injector.getInstance(key<Callable<A>>())
                a.call().get() shouldEqual "Impl of A"
            }

            it("should bind to a target using a type parameter") {
                val injector = Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        bind<A>().to<AImpl>()
                    }
                })

                val a = injector.getInstance(A::class.java)

                a.get() shouldEqual "Impl of A"
            }

            it("should bind to a complex target using a type parameter") {
                val injector = Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        bind<Callable<A>>().to<TCallable<A>>()
                    }
                })

                val callable = injector.getInstance(key<Callable<A>>())
                callable.call() shouldEqual null
            }

            it("should bind with an annotation using a type parameter") {
                val injector = Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        bind<A>().to<B>()
                        bind<A>().annotatedWith<Annotated>().to<AImpl>()
                    }
                })

                val a = injector.getInstance(Key.get(A::class.java, Annotated::class.java))

                a.get() shouldEqual "Impl of A"
            }

            it("should bind to a provider using a type parameter") {
                val injector = Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        bind<A>().toProvider<BProvider>()
                    }
                })

                val a = injector.getInstance(A::class.java)

                a shouldBeInstanceOf B::class.java
            }

            it("should bind to a complex provider using a type parameter") {
                val injector = Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        bind<Iterable<A>>().toProvider<TProvider<List<A>>>()
                    }
                })

                val iterable = injector.getInstance(key<Iterable<A>>())
                iterable shouldEqual null
            }

            it("should bind in a scope") {
                val injector = Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        bind<A>().to<AImpl>().`in`<Singleton>()
                    }
                })

                val a = injector.getInstance(A::class.java)
                a shouldBe injector.getInstance(A::class.java)
            }

            it("should bind wildcard types") {
                val injector = Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        bind<Callable<*>>().to<ACallable>()
                    }
                })

                val callable: Callable<*> = injector.getInstance(key<Callable<*>>())
                callable shouldBeInstanceOf ACallable::class.java
            }
        }

        describe("#bindConstant") {
            it("should bind to a target using a type parameter and annotation") {
                class ClassWithConstant @Inject constructor(@Annotated val constant: Class<Nothing>)

                val injector = Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        bindConstant().annotatedWith<Annotated>().to<Iterator<*>>()
                    }
                })

                val classWithConstant = injector.getInstance(ClassWithConstant::class.java)
                classWithConstant.constant shouldEqual Iterator::class.java
            }
        }

        describe("#requestStaticInjection") {
            it("should inject static fields") {
                Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        bind<String>().toInstance("Statically Injected")
                        requestStaticInjection<StaticInjectionObj>()
                    }
                })

                StaticInjectionObj.staticInjectionSite shouldEqual "Statically Injected"
            }

        }

        describe("#requireBinding") {
            val createInjector = {
                Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        kotlinBinder.requireExplicitBindings()
                        requireBinding<String>()
                    }

                })
            }

            createInjector shouldThrow CreationException::class
        }

        describe("#getProvider") {
            it("should get a provider") {
                Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        bind<A>().to<AImpl>()
                        val provider = getProvider<A>()
                        provider.toString() shouldEqual "Provider<com.authzee.kotlinguice.A>"
                    }

                })
            }
        }

        describe("#getMembersInjector") {
            it("should inject member fields") {
                Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        val membersInjector = getMembersInjector<AImpl>()
                        membersInjector.toString() shouldEqual
                                "MembersInjector<com.authzee.kotlinguice.AImpl>"
                    }
                })
            }
        }
    }
})

