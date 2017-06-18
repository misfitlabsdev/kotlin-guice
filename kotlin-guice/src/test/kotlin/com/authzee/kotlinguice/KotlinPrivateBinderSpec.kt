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
object KotlinPrivateBinderSpec : Spek({

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

        it("should allow getting an instance of the exposed binding") {
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

        it("should inject an exposed binding into classes using the binding") {
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

        it("should inject a private binding into an exposed class configured in the same module") {
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

        it("should skip the KotlinPrivateBinder class in the source trace") {
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
            it("should bind a custom scope using a scope annotation type parameter") {
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
            it("should bind source using a type parameter") {
                val injector = Guice.createInjector(object : KotlinPrivateModule() {
                    override fun configure() {
                        kotlinBinder.bind<A>().to(AImpl::class.java)

                        kotlinBinder.expose<A>()
                    }
                })

                val a = injector.getInstance(A::class.java)

                a.get() shouldEqual "Impl of A"
            }

            it("should bind a complex source using a type parameter") {
                val injector = Guice.createInjector(object : KotlinPrivateModule() {
                    override fun configure() {
                        kotlinBinder.bind<Callable<A>>().to(ACallable::class.java)

                        kotlinBinder.expose<Callable<A>>()
                    }
                })

                val a = injector.getInstance(key<Callable<A>>())
                a.call().get() shouldEqual "Impl of A"
            }

            it("should bind to a target using a type parameter") {
                val injector = Guice.createInjector(object : KotlinPrivateModule() {
                    override fun configure() {
                        kotlinBinder.bind<A>().to<AImpl>()

                        kotlinBinder.expose<A>()
                    }
                })

                val a = injector.getInstance(A::class.java)

                a.get() shouldEqual "Impl of A"
            }

            it("should bind to a complex target using a type parameter") {
                val injector = Guice.createInjector(object : KotlinPrivateModule() {
                    override fun configure() {
                        kotlinBinder.bind<Callable<A>>().to<TCallable<A>>()

                        kotlinBinder.expose<Callable<A>>()
                    }
                })

                val callable = injector.getInstance(key<Callable<A>>())
                callable.call() shouldEqual null
            }

            it("should bind with an annotation using a type parameter") {
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

            it("should bind to a provider using a type parameter") {
                val injector = Guice.createInjector(object : KotlinPrivateModule() {
                    override fun configure() {
                        kotlinBinder.bind<A>().toProvider<BProvider>()

                        kotlinBinder.expose<A>()
                    }
                })

                val a = injector.getInstance(A::class.java)

                a shouldBeInstanceOf B::class.java
            }

            it("should bind to a complex provider using a type parameter") {
                val injector = Guice.createInjector(object : KotlinPrivateModule() {
                    override fun configure() {
                        kotlinBinder.bind<Iterable<A>>().toProvider<TProvider<List<A>>>()

                        kotlinBinder.expose<Iterable<A>>()
                    }
                })

                val iterable = injector.getInstance(key<Iterable<A>>())
                iterable shouldEqual null
            }

            it("should bind in a scope") {
                val injector = Guice.createInjector(object : KotlinPrivateModule() {
                    override fun configure() {
                        kotlinBinder.bind<A>().to<AImpl>().`in`<Singleton>()

                        kotlinBinder.expose<A>()
                    }
                })

                val a = injector.getInstance(A::class.java)
                a shouldBe injector.getInstance(A::class.java)
            }

            it("should bind wildcard types") {
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

        // TODO: Consider removing the to<T>() method for bindConstant()
//        describe("#bindConstant") {
//            it("should bind to a target using a type parameter and annotation") {
//                class ClassWithConstant @Inject constructor(@Annotated val constant: Class<Nothing>)
//
//                val injector = Guice.createInjector(object : KotlinPrivateModule() {
//                    override fun configure() {
//                        kotlinBinder.bindConstant()
//                                .annotatedWith<Annotated>()
//                                .to<Iterator<*>>()
//
//                        kotlinBinder.expose(annotatedKey<Class<*>, Annotated>())
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
                        kotlinBinder.bind<String>().toInstance("Statically Injected")
                        requestStaticInjection<StaticInjectionObj>()
                    }
                })

                StaticInjectionObj.staticInjectionSite shouldEqual "Statically Injected"
            }

        }

        describe("#requireBinding") {
            val createInjector = {
                Guice.createInjector(object : KotlinPrivateModule() {
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
                Guice.createInjector(object : KotlinPrivateModule() {
                    override fun configure() {
                        kotlinBinder.bind<A>().to<AImpl>()
                        val provider = getProvider<A>()
                        provider.toString() shouldEqual "Provider<com.authzee.kotlinguice.A>"
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
