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

package com.authzee.kotlinguice4

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
            it("returns a members injector that injects member fields") {
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
            it("returns a binding for a simple type") {
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

            it("returns a binding for a complex type") {
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

            it("returns a binding for an implicit binding") {
                val injector = Guice.createInjector(object : KotlinModule() {
                    override fun configure() {}
                })

                val getBinding = { injector.getBinding<AImpl>() }
                getBinding shouldNotThrow ConfigurationException::class
                getBinding().shouldNotBeNull()
            }

            it("throws a ConfigurationException for a binding that cannot be found") {
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
            it("returns all bindings for the given type across containers types") {
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
            it("returns a provider for a simple type") {
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

            it("returns a provider for a complex type") {
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

            it("returns a provider for an implicit binding") {
                val injector = Guice.createInjector(object : KotlinModule() {
                    override fun configure() {}
                })

                val provider = injector.getProvider<AImpl>()
                provider shouldBeInstanceOf Provider::class
                (provider is Provider<AImpl>) shouldBe true
                provider.get().get() shouldEqual "Impl of A"
            }

            it("throws a ConfigurationException for a provider that cannot be found") {
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
            it("returns an instance for a simple type") {
                val injector = Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        kotlinBinder.requireExplicitBindings()
                        bind<A>().to<AImpl>()
                    }
                })

                val instance = injector.getInstance<A>()
                instance.get() shouldEqual "Impl of A"
            }

            it("returns an instance for a complex type") {
                val injector = Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        kotlinBinder.requireExplicitBindings()
                        bind<Callable<A>>().to<ACallable>()
                    }
                })

                val instance = injector.getInstance<Callable<A>>()
                instance.call().get() shouldEqual "Impl of A"
            }

            it("returns an instance for an implicit binding") {
                val injector = Guice.createInjector(object : KotlinModule() {
                    override fun configure() {}
                })

                val instance = injector.getInstance<AImpl>()
                instance.get() shouldEqual "Impl of A"
            }

            it("throws a ConfigurationException for an instance that cannot be found") {
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
