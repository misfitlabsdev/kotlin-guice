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

import com.google.inject.CreationException
import com.google.inject.Guice
import com.google.inject.Key
import com.google.inject.name.Names
import com.google.inject.spi.ElementSource
import dev.misfitlabs.kotlinguice4.binder.annotatedWith
import dev.misfitlabs.kotlinguice4.binder.to
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.util.concurrent.Callable
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldContain
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldNotBe
import org.amshove.kluent.shouldThrow
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

/**
 * @author Brian van de Boogaard.
 */
class KotlinPrivateModuleSpec : Spek({
    describe("KotlinPrivateModule") {

        beforeEachTest {
            StaticInjectionObj.reset()
        }

        it("throws a CreationException when a non-exposed binding is attempted to be used") {
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

        it("allows getting an instance of the exposed binding") {
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

        it("injects an exposed binding into classes using the binding") {
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

        it("injects a private binding into an exposed class configured in the same module") {
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

        it("skips the KotlinPrivateBinder class in the source trace") {
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
            it("binds a custom scope using a scope annotation type parameter") {
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
            it("binds source using a type parameter") {
                val injector = Guice.createInjector(object : KotlinPrivateModule() {
                    override fun configure() {
                        bind<A>().to(AImpl::class.java)

                        expose<A>()
                    }
                })

                val a = injector.getInstance(A::class.java)

                a.get() shouldEqual "Impl of A"
            }

            it("binds a complex source using a type parameter") {
                val injector = Guice.createInjector(object : KotlinPrivateModule() {
                    override fun configure() {
                        bind<Callable<A>>().to(ACallable::class.java)

                        expose<Callable<A>>()
                    }
                })

                val a = injector.getInstance(key<Callable<A>>())
                a.call().get() shouldEqual "Impl of A"
            }

            it("binds to a target using a type parameter") {
                val injector = Guice.createInjector(object : KotlinPrivateModule() {
                    override fun configure() {
                        bind<A>().to<AImpl>()

                        expose<A>()
                    }
                })

                val a = injector.getInstance(A::class.java)

                a.get() shouldEqual "Impl of A"
            }

            it("binds to a complex target using a type parameter") {
                val injector = Guice.createInjector(object : KotlinPrivateModule() {
                    override fun configure() {
                        bind<Callable<A>>().to<TCallable<A>>()

                        expose<Callable<A>>()
                    }
                })

                val callable = injector.getInstance(key<Callable<A>>())
                callable.call() shouldEqual null
            }

            it("binds with an annotation using a type parameter") {
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

            it("binds with an annotation using an annotation instance") {
                val named = Names.named("Some Name")

                val injector = Guice.createInjector(object : KotlinPrivateModule() {
                    override fun configure() {
                        bind<A>().to<B>()
                        bind<A>().annotatedWith(named).to<AImpl>()

                        expose<A>().annotatedWith(named)
                    }
                })

                val a = injector.getInstance(Key.get(A::class.java, named))

                a.get() shouldEqual "Impl of A"
            }

            it("binds to a provider using a type parameter") {
                val injector = Guice.createInjector(object : KotlinPrivateModule() {
                    override fun configure() {
                        bind<A>().toProvider<BProvider>()

                        expose<A>()
                    }
                })

                val a = injector.getInstance(A::class.java)

                a shouldBeInstanceOf B::class.java
            }

            it("binds to a complex provider using a type parameter") {
                val injector = Guice.createInjector(object : KotlinPrivateModule() {
                    override fun configure() {
                        bind<Iterable<A>>().toProvider<TProvider<List<A>>>()

                        expose<Iterable<A>>()
                    }
                })

                injector.getInstance(key<Iterable<A>>())
            }

            it("binds in a scope") {
                val injector = Guice.createInjector(object : KotlinPrivateModule() {
                    override fun configure() {
                        bind<A>().to<AImpl>().`in`<Singleton>()

                        expose<A>()
                    }
                })

                val a = injector.getInstance(A::class.java)
                a shouldBe injector.getInstance(A::class.java)
            }

            it("binds wildcard types") {
                val injector = Guice.createInjector(object : KotlinPrivateModule() {
                    override fun configure() {
                        bind<Callable<*>>().to<ACallable>()

                        expose<Callable<*>>()
                    }
                })

                val callable: Callable<*> = injector.getInstance(key<Callable<*>>())
                callable shouldBeInstanceOf ACallable::class.java
            }

            describe("when binding to a null instance") {
                it("throws a CreationException with message that skips internal sources") {
                    val outerModule = object : KotlinPrivateModule() {
                        override fun configure() {
                            bind<String>().toInstance(null)
                        }
                    }

                    val createInjector = {
                        Guice.createInjector(outerModule)
                    }

                    val exception = (createInjector shouldThrow CreationException::class).exception
                    exception.message!! shouldContain outerModule::class.java.name
                }
            }
        }

        describe("#bindConstant") {
            it("binds to a target using a type parameter and annotation") {
                class ClassWithConstant @Inject constructor(@Annotated val constant: Class<Nothing>)

                val injector = Guice.createInjector(object : KotlinPrivateModule() {
                    override fun configure() {
                        bindConstant()
                                .annotatedWith<Annotated>()
                                .to<Iterator<*>>()

                        expose(annotatedKey<Class<Nothing>, Annotated>())
                    }
                })

                val classWithConstant = injector.getInstance(ClassWithConstant::class.java)
                classWithConstant.constant shouldEqual Iterator::class.java
            }
        }

        describe("#requestStaticInjection") {
            it("injects static fields") {
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
            it("throws a CreationException for a simple type that is unbound") {
                val createInjector = {
                    Guice.createInjector(object : KotlinPrivateModule() {
                        override fun configure() {
                            requireBinding<A>()
                        }
                    })
                }

                createInjector shouldThrow CreationException::class
            }

            it("throws a CreationException for a complex type that is unbound") {
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
            it("gets a provider for a simple type") {
                Guice.createInjector(object : KotlinPrivateModule() {
                    override fun configure() {
                        kotlinBinder.bind<A>().to<AImpl>()
                        val provider = kotlinBinder.getProvider<A>()
                        provider.toString() shouldEqual "Provider<dev.misfitlabs.kotlinguice4.A>"
                    }
                })
            }

            it("gets a provider for an annotated key") {
                Guice.createInjector(object : KotlinPrivateModule() {
                    override fun configure() {
                        kotlinBinder.bind<Callable<A>>().to<TCallable<A>>()
                        val provider = kotlinBinder.getProvider<Callable<A>>()
                        provider.toString() shouldEqual
                                "Provider<java.util.concurrent.Callable<dev.misfitlabs.kotlinguice4.A>>"
                    }
                })
            }
        }

        describe("#getMembersInjector") {
            it("injects member fields") {
                Guice.createInjector(object : KotlinPrivateModule() {
                    override fun configure() {
                        val membersInjector = getMembersInjector<AImpl>()
                        membersInjector.toString() shouldEqual
                                "MembersInjector<dev.misfitlabs.kotlinguice4.AImpl>"
                    }
                })
            }
        }
    }
})
