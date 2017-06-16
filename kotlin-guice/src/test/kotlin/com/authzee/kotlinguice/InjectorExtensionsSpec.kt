package com.authzee.kotlinguice

import com.google.inject.ConfigurationException
import com.google.inject.Guice
import com.google.inject.multibindings.MapBinder
import com.google.inject.multibindings.Multibinder
import com.google.inject.multibindings.OptionalBinder
import com.google.inject.spi.InstanceBinding
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldNotBeNull
import org.amshove.kluent.shouldNotThrow
import org.amshove.kluent.shouldThrow
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import java.util.concurrent.Callable
import javax.inject.Provider

/**
 * @author John Leacox
 */
object InjectorExtensionsSpec : Spek({
    beforeEachTest {
        StaticInjectionObj.reset()
    }

    describe("InjectorExtensions") {
        describe("#getMembersInjector") {
            it("should return a members injector that injects member fields") {
                val injector = Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        bind<String>().toInstance("Hello World")
                        bind<String>()
                                .annotatedWith<Annotated>()
                                .toInstance("Annotated Hello World")
                    }
                })

                val membersInjection = MembersInjection()
                val membersInjector = injector.getMembersInjector<MembersInjection>()
                membersInjector.injectMembers(membersInjection)

                membersInjection.memberInjectionSite shouldEqual "Hello World"
                membersInjection.annotatedMemberInjectionSite shouldEqual "Annotated Hello World"
            }
        }

        describe("#getBinding") {
            it("should return a binding for a simple type") {
                val injector = Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        kotlinBinder.requireExplicitBindings()
                        bind<A>().to<AImpl>()
                    }
                })

                val getBinding = { injector.getBinding<AImpl>() }
                getBinding shouldNotThrow ConfigurationException::class
                getBinding().shouldNotBeNull()
            }

            it("should return a binding for a complex type") {
                val injector = Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        kotlinBinder.requireExplicitBindings()
                        bind<Callable<A>>().to<TCallable<A>>()
                    }
                })

                val getBinding = { injector.getBinding<Callable<A>>() }
                getBinding shouldNotThrow ConfigurationException::class

                val binding = getBinding()
                binding.key shouldEqual key<Callable<A>>()
            }

            it("should return a binding for an implicit binding") {
                val injector = Guice.createInjector(object : KotlinModule() {
                    override fun configure() {}
                })

                val getBinding = { injector.getBinding<AImpl>() }
                getBinding shouldNotThrow ConfigurationException::class
                getBinding().shouldNotBeNull()
            }

            it("should throw a ConfigurationException for a binding that cannot be found") {
                val injector = Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        kotlinBinder.requireExplicitBindings()
                        bind<A>().toProvider<BProvider>()
                    }
                })

                val getBinding = { injector.getBinding<AImpl>() }

                getBinding shouldThrow ConfigurationException::class
            }
        }

        describe("#findBindingsByType") {
            it("should return all bindings for the given type across containers types") {
                val injector = Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        Multibinder.newSetBinder(binder(), String::class.java)
                                .addBinding()
                                .toInstance("A")

                        MapBinder.newMapBinder(binder(), String::class.java, String::class.java)
                                .addBinding("b")
                                .toInstance("B")

                        OptionalBinder.newOptionalBinder(binder(), String::class.java)
                                .setDefault()
                                .toInstance("C")
                    }
                })

                // OptionalBinder adds Provider bindings, so must filter to instance bindings
                val bindings = injector.findBindingsByType<String>()
                        .filterIsInstance<InstanceBinding<String>>()

                bindings[0].instance shouldEqual "A"
                bindings[1].instance shouldEqual "B"
                bindings[2].instance shouldEqual "C"
            }
        }

        describe("#getProvider") {
            it("should return a provider for a simple type") {
                val injector = Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        kotlinBinder.requireExplicitBindings()
                        bind<A>().to<AImpl>()
                    }
                })

                val provider = injector.getProvider<A>()
                provider shouldBeInstanceOf Provider::class
                (provider is Provider<A>) shouldBe true
                provider.get().get() shouldEqual "Impl of A"
            }

            it("should return a provider for a complex type") {
                val injector = Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        kotlinBinder.requireExplicitBindings()
                        bind<Callable<A>>().to<ACallable>()
                    }
                })

                val provider = injector.getProvider<Callable<A>>()
                provider shouldBeInstanceOf Provider::class
                (provider is Provider<Callable<A>>) shouldBe true
                provider.get().call().get() shouldEqual "Impl of A"
            }

            it("should return a provider for an implicit binding") {
                val injector = Guice.createInjector(object : KotlinModule() {
                    override fun configure() {}
                })

                val provider = injector.getProvider<AImpl>()
                provider shouldBeInstanceOf Provider::class
                (provider is Provider<AImpl>) shouldBe true
                provider.get().get() shouldEqual "Impl of A"
            }

            it("should throw a ConfigurationException for a provider that cannot be found") {
                val injector = Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        kotlinBinder.requireExplicitBindings()
                        bind<A>().toProvider<BProvider>()
                    }
                })

                val getProvider = { injector.getProvider<AImpl>() }

                getProvider shouldThrow ConfigurationException::class
            }
        }

        describe("#getInstance") {
            it("should return an instance for a simple type") {
                val injector = Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        kotlinBinder.requireExplicitBindings()
                        bind<A>().to<AImpl>()
                    }
                })

                val instance = injector.getInstance<A>()
                instance.get() shouldEqual "Impl of A"
            }

            it("should return an instance for a complex type") {
                val injector = Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        kotlinBinder.requireExplicitBindings()
                        bind<Callable<A>>().to<ACallable>()
                    }
                })

                val instance = injector.getInstance<Callable<A>>()
                instance.call().get() shouldEqual "Impl of A"
            }

            it("should return an instance for an implicit binding") {
                val injector = Guice.createInjector(object : KotlinModule() {
                    override fun configure() {}
                })

                val instance = injector.getInstance<AImpl>()
                instance.get() shouldEqual "Impl of A"
            }

            it("should throw a ConfigurationException for an instance that cannot be found") {
                val injector = Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        kotlinBinder.requireExplicitBindings()
                        bind<A>().toProvider<BProvider>()
                    }
                })

                val getInstance = { injector.getInstance<AImpl>() }
                getInstance shouldThrow ConfigurationException::class
            }
        }
    }
})

