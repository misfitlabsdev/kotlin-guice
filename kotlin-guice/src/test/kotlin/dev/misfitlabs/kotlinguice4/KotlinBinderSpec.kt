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

import com.google.inject.Guice
import com.google.inject.Key
import dev.misfitlabs.kotlinguice4.binder.annotatedWith
import dev.misfitlabs.kotlinguice4.binder.to
import java.util.concurrent.Callable
import javax.inject.Inject
import javax.inject.Singleton
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldNotBe
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

/**
 * @author John Leacox
 */
object KotlinBinderSpec : Spek({
    beforeEachTest {
        StaticInjectionObj.reset()
    }

    describe("KotlinBinder") {
        describe("#bindScope") {
            it("binds a custom scope using a scope annotation type parameter") {
                val scope = TestScope()
                val injector = Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        kotlinBinder.bindScope<TestScoped>(scope)
                        kotlinBinder.bind<A>().to<AImpl>().`in`<TestScoped>()
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
                        kotlinBinder.bind<A>().to(AImpl::class.java)
                    }
                })

                val a = injector.getInstance(A::class.java)

                a.get() shouldEqual "Impl of A"
            }

            it("binds a complex source using a type parameter") {
                val injector = Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        kotlinBinder.bind<Callable<A>>().to(ACallable::class.java)
                    }
                })

                val a = injector.getInstance(key<Callable<A>>())
                a.call().get() shouldEqual "Impl of A"
            }

            it("binds to a target using a type parameter") {
                val injector = Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        kotlinBinder.bind<A>().to<AImpl>()
                    }
                })

                val a = injector.getInstance(A::class.java)

                a.get() shouldEqual "Impl of A"
            }

            it("binds to a complex target using a type parameter") {
                val injector = Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        kotlinBinder.bind<Callable<A>>().to<TCallable<A>>()
                    }
                })

                val callable = injector.getInstance(key<Callable<A>>())
                callable.call() shouldEqual null
            }

            it("binds with an annotation using a type parameter") {
                val injector = Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        kotlinBinder.bind<A>().to<B>()
                        kotlinBinder.bind<A>().annotatedWith<Annotated>().to<AImpl>()
                    }
                })

                val a = injector.getInstance(Key.get(A::class.java, Annotated::class.java))

                a.get() shouldEqual "Impl of A"
            }

            it("binds with an annotation using a type argument") {
                val injector = Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        kotlinBinder.bind<A>().to<B>()
                        kotlinBinder.bind<A>().annotatedWith(Annotated::class).to<AImpl>()
                    }
                })

                val a = injector.getInstance(Key.get(A::class.java, Annotated::class.java))

                a.get() shouldEqual "Impl of A"
            }

            it("binds to a provider using a type parameter") {
                val injector = Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        kotlinBinder.bind<A>().toProvider<BProvider>()
                    }
                })

                val a = injector.getInstance(A::class.java)

                a shouldBeInstanceOf B::class.java
            }

            it("binds to a complex provider using a type parameter") {
                val injector = Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        kotlinBinder.bind<Iterable<A>>().toProvider<TProvider<List<A>>>()
                    }
                })

                injector.getInstance(key<Iterable<A>>())
            }

            it("binds in a scope") {
                val injector = Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        kotlinBinder.bind<A>().to<AImpl>().`in`<Singleton>()
                    }
                })

                val a = injector.getInstance(A::class.java)
                a shouldBe injector.getInstance(A::class.java)
            }
        }

        describe("#bindConstant") {
            it("binds to a target using a type parameter and annotation") {
                class ClassWithConstant @Inject constructor(@Annotated val constant: Class<Nothing>)

                val injector = Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        kotlinBinder.bindConstant().annotatedWith<Annotated>().to<Iterator<*>>()
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
                        kotlinBinder.bind<String>().toInstance("Statically Injected")
                        kotlinBinder.requestStaticInjection<StaticInjectionObj>()
                    }
                })

                StaticInjectionObj.staticInjectionSite shouldEqual "Statically Injected"
            }
        }

        describe("#getProvider") {
            it("get a provider for a simple type") {
                Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        kotlinBinder.bind<A>().to<AImpl>()
                        val provider = kotlinBinder.getProvider<A>()
                        provider.toString() shouldEqual "Provider<dev.misfitlabs.kotlinguice4.A>"
                    }
                })
            }

            it("get a provider for an annotated key") {
                Guice.createInjector(object : KotlinModule() {
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
                Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        val membersInjector = kotlinBinder.getMembersInjector<AImpl>()
                        membersInjector.toString() shouldEqual
                                "MembersInjector<dev.misfitlabs.kotlinguice4.AImpl>"
                    }
                })
            }
        }
    }
})
