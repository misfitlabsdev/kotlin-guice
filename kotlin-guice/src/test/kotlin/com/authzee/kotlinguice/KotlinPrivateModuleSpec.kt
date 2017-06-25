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

import com.google.inject.CreationException
import com.google.inject.Guice
import com.google.inject.spi.ElementSource
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldNotBe
import org.amshove.kluent.shouldThrow
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import java.util.concurrent.Callable
import javax.inject.Singleton

/**
 * @author Brian van de Boogaard.
 */
class KotlinPrivateModuleSpec : Spek({
    describe("KotlinPrivateModule") {

        beforeEachTest {
            StaticInjectionObj.reset()
        }

        it("should throw a CreationException when a non-exposed binding is attempted to be used") {
            val injector = {
                Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        install(object : KotlinPrivateModule() {
                            override fun configure() {
                                bind<A>().to<AImpl>()

                                // Do not expose A
                            }
                        })

                        bind<AContainer>() // Bind a type that requires an A
                    }
                })
            }

            injector shouldThrow CreationException::class
        }

        it("should allow getting an instance of the exposed binding") {
            val injector = Guice.createInjector(object : KotlinModule() {
                override fun configure() {
                    install(object : KotlinPrivateModule() {
                        override fun configure() {
                            bind<A>().to<AImpl>()

                            expose<A>()
                        }
                    })

                    bind<AContainer>() // Bind a type that requires an A
                }
            })

            val a = injector.getInstance<A>()
            a.get() shouldBe "Impl of A"
        }

        it("should inject an exposed binding into classes using the binding") {
            val injector = Guice.createInjector(object : KotlinModule() {
                override fun configure() {
                    install(object : KotlinPrivateModule() {
                        override fun configure() {
                            bind<A>().to<AImpl>()

                            expose<A>()
                        }
                    })

                    bind<AContainer>() // Bind a type that requires an A
                }
            })

            val aContainer = injector.getInstance<AContainer>()
            aContainer.a.get() shouldBe "Impl of A"
        }

        it("should inject a private binding into an exposed class configured in the same module") {
            val injector = Guice.createInjector(object : KotlinModule() {
                override fun configure() {
                    install(object : KotlinPrivateModule() {
                        override fun configure() {
                            bind<A>().to<AImpl>()
                            bind<AContainer>() // Bind a type that requires an A

                            expose<AContainer>() // Only expose the container
                        }
                    })

                }
            })

            val aContainer = injector.getInstance<AContainer>()
            aContainer.a.get() shouldBe "Impl of A"
        }

        it("should skip the KotlinPrivateBinder class in the source trace") {
            val outerModule = object : KotlinPrivateModule() {
                override fun configure() {
                    bind<A>().to<AImpl>()
                    expose<A>()
                }
            }

            val injector = Guice.createInjector(outerModule)

            val source = injector.getBinding(A::class.java).source as ElementSource
            val stackTraceElement = source.declaringSource as StackTraceElement

            stackTraceElement.className shouldEqual outerModule::class.java.name
        }

        describe("#bindScope") {
            it("should bind a custom scope using a scope annotation type parameter") {
                val scope = TestScope()
                val injector = Guice.createInjector(object : KotlinPrivateModule() {
                    override fun configure() {
                        bindScope<TestScoped>(scope)
                        bind<A>().to<AImpl>().`in`<TestScoped>()

                        expose<A>()
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
                val injector = Guice.createInjector(object : KotlinPrivateModule() {
                    override fun configure() {
                        bind<A>().to(AImpl::class.java)

                        expose<A>()
                    }
                })

                val a = injector.getInstance(A::class.java)

                a.get() shouldEqual "Impl of A"
            }

            it("should bind a complex source using a type parameter") {
                val injector = Guice.createInjector(object : KotlinPrivateModule() {
                    override fun configure() {
                        bind<Callable<A>>().to(ACallable::class.java)

                        expose<Callable<A>>()
                    }
                })

                val a = injector.getInstance(key<Callable<A>>())
                a.call().get() shouldEqual "Impl of A"
            }

            it("should bind to a target using a type parameter") {
                val injector = Guice.createInjector(object : KotlinPrivateModule() {
                    override fun configure() {
                        bind<A>().to<AImpl>()

                        expose<A>()
                    }
                })

                val a = injector.getInstance(A::class.java)

                a.get() shouldEqual "Impl of A"
            }

            it("should bind to a complex target using a type parameter") {
                val injector = Guice.createInjector(object : KotlinPrivateModule() {
                    override fun configure() {
                        bind<Callable<A>>().to<TCallable<A>>()

                        expose<Callable<A>>()
                    }
                })

                val callable = injector.getInstance(key<Callable<A>>())
                callable.call() shouldEqual null
            }

            it("should bind with an annotation using a type parameter") {
                val injector = Guice.createInjector(object : KotlinPrivateModule() {
                    override fun configure() {
                        bind<A>().to<B>()
                        bind<A>().annotatedWith<Annotated>().to<AImpl>()

                        expose<A>().annotatedWith<Annotated>()
                    }
                })

                val a = injector.getInstance(annotatedKey<A, Annotated>())

                a.get() shouldEqual "Impl of A"
            }

            it("should bind to a provider using a type parameter") {
                val injector = Guice.createInjector(object : KotlinPrivateModule() {
                    override fun configure() {
                        bind<A>().toProvider<BProvider>()

                        expose<A>()
                    }
                })

                val a = injector.getInstance(A::class.java)

                a shouldBeInstanceOf B::class.java
            }

            it("should bind to a complex provider using a type parameter") {
                val injector = Guice.createInjector(object : KotlinPrivateModule() {
                    override fun configure() {
                        bind<Iterable<A>>().toProvider<TProvider<List<A>>>()

                        expose<Iterable<A>>()
                    }
                })

                val iterable = injector.getInstance(key<Iterable<A>>())
                iterable shouldEqual null
            }

            it("should bind in a scope") {
                val injector = Guice.createInjector(object : KotlinPrivateModule() {
                    override fun configure() {
                        bind<A>().to<AImpl>().`in`<Singleton>()

                        expose<A>()
                    }
                })

                val a = injector.getInstance(A::class.java)
                a shouldBe injector.getInstance(A::class.java)
            }

            it("should bind wildcard types") {
                val injector = Guice.createInjector(object : KotlinPrivateModule() {
                    override fun configure() {
                        bind<Callable<*>>().to<ACallable>()

                        expose<Callable<*>>()
                    }
                })

                val callable: Callable<*> = injector.getInstance(key<Callable<*>>())
                callable shouldBeInstanceOf ACallable::class.java
            }
        }

        // TODO: Consider removing the to<T>() method for bindConstant()
//        describe("#bindConstant") {
//            it("should bind to a target using a type parameter and annotation") {
//                class ClassWithConstant @Inject constructor(@Annotated val constant: Class<Nothing>)
//
//                val injector = Guice.createInjector(object : KotlinPrivateModule() {
//                    override fun configure() {
//                        bindConstant()
//                                .annotatedWith<Annotated>()
//                                .to<Iterator<*>>()
//
//                        expose(annotatedKey<Class<*>, Annotated>())
//                    }
//                })
//
//                val classWithConstant = injector.getInstance(ClassWithConstant::class.java)
//                classWithConstant.constant shouldEqual Iterator::class.java
//            }
//        }

        describe("#requestStaticInjection") {
            it("should inject static fields") {
                Guice.createInjector(object : KotlinPrivateModule() {
                    override fun configure() {
                        bind<String>().toInstance("Statically Injected")
                        requestStaticInjection<StaticInjectionObj>()
                    }
                })

                StaticInjectionObj.staticInjectionSite shouldEqual "Statically Injected"
            }
        }

        describe("#requireBinding") {
            it("should throw a CreationException for a simple type that is unbound") {
                val createInjector = {
                    Guice.createInjector(object : KotlinPrivateModule() {
                        override fun configure() {
                            requireBinding<A>()
                        }

                    })
                }

                createInjector shouldThrow CreationException::class
            }

            it("should throw a CreationException for a complex type that is unbound") {
                val createInjector = {
                    Guice.createInjector(object : KotlinPrivateModule() {
                        override fun configure() {
                            requireBinding<Callable<A>>()
                            // Bind something that matches Callable::class but not the reified type
                            bind(Callable::class.java).to(ACallable::class.java)
                        }

                    })
                }

                createInjector shouldThrow CreationException::class
            }
        }

        describe("#getProvider") {
            it("should get a provider for a simple type") {
                Guice.createInjector(object : KotlinPrivateModule() {
                    override fun configure() {
                        kotlinBinder.bind<A>().to<AImpl>()
                        val provider = kotlinBinder.getProvider<A>()
                        provider.toString() shouldEqual "Provider<com.authzee.kotlinguice.A>"
                    }

                })
            }

            it("should get a provider for an annotated key") {
                Guice.createInjector(object : KotlinPrivateModule() {
                    override fun configure() {
                        kotlinBinder.bind<Callable<A>>().to<TCallable<A>>()
                        val provider = kotlinBinder.getProvider<Callable<A>>()
                        provider.toString() shouldEqual
                                "Provider<java.util.concurrent.Callable<com.authzee.kotlinguice.A>>"
                    }

                })
            }
        }

        describe("#getMembersInjector") {
            it("should inject member fields") {
                Guice.createInjector(object : KotlinPrivateModule() {
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
