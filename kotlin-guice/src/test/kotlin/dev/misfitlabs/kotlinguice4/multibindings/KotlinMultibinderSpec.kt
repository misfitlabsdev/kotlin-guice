/*
 * Copyright (C) 2017 John Leacox
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

package dev.misfitlabs.kotlinguice4.multibindings

import com.google.inject.Guice
import com.google.inject.Key
import com.google.inject.ProvisionException
import com.google.inject.TypeLiteral
import com.google.inject.multibindings.ProvidesIntoSet
import com.google.inject.name.Names
import com.google.inject.spi.ElementSource
import com.google.inject.util.Providers
import dev.misfitlabs.kotlinguice4.KotlinModule
import java.util.concurrent.Callable
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldThrow
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

/**
 * @author John Leacox
 */
object KotlinMultibinderSpec : Spek({
    describe("KotlinMultibinder") {
        it("skips the multibinder classes in the source trace") {
            val outerModule = object : KotlinModule() {
                override fun configure() {
                    val aBinder = KotlinMultibinder.newSetBinder<A>(kotlinBinder)
                    aBinder.addBinding().to<AImpl>()
                }
            }

            val injector = Guice.createInjector(outerModule)

            val source = injector.getBinding(Key.get(object : TypeLiteral<Set<A>>() {}))
                    .source as ElementSource
            val stackTraceElement = source.declaringSource as StackTraceElement

            stackTraceElement.className shouldEqual outerModule::class.java.name
        }

        describe("#newSetBinder") {
            it("binds simple types into a set") {
                val injector = Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        val aBinder = KotlinMultibinder.newSetBinder<A>(kotlinBinder)
                        aBinder.addBinding().to<AImpl>()
                        aBinder.addBinding().to<B>()

                        val callableBinder = KotlinMultibinder.newSetBinder<Callable<A>>(kotlinBinder)
                        callableBinder.addBinding().to<ACallable>()
                    }
                })

                val setContainer = injector.getInstance(Key
                        .get(object : TypeLiteral<SetContainer<A>>() {}))

                setContainer.set.size shouldEqual 2
            }

            it("binds complex types into a set") {
                val injector = Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        val callableBinder = KotlinMultibinder.newSetBinder<Callable<A>>(kotlinBinder)
                        callableBinder.addBinding().to<ACallable>()
                        callableBinder.addBinding().to<TCallable<A>>()

                        val aBinder = KotlinMultibinder.newSetBinder<A>(kotlinBinder)
                        aBinder.addBinding().to<AImpl>()
                        aBinder.addBinding().to<B>()
                    }
                })

                val setContainer = injector.getInstance(Key
                        .get(object : TypeLiteral<SetContainer<Callable<A>>>() {}))

                setContainer.set.size shouldEqual 2
            }

            it("forbids duplicate elements") {
                val module1 = object : KotlinModule() {
                    override fun configure() {
                        val stringBinder = KotlinMultibinder.newSetBinder<String>(kotlinBinder)
                        stringBinder.addBinding().toProvider(Providers.of("Hello World"))
                    }
                }
                val module2 = object : KotlinModule() {
                    override fun configure() {
                        val stringBinder = KotlinMultibinder.newSetBinder<String>(kotlinBinder)
                        stringBinder.addBinding().toInstance("Hello World")
                    }
                }

                val injector = Guice.createInjector(module1, module2)

                val getInstance = {
                    injector.getInstance(Key.get(object : TypeLiteral<Set<String>>() {}))
                }
                getInstance shouldThrow ProvisionException::class
            }

            it("silently ignores duplicates when using permitDuplicates") {
                val module1 = object : KotlinModule() {
                    override fun configure() {
                        val stringBinder = KotlinMultibinder.newSetBinder<String>(kotlinBinder)
                        stringBinder.addBinding().toProvider(Providers.of("Hello World"))
                    }
                }
                val module2 = object : KotlinModule() {
                    override fun configure() {
                        val stringBinder = KotlinMultibinder.newSetBinder<String>(kotlinBinder)
                        stringBinder.permitDuplicates()
                        stringBinder.addBinding().toInstance("Hello World")
                    }
                }

                val injector = Guice.createInjector(module1, module2)

                val set = injector.getInstance(Key.get(object : TypeLiteral<Set<String>>() {}))
                set.size shouldEqual 1
                set shouldEqual setOf("Hello World")
            }
        }

        describe("#newAnnotatedSetBinder") {
            it("binds simple types into an annotated set") {
                val injector = Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        val aBinder = KotlinMultibinder
                                .newAnnotatedSetBinder<A, Annotated>(kotlinBinder)
                        aBinder.addBinding().to<AImpl>()
                        aBinder.addBinding().to<B>()

                        val unannotatedABinder = KotlinMultibinder.newSetBinder<A>(kotlinBinder)
                        unannotatedABinder.addBinding().to<AImpl>()
                    }
                })

                val set = injector.getInstance(Key
                        .get(object : TypeLiteral<Set<A>>() {}, Annotated::class.java))

                set.size shouldEqual 2
            }

            it("binds simple types into a named set") {
                val named = Names.named("A Name")

                val injector = Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        val aBinder = KotlinMultibinder
                            .newAnnotatedSetBinder<A>(kotlinBinder, named)
                        aBinder.addBinding().to<AImpl>()
                        aBinder.addBinding().to<B>()

                        val unannotatedABinder = KotlinMultibinder.newSetBinder<A>(kotlinBinder)
                        unannotatedABinder.addBinding().to<AImpl>()
                    }
                })

                val set = injector.getInstance(Key.get(object : TypeLiteral<Set<A>>() {}, named))

                set.size shouldEqual 2
            }

            it("binds complex types into an annotated set") {
                val injector = Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        val callableBinder = KotlinMultibinder
                                .newAnnotatedSetBinder<Callable<A>, Annotated>(kotlinBinder)
                        callableBinder.addBinding().to<ACallable>()
                        callableBinder.addBinding().to<TCallable<A>>()

                        val unannotatedCallableBinder = KotlinMultibinder
                                .newSetBinder<Callable<A>>(kotlinBinder)
                        unannotatedCallableBinder.addBinding().to<ACallable>()
                    }
                })

                val set = injector.getInstance(Key
                        .get(object : TypeLiteral<Set<Callable<A>>>() {},
                                Annotated::class.java))

                set.size shouldEqual 2
            }
        }

        describe("@ProvidesIntoSet") {
            it("binds simple types into a set") {
                val injector = Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        install(KotlinMultibindingsScanner.asModule())
                    }

                    @ProvidesIntoSet
                    fun provideAImpl(): A {
                        return AImpl()
                    }

                    @ProvidesIntoSet
                    fun provideB(): A {
                        return B()
                    }

                    @ProvidesIntoSet
                    fun provideACallable(): Callable<A> {
                        return ACallable()
                    }
                })

                val setContainer = injector.getInstance(Key
                        .get(object : TypeLiteral<SetContainer<A>>() {}))

                setContainer.set.size shouldEqual 2
            }

            it("binds complex types into a set") {
                val injector = Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        install(KotlinMultibindingsScanner.asModule())
                    }

                    @ProvidesIntoSet
                    fun provideACallable(): Callable<A> {
                        return ACallable()
                    }

                    @ProvidesIntoSet
                    fun provideTCallable(): Callable<A> {
                        return TCallable<A>()
                    }

                    @ProvidesIntoSet
                    fun provideAImpl(): A {
                        return AImpl()
                    }
                })

                val setContainer = injector.getInstance(Key
                        .get(object : TypeLiteral<SetContainer<Callable<A>>>() {}))

                setContainer.set.size shouldEqual 2
            }

            it("binds simple types into an annotated set") {
                val injector = Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        install(KotlinMultibindingsScanner.asModule())
                    }

                    @ProvidesIntoSet
                    @Annotated
                    fun provideAImpl(): A {
                        return AImpl()
                    }

                    @ProvidesIntoSet
                    @Annotated
                    fun provideB(): A {
                        return B()
                    }

                    @ProvidesIntoSet
                    fun provideACallable(): Callable<A> {
                        return ACallable()
                    }
                })

                val set = injector.getInstance(Key
                        .get(object : TypeLiteral<Set<A>>() {},
                                Annotated::class.java))

                set.size shouldEqual 2
            }

            it("binds complex types into an annotated set") {
                val injector = Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        install(KotlinMultibindingsScanner.asModule())
                    }

                    @ProvidesIntoSet
                    @Annotated
                    fun provideAnnotatedACallable(): Callable<A> {
                        return ACallable()
                    }

                    @ProvidesIntoSet
                    @Annotated
                    fun provideAnnotatedTCallable(): Callable<A> {
                        return TCallable<A>()
                    }

                    @ProvidesIntoSet
                    fun provideUnannotatedACallable(): Callable<A> {
                        return ACallable()
                    }
                })

                val set = injector.getInstance(Key
                        .get(object : TypeLiteral<Set<Callable<A>>>() {},
                                Annotated::class.java))

                set.size shouldEqual 2
            }
        }
    }
})
