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

package com.authzee.kotlinguice.multibindings

import com.authzee.kotlinguice.KotlinModule
import com.authzee.kotlinguice.annotatedKey
import com.google.inject.CreationException
import com.google.inject.Guice
import com.google.inject.Key
import com.google.inject.TypeLiteral
import com.google.inject.multibindings.ProvidesIntoOptional
import com.google.inject.multibindings.ProvidesIntoOptional.Type
import com.google.inject.spi.ElementSource
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldThrow
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import java.util.Optional
import java.util.concurrent.Callable

/**
 * @author John Leacox
 */
object KotlinOptionalBinderSpec : Spek({
    describe("KotlinOptionalBinder") {
        it("should skip the optional binder classes in the source trace") {
            val outerModule = object : KotlinModule() {
                override fun configure() {
                    val aBinder = KotlinOptionalBinder.newOptionalBinder<A>(kotlinBinder)
                    aBinder.setBinding().to<AImpl>()
                }
            }

            val injector = Guice.createInjector(outerModule)

            val source = injector.getBinding(Key.get(object : TypeLiteral<Optional<A>>() {}))
                    .source as ElementSource
            val stackTraceElement = source.declaringSource as StackTraceElement

            stackTraceElement.className shouldEqual outerModule::class.java.name
        }

        describe("#newOptionalBinder") {
            it("binds a simple type as default optional") {
                val injector = Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        val aBinder = KotlinOptionalBinder.newOptionalBinder<A>(kotlinBinder)
                        aBinder.setDefault().to<AImpl>()
                    }
                })

                val optional = injector.getInstance(Key.get(object : TypeLiteral<Optional<A>>() {}))
                optional.get().get() shouldEqual "Impl of A"
            }

            it("binds a simple type as optional overriding the default") {
                val defaultModule = object : KotlinModule() {
                    override fun configure() {
                        val aBinder = KotlinOptionalBinder.newOptionalBinder<A>(kotlinBinder)
                        aBinder.setDefault().to<AImpl>()
                    }
                }

                val bindingModule = object : KotlinModule() {
                    override fun configure() {
                        val aBinder = KotlinOptionalBinder.newOptionalBinder<A>(kotlinBinder)
                        aBinder.setBinding().to<B>()
                    }
                }

                val injector = Guice.createInjector(defaultModule, bindingModule)

                val optional = injector.getInstance(Key.get(object : TypeLiteral<Optional<A>>() {}))
                optional.get().get() shouldEqual "This is B"
            }

            it("binds a complex type as default optional") {
                val injector = Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        val aBinder = KotlinOptionalBinder
                                .newOptionalBinder<Callable<A>>(kotlinBinder)
                        aBinder.setDefault().to<ACallable>()
                    }
                })

                val optional = injector.getInstance(
                        Key.get(object : TypeLiteral<Optional<Callable<A>>>() {}))
                optional.get().call().get() shouldEqual "Impl of A"
            }

            it("binds a complex type as optional overriding the default") {
                val defaultModule = object : KotlinModule() {
                    override fun configure() {
                        val aBinder = KotlinOptionalBinder
                                .newOptionalBinder<Callable<A>>(kotlinBinder)
                        aBinder.setDefault().to<ACallable>()
                    }
                }

                val bindingModule = object : KotlinModule() {
                    override fun configure() {
                        val aBinder = KotlinOptionalBinder
                                .newOptionalBinder<Callable<A>>(kotlinBinder)
                        aBinder.setBinding().to<BCallable>()
                    }
                }

                val injector = Guice.createInjector(defaultModule, bindingModule)

                val optional = injector.getInstance(
                        Key.get(object : TypeLiteral<Optional<Callable<A>>>() {}))
                optional.get().call().get() shouldEqual "This is B"
            }

            it("forbids duplicate defaults for an optional type") {
                val module1 = object : KotlinModule() {
                    override fun configure() {
                        val aBinder = KotlinOptionalBinder.newOptionalBinder<A>(kotlinBinder)
                        aBinder.setBinding().to<AImpl>()
                    }
                }

                val module2 = object : KotlinModule() {
                    override fun configure() {
                        val aBinder = KotlinOptionalBinder.newOptionalBinder<A>(kotlinBinder)
                        aBinder.setBinding().to<B>()
                    }
                }

                val createInjector = {
                    Guice.createInjector(module1, module2)
                }
                createInjector shouldThrow CreationException::class
            }

            it("forbids duplicate bindings for an optional type") {
                val module1 = object : KotlinModule() {
                    override fun configure() {
                        val aBinder = KotlinOptionalBinder.newOptionalBinder<A>(kotlinBinder)
                        aBinder.setBinding().to<AImpl>()
                    }
                }

                val module2 = object : KotlinModule() {
                    override fun configure() {
                        val aBinder = KotlinOptionalBinder.newOptionalBinder<A>(kotlinBinder)
                        aBinder.setBinding().to<B>()
                    }
                }

                val createInjector = {
                    Guice.createInjector(module1, module2)
                }
                createInjector shouldThrow CreationException::class
            }
        }

        describe("#newAnnotatedOptionalBinder") {
            it("binds an annotated simple type as default optional") {
                val injector = Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        val aBinder = KotlinOptionalBinder
                                .newAnnotatedOptionalBinder<A, Annotated>(kotlinBinder)
                        aBinder.setDefault().to<AImpl>()
                    }
                })

                val optional = injector.getInstance(annotatedKey<Optional<A>, Annotated>())
                optional.get().get() shouldEqual "Impl of A"
            }

            it("binds an annotated simple type as optional overriding the default") {
                val defaultModule = object : KotlinModule() {
                    override fun configure() {
                        val aBinder = KotlinOptionalBinder
                                .newAnnotatedOptionalBinder<A, Annotated>(kotlinBinder)
                        aBinder.setDefault().to<AImpl>()
                    }
                }

                val bindingModule = object : KotlinModule() {
                    override fun configure() {
                        val aBinder = KotlinOptionalBinder
                                .newAnnotatedOptionalBinder<A, Annotated>(kotlinBinder)
                        aBinder.setBinding().to<B>()
                    }
                }

                val injector = Guice.createInjector(defaultModule, bindingModule)

                val optional = injector.getInstance(annotatedKey<Optional<A>, Annotated>())
                optional.get().get() shouldEqual "This is B"
            }

            it("binds an annotated complex type as default optional") {
                val injector = Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        val aBinder = KotlinOptionalBinder
                                .newAnnotatedOptionalBinder<Callable<A>, Annotated>(kotlinBinder)
                        aBinder.setDefault().to<ACallable>()
                    }
                })

                val optional = injector
                        .getInstance(annotatedKey<Optional<Callable<A>>, Annotated>())
                optional.get().call().get() shouldEqual "Impl of A"
            }

            it("binds an annotated complex type as optional overriding the default") {
                val defaultModule = object : KotlinModule() {
                    override fun configure() {
                        val aBinder = KotlinOptionalBinder
                                .newAnnotatedOptionalBinder<Callable<A>, Annotated>(kotlinBinder)
                        aBinder.setDefault().to<ACallable>()
                    }
                }

                val bindingModule = object : KotlinModule() {
                    override fun configure() {
                        val aBinder = KotlinOptionalBinder
                                .newAnnotatedOptionalBinder<Callable<A>, Annotated>(kotlinBinder)
                        aBinder.setBinding().to<BCallable>()
                    }
                }

                val injector = Guice.createInjector(defaultModule, bindingModule)

                val optional = injector
                        .getInstance(annotatedKey<Optional<Callable<A>>, Annotated>())
                optional.get().call().get() shouldEqual "This is B"
            }

            it("forbids duplicate defaults for an annotated optional type") {
                val module1 = object : KotlinModule() {
                    override fun configure() {
                        val aBinder = KotlinOptionalBinder
                                .newAnnotatedOptionalBinder<A, Annotated>(kotlinBinder)
                        aBinder.setBinding().to<AImpl>()
                    }
                }

                val module2 = object : KotlinModule() {
                    override fun configure() {
                        val aBinder = KotlinOptionalBinder
                                .newAnnotatedOptionalBinder<A, Annotated>(kotlinBinder)
                        aBinder.setBinding().to<B>()
                    }
                }

                val createInjector = {
                    Guice.createInjector(module1, module2)
                }
                createInjector shouldThrow CreationException::class
            }

            it("forbids duplicate bindings for an annotated optional type") {
                val module1 = object : KotlinModule() {
                    override fun configure() {
                        val aBinder = KotlinOptionalBinder
                                .newAnnotatedOptionalBinder<A, Annotated>(kotlinBinder)
                        aBinder.setBinding().to<AImpl>()
                    }
                }

                val module2 = object : KotlinModule() {
                    override fun configure() {
                        val aBinder = KotlinOptionalBinder
                                .newAnnotatedOptionalBinder<A, Annotated>(kotlinBinder)
                        aBinder.setBinding().to<B>()
                    }
                }

                val createInjector = {
                    Guice.createInjector(module1, module2)
                }
                createInjector shouldThrow CreationException::class
            }
        }
    }

    describe("@ProvidesIntoOptional") {
        it("binds a simple type as default optional") {
            val injector = Guice.createInjector(object : KotlinModule() {
                override fun configure() {
                    install(KotlinMultibindingsScanner.asModule())
                }

                @ProvidesIntoOptional(Type.DEFAULT)
                fun provideAImplDefault(): A {
                    return AImpl()
                }
            })

            val optional = injector.getInstance(Key.get(object : TypeLiteral<Optional<A>>() {}))
            optional.get().get() shouldEqual "Impl of A"
        }

        it("binds a simple type as optional overriding the default") {
            val defaultModule = object : KotlinModule() {
                override fun configure() {
                    install(KotlinMultibindingsScanner.asModule())
                }

                @ProvidesIntoOptional(Type.DEFAULT)
                fun provideAImplDefault(): A {
                    return AImpl()
                }
            }

            val bindingModule = object : KotlinModule() {
                override fun configure() {
                    install(KotlinMultibindingsScanner.asModule())
                }

                @ProvidesIntoOptional(Type.ACTUAL)
                fun provideBBinding(): A {
                    return B()
                }
            }

            val injector = Guice.createInjector(defaultModule, bindingModule)

            val optional = injector.getInstance(Key.get(object : TypeLiteral<Optional<A>>() {}))
            optional.get().get() shouldEqual "This is B"
        }

        it("binds a complex type as default optional") {
            val injector = Guice.createInjector(object : KotlinModule() {
                override fun configure() {
                    install(KotlinMultibindingsScanner.asModule())
                }

                @ProvidesIntoOptional(Type.DEFAULT)
                fun provideACallableActual(): Callable<A> {
                    return ACallable()
                }
            })

            val optional = injector.getInstance(
                    Key.get(object : TypeLiteral<Optional<Callable<A>>>() {}))
            optional.get().call().get() shouldEqual "Impl of A"
        }

        it("binds a complex type as optional overriding the default") {
            val defaultModule = object : KotlinModule() {
                override fun configure() {
                    install(KotlinMultibindingsScanner.asModule())
                }

                @ProvidesIntoOptional(Type.DEFAULT)
                fun provideACallableDefault(): Callable<A> {
                    return ACallable()
                }
            }

            val bindingModule = object : KotlinModule() {
                override fun configure() {
                    install(KotlinMultibindingsScanner.asModule())
                }

                @ProvidesIntoOptional(Type.ACTUAL)
                fun provideBCallableActual(): Callable<A> {
                    return BCallable()
                }
            }

            val injector = Guice.createInjector(defaultModule, bindingModule)

            val optional = injector.getInstance(
                    Key.get(object : TypeLiteral<Optional<Callable<A>>>() {}))
            optional.get().call().get() shouldEqual "This is B"
        }

        it("forbids duplicate defaults for an optional type") {
            val module1 = object : KotlinModule() {
                override fun configure() {
                    install(KotlinMultibindingsScanner.asModule())
                }

                @ProvidesIntoOptional(Type.DEFAULT)
                fun provideAImplDefault(): A {
                    return AImpl()
                }
            }

            val module2 = object : KotlinModule() {
                override fun configure() {
                    install(KotlinMultibindingsScanner.asModule())
                }

                @ProvidesIntoOptional(Type.DEFAULT)
                fun provideBDefault(): A {
                    return B()
                }
            }

            val createInjector = {
                Guice.createInjector(module1, module2)
            }
            createInjector shouldThrow CreationException::class
        }

        it("forbids duplicate bindings for an optional type") {
            val module1 = object : KotlinModule() {
                override fun configure() {
                    install(KotlinMultibindingsScanner.asModule())
                }

                @ProvidesIntoOptional(Type.ACTUAL)
                fun provideAImplActual(): A {
                    return AImpl()
                }
            }

            val module2 = object : KotlinModule() {
                override fun configure() {
                    install(KotlinMultibindingsScanner.asModule())
                }

                @ProvidesIntoOptional(Type.ACTUAL)
                fun provideBActual(): A {
                    return B()
                }
            }

            val createInjector = {
                Guice.createInjector(module1, module2)
            }
            createInjector shouldThrow CreationException::class
        }

        it("binds an annotated simple type as default optional") {
            val injector = Guice.createInjector(object : KotlinModule() {
                override fun configure() {
                    install(KotlinMultibindingsScanner.asModule())
                }

                @Annotated
                @ProvidesIntoOptional(Type.DEFAULT)
                fun provideAImplDefault(): A {
                    return AImpl()
                }
            })

            val optional = injector.getInstance(annotatedKey<Optional<A>, Annotated>())
            optional.get().get() shouldEqual "Impl of A"
        }

        it("binds an annotated simple type as optional overriding the default") {
            val defaultModule = object : KotlinModule() {
                override fun configure() {
                    install(KotlinMultibindingsScanner.asModule())
                }

                @Annotated
                @ProvidesIntoOptional(Type.DEFAULT)
                fun provideAImplDefault(): A {
                    return AImpl()
                }
            }

            val bindingModule = object : KotlinModule() {
                override fun configure() {
                    install(KotlinMultibindingsScanner.asModule())
                }

                @Annotated
                @ProvidesIntoOptional(Type.ACTUAL)
                fun provideBBinding(): A {
                    return B()
                }
            }

            val injector = Guice.createInjector(defaultModule, bindingModule)

            val optional = injector.getInstance(annotatedKey<Optional<A>, Annotated>())
            optional.get().get() shouldEqual "This is B"
        }

        it("binds an annotated complex type as default optional") {
            val injector = Guice.createInjector(object : KotlinModule() {
                override fun configure() {
                    install(KotlinMultibindingsScanner.asModule())
                }

                @Annotated
                @ProvidesIntoOptional(Type.DEFAULT)
                fun provideACallableActual(): Callable<A> {
                    return ACallable()
                }
            })

            val optional = injector
                    .getInstance(annotatedKey<Optional<Callable<A>>, Annotated>())
            optional.get().call().get() shouldEqual "Impl of A"
        }

        it("binds an annotated complex type as optional overriding the default") {
            val defaultModule = object : KotlinModule() {
                override fun configure() {
                    install(KotlinMultibindingsScanner.asModule())
                }

                @Annotated
                @ProvidesIntoOptional(Type.DEFAULT)
                fun provideACallableDefault(): Callable<A> {
                    return ACallable()
                }
            }

            val bindingModule = object : KotlinModule() {
                override fun configure() {
                    install(KotlinMultibindingsScanner.asModule())
                }

                @Annotated
                @ProvidesIntoOptional(Type.ACTUAL)
                fun provideBCallableActual(): Callable<A> {
                    return BCallable()
                }
            }

            val injector = Guice.createInjector(defaultModule, bindingModule)

            val optional = injector
                    .getInstance(annotatedKey<Optional<Callable<A>>, Annotated>())
            optional.get().call().get() shouldEqual "This is B"
        }

        it("forbids duplicate defaults for an annotated optional type") {
            val module1 = object : KotlinModule() {
                override fun configure() {
                    install(KotlinMultibindingsScanner.asModule())
                }

                @Annotated
                @ProvidesIntoOptional(Type.DEFAULT)
                fun provideAImplDefault(): A {
                    return AImpl()
                }
            }

            val module2 = object : KotlinModule() {
                override fun configure() {
                    install(KotlinMultibindingsScanner.asModule())
                }

                @Annotated
                @ProvidesIntoOptional(Type.DEFAULT)
                fun provideBDefault(): A {
                    return B()
                }
            }

            val createInjector = {
                Guice.createInjector(module1, module2)
            }
            createInjector shouldThrow CreationException::class
        }

        it("forbids duplicate bindings for an annotated optional type") {
            val module1 = object : KotlinModule() {
                override fun configure() {
                    install(KotlinMultibindingsScanner.asModule())
                }

                @Annotated
                @ProvidesIntoOptional(Type.ACTUAL)
                fun provideAImplActual(): A {
                    return AImpl()
                }
            }

            val module2 = object : KotlinModule() {
                override fun configure() {
                    install(KotlinMultibindingsScanner.asModule())
                }

                @Annotated
                @ProvidesIntoOptional(Type.ACTUAL)
                fun provideBActual(): A {
                    return B()
                }
            }

            val createInjector = {
                Guice.createInjector(module1, module2)
            }
            createInjector shouldThrow CreationException::class
        }
    }
})
