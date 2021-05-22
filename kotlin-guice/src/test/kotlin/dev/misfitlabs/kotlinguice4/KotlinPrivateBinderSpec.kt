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
import com.google.inject.spi.ElementSource
import dev.misfitlabs.kotlinguice4.binder.annotatedWith
import dev.misfitlabs.kotlinguice4.binder.to
import java.util.concurrent.Callable
import javax.inject.Inject
import javax.inject.Singleton
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldNotBe
import org.amshove.kluent.shouldThrow
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

/**
 * @author Brian van de Boogaard.
 */
object KotlinPrivateBinderSpec : Spek({

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
                                kotlinBinder.bind<A>().to<AImpl>()

                                // Do not expose A
                            }
                        })

                        kotlinBinder.bind<AContainer>() // Bind a type that requires an A
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
                            kotlinBinder.bind<A>().to<AImpl>()

                            kotlinBinder.expose<A>()
                        }
                    })

                    kotlinBinder.bind<AContainer>() // Bind a type that requires an A
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
                            kotlinBinder.bind<A>().to<AImpl>()

                            kotlinBinder.expose<A>()
                        }
                    })

                    kotlinBinder.bind<AContainer>() // Bind a type that requires an A
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
                            kotlinBinder.bind<A>().to<AImpl>()
                            kotlinBinder.bind<AContainer>() // Bind a type that requires an A

                            kotlinBinder.expose<AContainer>() // Only expose the container
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
                    kotlinBinder.bind<A>().to<AImpl>()
                    kotlinBinder.expose<A>()
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
                        kotlinBinder.bindScope<TestScoped>(scope)
                        kotlinBinder.bind<A>().to<AImpl>().`in`<TestScoped>()

                        kotlinBinder.expose<A>()
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
                        kotlinBinder.bind<A>().to(AImpl::class.java)

                        kotlinBinder.expose<A>()
                    }
                })

                val a = injector.getInstance(A::class.java)

                a.get() shouldEqual "Impl of A"
            }

            it("binds a complex source using a type parameter") {
                val injector = Guice.createInjector(object : KotlinPrivateModule() {
                    override fun configure() {
                        kotlinBinder.bind<Callable<A>>().to(ACallable::class.java)

                        kotlinBinder.expose<Callable<A>>()
                    }
                })

                val a = injector.getInstance(key<Callable<A>>())
                a.call().get() shouldEqual "Impl of A"
            }

            it("binds to a target using a type parameter") {
                val injector = Guice.createInjector(object : KotlinPrivateModule() {
                    override fun configure() {
                        kotlinBinder.bind<A>().to<AImpl>()

                        kotlinBinder.expose<A>()
                    }
                })

                val a = injector.getInstance(A::class.java)

                a.get() shouldEqual "Impl of A"
            }

            it("binds to a complex target using a type parameter") {
                val injector = Guice.createInjector(object : KotlinPrivateModule() {
                    override fun configure() {
                        kotlinBinder.bind<Callable<A>>().to<TCallable<A>>()

                        kotlinBinder.expose<Callable<A>>()
                    }
                })

                val callable = injector.getInstance(key<Callable<A>>())
                callable.call() shouldEqual null
            }

            it("binds with an annotation using a type parameter") {
                val injector = Guice.createInjector(object : KotlinPrivateModule() {
                    override fun configure() {
                        kotlinBinder.bind<A>().to<B>()
                        kotlinBinder.bind<A>().annotatedWith<Annotated>().to<AImpl>()

                        kotlinBinder.expose<A>().annotatedWith<Annotated>()
                    }
                })

                val a = injector.getInstance(annotatedKey<A, Annotated>())

                a.get() shouldEqual "Impl of A"
            }

            it("binds with an annotation using a class") {
                val injector = Guice.createInjector(object : KotlinPrivateModule() {
                    override fun configure() {
                        kotlinBinder.bind<A>().to<B>()
                        kotlinBinder.bind<A>().annotatedWith(Annotated::class).to<AImpl>()

                        kotlinBinder.expose<A>().annotatedWith(Annotated::class)
                    }
                })

                val a = injector.getInstance(annotatedKey<A, Annotated>())

                a.get() shouldEqual "Impl of A"
            }

            it("binds to a provider using a type parameter") {
                val injector = Guice.createInjector(object : KotlinPrivateModule() {
                    override fun configure() {
                        kotlinBinder.bind<A>().toProvider<BProvider>()

                        kotlinBinder.expose<A>()
                    }
                })

                val a = injector.getInstance(A::class.java)

                a shouldBeInstanceOf B::class.java
            }

            it("binds to a complex provider using a type parameter") {
                val injector = Guice.createInjector(object : KotlinPrivateModule() {
                    override fun configure() {
                        kotlinBinder.bind<Iterable<A>>().toProvider<TProvider<List<A>>>()

                        kotlinBinder.expose<Iterable<A>>()
                    }
                })

                injector.getInstance(key<Iterable<A>>())
            }

            it("binds in a scope") {
                val injector = Guice.createInjector(object : KotlinPrivateModule() {
                    override fun configure() {
                        kotlinBinder.bind<A>().to<AImpl>().`in`<Singleton>()

                        kotlinBinder.expose<A>()
                    }
                })

                val a = injector.getInstance(A::class.java)
                a shouldBe injector.getInstance(A::class.java)
            }

            it("binds wildcard types") {
                val injector = Guice.createInjector(object : KotlinPrivateModule() {
                    override fun configure() {
                        kotlinBinder.bind<Callable<*>>().to<ACallable>()

                        kotlinBinder.expose<Callable<*>>()
                    }
                })

                val callable: Callable<*> = injector.getInstance(key<Callable<*>>())
                callable shouldBeInstanceOf ACallable::class.java
            }
        }

        describe("#bindConstant") {
            it("binds to a target using a type parameter and annotation") {
                class ClassWithConstant @Inject constructor(@Annotated val constant: Class<Nothing>)

                val injector = Guice.createInjector(object : KotlinPrivateModule() {
                    override fun configure() {
                        kotlinBinder.bindConstant()
                                .annotatedWith<Annotated>()
                                .to<Iterator<*>>()

                        kotlinBinder.expose(annotatedKey<Class<Nothing>, Annotated>())
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
                        kotlinBinder.bind<String>().toInstance("Statically Injected")
                        requestStaticInjection<StaticInjectionObj>()
                    }
                })

                StaticInjectionObj.staticInjectionSite shouldEqual "Statically Injected"
            }
        }

        describe("#getProvider") {
            it("get a provider for a simple type") {
                Guice.createInjector(object : KotlinPrivateModule() {
                    override fun configure() {
                        kotlinBinder.bind<A>().to<AImpl>()
                        val provider = kotlinBinder.getProvider<A>()
                        provider.toString() shouldEqual "Provider<dev.misfitlabs.kotlinguice4.A>"
                    }
                })
            }

            it("get a provider for an annotated key") {
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
