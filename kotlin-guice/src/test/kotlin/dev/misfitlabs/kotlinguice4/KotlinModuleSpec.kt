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
import com.google.inject.Provides
import com.google.inject.name.Names
import com.google.inject.spi.ElementSource
import dev.misfitlabs.kotlinguice4.binder.annotatedWith
import dev.misfitlabs.kotlinguice4.binder.to
import java.util.concurrent.Callable
import javax.inject.Inject
import javax.inject.Singleton
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldContain
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldNotBe
import org.amshove.kluent.shouldThrow
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

/**
 * @author John Leacox
 */
object KotlinModuleSpec : Spek({
    beforeEachTest {
        StaticInjectionObj.reset()
    }

    describe("KotlinModule") {
        it("skips the KotlinBinder class in the source trace") {
            val outerModule = object : KotlinModule() {
                override fun configure() {
                    bind<A>().to<AImpl>()
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
            it("binds source using a type parameter") {
                val injector = Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        bind<A>().to(AImpl::class.java)
                    }
                })

                val a = injector.getInstance(A::class.java)

                a.get() shouldEqual "Impl of A"
            }

            it("binds a complex source using a type parameter") {
                val injector = Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        bind<Callable<A>>().to(ACallable::class.java)
                    }
                })

                val a = injector.getInstance(key<Callable<A>>())
                a.call().get() shouldEqual "Impl of A"
            }

            it("binds to a target using a type parameter") {
                val injector = Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        bind<A>().to<AImpl>()
                    }
                })

                val a = injector.getInstance(A::class.java)

                a.get() shouldEqual "Impl of A"
            }

            it("binds to a complex target using a type parameter") {
                val injector = Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        bind<Callable<A>>().to<TCallable<A>>()
                    }
                })

                val callable = injector.getInstance(key<Callable<A>>())
                callable.call() shouldEqual null
            }

            it("binds with an annotation using a type parameter") {
                val injector = Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        bind<A>().to<B>()
                        bind<A>().annotatedWith<Annotated>().to<AImpl>()
                    }
                })

                val a = injector.getInstance(Key.get(A::class.java, Annotated::class.java))

                a.get() shouldEqual "Impl of A"
            }

            it("binds with an annotation using an annotation instance") {
                val named = Names.named("Some Name")

                val injector = Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        bind<A>().to<B>()
                        bind<A>().annotatedWith(named).to<AImpl>()
                    }
                })

                val a = injector.getInstance(Key.get(A::class.java, named))

                a.get() shouldEqual "Impl of A"
            }

            it("binds to a provider using a type parameter") {
                val injector = Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        bind<A>().toProvider<BProvider>()
                    }
                })

                val a = injector.getInstance(A::class.java)

                a shouldBeInstanceOf B::class.java
            }

            it("binds to a complex provider using a type parameter") {
                val injector = Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        bind<Iterable<A>>().toProvider<TProvider<List<A>>>()
                    }
                })

                injector.getInstance(key<Iterable<A>>())
            }

            it("binds in a scope") {
                val injector = Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        bind<A>().to<AImpl>().`in`<Singleton>()
                    }
                })

                val a = injector.getInstance(A::class.java)
                a shouldBe injector.getInstance(A::class.java)
            }

            it("binds wildcard types") {
                val injector = Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        bind<Callable<*>>().to<ACallable>()
                    }
                })

                val callable: Callable<*> = injector.getInstance(key<Callable<*>>())
                callable shouldBeInstanceOf ACallable::class.java
            }

            describe("when binding to a null instance") {
                it("throws a CreationException with message that skips internal sources") {
                    val outerModule = object : KotlinModule() {
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
            it("injects static fields") {
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
            it("throws a CreationException for a simple type that is unbound") {
                val createInjector = {
                    Guice.createInjector(object : KotlinModule() {
                        override fun configure() {
                            requireBinding<A>()
                        }
                    })
                }

                createInjector shouldThrow CreationException::class
            }

            it("throws a CreationException for a complex type that is unbound") {
                val createInjector = {
                    Guice.createInjector(object : KotlinModule() {
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
            it("get a provider for a simple type") {
                Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        bind<A>().to<AImpl>()
                        val provider = getProvider<A>()
                        provider.toString() shouldEqual "Provider<dev.misfitlabs.kotlinguice4.A>"
                    }
                })
            }

            it("get a provider for an annotated key") {
                Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        bind<Callable<A>>().to<TCallable<A>>()
                        val provider = kotlinBinder.getProvider<Callable<A>>()
                        provider.toString() shouldEqual
                                "Provider<java.util.concurrent.Callable<dev.misfitlabs.kotlinguice4.A>>"
                    }
                })
            }
        }

        describe("#getMembersInjector") {
            it("injects member fields") {
                Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        val membersInjector = getMembersInjector<AImpl>()
                        membersInjector.toString() shouldEqual
                                "MembersInjector<dev.misfitlabs.kotlinguice4.AImpl>"
                    }
                })
            }
        }

        describe("methods with @Provides annotation") {
            it("binds from return type") {
                val injector = Guice.createInjector(object : KotlinModule() {
                    override fun configure() {}

                    @Provides
                    fun provideA(): A {
                        return AImpl()
                    }
                })

                val a = injector.getInstance(A::class.java)

                a.get() shouldEqual "Impl of A"
            }
        }
    }
})
