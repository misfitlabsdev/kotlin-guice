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

import com.google.inject.CreationException
import com.google.inject.Guice
import com.google.inject.Injector
import com.google.inject.Key
import com.google.inject.Stage
import com.google.inject.multibindings.ProvidesIntoMap
import com.google.inject.multibindings.StringMapKey
import com.google.inject.name.Names
import com.google.inject.spi.ElementSource
import com.google.inject.util.Providers
import com.google.inject.util.Types
import dev.misfitlabs.kotlinguice4.KotlinModule
import dev.misfitlabs.kotlinguice4.annotatedKey
import dev.misfitlabs.kotlinguice4.key
import java.lang.reflect.Type
import java.util.concurrent.Callable
import org.amshove.kluent.shouldBeEmpty
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldThrow
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

/**
 * @author John Leacox
 */
object KotlinMapBinderSpec : Spek({
    describe("KotlinMapBinder") {
        it("skips the MapBinder classes in the source trace") {
            val outerModule = object : KotlinModule() {
                override fun configure() {
                    val mapBinder = KotlinMapBinder.newMapBinder<String, A>(kotlinBinder)
                    mapBinder.addBinding("AImpl").to<AImpl>()
                }
            }

            val injector = Guice.createInjector(outerModule)

            val source = injector.getBinding(key<Map<String, A>>())
                    .source as ElementSource
            val stackTraceElement = source.declaringSource as StackTraceElement

            stackTraceElement.className shouldEqual outerModule::class.java.name
        }

        it("binds immutable versions of all guice multimap bindings when using " +
                "permitDuplicates") {
            val module = object : KotlinModule() {
                override fun configure() {
                    KotlinMapBinder.newMapBinder<String, String>(kotlinBinder)
                            .permitDuplicates()
                }
            }

            val injector = Guice.createInjector(module)

            val expectedBindings = setOf(
                    // MutableMap<K, V>
                    Key.get(Types.mapOf(String::class.java, String::class.java)),
                    // Map<K, V>
                    Key.get(KotlinTypes.mapOf(String::class.java, String::class.java)),
                    // MutableMap<K, Provider<V>>
                    Key.get(Types.mapOf(
                            String::class.java,
                            Types.providerOf(String::class.java))),
                    // Map<K, Provider<V>>
                    Key.get(KotlinTypes.mapOf(
                            String::class.java,
                            Types.providerOf(String::class.java))),
                    // MutableMap<K, javax.inject.Provider<V>>
                    Key.get(Types.mapOf(
                            String::class.java,
                            Types.javaxProviderOf(String::class.java))),
                    // Map<K, javax.inject.Provider<V>>
                    Key.get(KotlinTypes.mapOf(
                            String::class.java,
                            Types.javaxProviderOf(String::class.java))),
                    // MutableMap<K, Set<V>>
                    Key.get(Types.mapOf(String::class.java, Types.setOf(String::class.java))),
                    // Map<K, MutableSet<V>>
                    Key.get(KotlinTypes.mapOf(
                            String::class.java,
                            KotlinTypes.setOf(String::class.java))),
                    // MutableMap<K, Set<Provider<V>>
                    Key.get(Types.mapOf(
                            String::class.java,
                            Types.setOf(Types.providerOf(String::class.java)))),
                    // Map<K, MutableSet<Provider<V>>
                    Key.get(KotlinTypes.mapOf(
                            String::class.java,
                            KotlinTypes.setOf(Types.providerOf(String::class.java)))),
                    // MutableMap<K, Set<javax.inject.Provider<V>>
                    Key.get(Types.mapOf(
                            String::class.java,
                            Types.setOf(Types.javaxProviderOf(String::class.java)))),
                    // Map<K, MutableSet<javax.inject.Provider<V>>
                    Key.get(KotlinTypes.mapOf(
                            String::class.java,
                            KotlinTypes.setOf(Types.javaxProviderOf(String::class.java)))),
                    // MutableMap<K, Collection<Provider<V>>
                    Key.get(KotlinTypes.mutableMapOf(
                            String::class.java,
                            KotlinTypes.mutableCollectionOf(
                                    Types.providerOf(String::class.java)))),
                    // Map<K, MutableCollection<Provider<V>>
                    Key.get(KotlinTypes.mapOf(
                            String::class.java,
                            KotlinTypes.collectionOf(Types.providerOf(String::class.java)))),
                    // MutableMap<K, Collection<javax.inject.Provider<V>>
                    Key.get(KotlinTypes.mutableMapOf(
                            String::class.java,
                            KotlinTypes.mutableCollectionOf(
                                    Types.javaxProviderOf(String::class.java)))),
                    // Map<K, Collection<javax.inject.Provider<V>>
                    Key.get(KotlinTypes.mapOf(
                            String::class.java,
                            KotlinTypes.collectionOf(
                                    Types.javaxProviderOf(String::class.java)))),
                    // MutableSet<Map.Entry<K, Provider<V>>>
                    Key.get(KotlinTypes.mutableSetOf(mutableMapEntryOf(
                        String::class.java,
                        Types.providerOf(String::class.java)))),
                    // Set<Map.Entry<K, Provider<V>>>
                    Key.get(KotlinTypes.setOf(mapEntryOf(
                        String::class.java,
                        Types.providerOf(String::class.java)))),
                    // MutableSet<Map.Entry<K, javax.inject.Provider<V>>>
                    Key.get(KotlinTypes.mutableSetOf(mutableMapEntryOf(
                        String::class.java,
                        Types.javaxProviderOf(String::class.java)))),
                    // Set<Map.Entry<K, javax.inject.Provider<V>>>
                    Key.get(KotlinTypes.setOf(mapEntryOf(
                        String::class.java,
                        Types.javaxProviderOf(String::class.java)))),
                    // MutableCollection<Provider<Map.Entry<K, Provider<V>>>>
                    Key.get(KotlinTypes.mutableCollectionOf(Types.providerOf(
                        mutableMapEntryOf(
                            String::class.java,
                            Types.providerOf(String::class.java))))),
                    // Collection<Provider<Map.Entry<K, Provider<V>>>>
                    Key.get(KotlinTypes.collectionOf(Types.providerOf(
                        mapEntryOf(
                            String::class.java,
                            Types.providerOf(String::class.java))))),
                    // MutableCollection<javax.inject.Provider<Map.Entry<K, Provider<V>>>>
                    Key.get(KotlinTypes.mutableCollectionOf(Types.javaxProviderOf(
                        mutableMapEntryOf(
                            String::class.java,
                            Types.providerOf(String::class.java))))),
                    // Collection<javax.inject.Provider<Map.Entry<K, Provider<V>>>>
                    Key.get(KotlinTypes.collectionOf(Types.javaxProviderOf(
                        mapEntryOf(
                            String::class.java,
                            Types.providerOf(String::class.java))))),
                    // @Named(...) Boolean
                    Key.get(Boolean::class.java,
                            Names.named("Multibinder<java.util.Map\$Entry<java.lang.String, " +
                                    "com.google.inject.Provider<java.lang.String>>> " +
                                    "permits duplicates")),
                    Key.get(java.util.logging.Logger::class.java),
                    Key.get(Stage::class.java),
                    Key.get(Injector::class.java)
            )

            injector.bindings.keys shouldEqual expectedBindings
        }

        describe("#newMapBinder") {
            it("binds simple types into a map") {
                val injector = Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        val aBinder = KotlinMapBinder.newMapBinder<String, A>(kotlinBinder)
                        aBinder.addBinding("AImpl").to<AImpl>()
                        aBinder.addBinding("B").to<B>()

                        val callableBinder = KotlinMapBinder
                                .newMapBinder<String, Callable<A>>(kotlinBinder)
                        callableBinder.addBinding("ACallable").to<ACallable>()
                    }
                })

                val map = injector.getInstance(key<Map<String, A>>())
                map.size shouldEqual 2
                map["AImpl"]?.get() shouldEqual "Impl of A"
                map["B"]?.get() shouldEqual "This is B"
            }

            it("binds complex types into a map") {
                val injector = Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        val callableBinder = KotlinMapBinder
                                .newMapBinder<String, Callable<A>>(kotlinBinder)
                        callableBinder.addBinding("ACallable").to<ACallable>()
                        callableBinder.addBinding("TCallable").to<TCallable<A>>()

                        val aBinder = KotlinMapBinder.newMapBinder<String, A>(kotlinBinder)
                        aBinder.addBinding("AImpl").to<AImpl>()
                    }
                })

                val map = injector.getInstance(key<Map<String, Callable<A>>>())
                map.size shouldEqual 2
                map["ACallable"]?.call()?.get() shouldEqual "Impl of A"
                map["TCallable"]?.call()?.get() shouldEqual null
            }

            it("forbids duplicate elements") {
                val module1 = object : KotlinModule() {
                    override fun configure() {
                        val stringBinder = KotlinMapBinder
                                .newMapBinder<String, String>(kotlinBinder)
                        stringBinder.addBinding("Hello").toProvider(Providers.of("World"))
                    }
                }
                val module2 = object : KotlinModule() {
                    override fun configure() {
                        val stringBinder = KotlinMapBinder
                                .newMapBinder<String, String>(kotlinBinder)
                        stringBinder.addBinding("Hello").toInstance("Another World")
                    }
                }

                val createInjector = {
                    Guice.createInjector(module1, module2)
                }
                createInjector shouldThrow CreationException::class
            }

            it("binds duplicates into a multimap when using permitDuplicates") {
                val module1 = object : KotlinModule() {
                    override fun configure() {
                        val stringBinder = KotlinMapBinder
                                .newMapBinder<String, String>(kotlinBinder)
                        stringBinder.addBinding("A").toProvider(Providers.of("a"))
                        stringBinder.addBinding("B").toInstance("b")
                        stringBinder.permitDuplicates()
                    }
                }
                val module2 = object : KotlinModule() {
                    override fun configure() {
                        val stringBinder = KotlinMapBinder
                                .newMapBinder<String, String>(kotlinBinder)
                        stringBinder.addBinding("A").toInstance("A")
                        stringBinder.addBinding("C").toInstance("C")
                        stringBinder.permitDuplicates()
                    }
                }

                val injector = Guice.createInjector(module1, module2)

                val map = injector.getInstance(key<Map<String, Set<String>>>())
                map.size shouldEqual 3
                map["A"]!!.minus(setOf("a", "A")).shouldBeEmpty()
                map["B"]!!.minus(setOf("b")).shouldBeEmpty()
                map["C"]!!.minus(setOf("C")).shouldBeEmpty()
            }
        }

        describe("#newAnnotatedMapBinder") {
            it("binds simple types into a map") {
                val injector = Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        val aBinder = KotlinMapBinder
                                .newAnnotatedMapBinder<String, A, Annotated>(kotlinBinder)
                        aBinder.addBinding("AImpl").to<AImpl>()
                        aBinder.addBinding("B").to<B>()

                        val callableBinder = KotlinMapBinder
                                .newMapBinder<String, Callable<A>>(kotlinBinder)
                        callableBinder.addBinding("ACallable").to<ACallable>()
                    }
                })

                val map = injector.getInstance(annotatedKey<Map<String, A>, Annotated>())
                map.size shouldEqual 2
                map["AImpl"]?.get() shouldEqual "Impl of A"
                map["B"]?.get() shouldEqual "This is B"
            }

            it("binds complex types into a map") {
                val injector = Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        val callableBinder = KotlinMapBinder
                                .newAnnotatedMapBinder<String, Callable<A>, Annotated>(kotlinBinder)
                        callableBinder.addBinding("ACallable").to<ACallable>()
                        callableBinder.addBinding("TCallable").to<TCallable<A>>()

                        val aBinder = KotlinMapBinder.newMapBinder<String, A>(kotlinBinder)
                        aBinder.addBinding("AImpl").to<AImpl>()
                    }
                })

                val map = injector.getInstance(annotatedKey<Map<String, Callable<A>>, Annotated>())
                map.size shouldEqual 2
                map["ACallable"]?.call()?.get() shouldEqual "Impl of A"
                map["TCallable"]?.call()?.get() shouldEqual null
            }

            it("forbids duplicate elements") {
                val module1 = object : KotlinModule() {
                    override fun configure() {
                        val stringBinder = KotlinMapBinder
                                .newAnnotatedMapBinder<String, String, Annotated>(kotlinBinder)
                        stringBinder.addBinding("Hello")
                                .toProvider(Providers.of("World"))
                    }
                }
                val module2 = object : KotlinModule() {
                    override fun configure() {
                        val stringBinder = KotlinMapBinder
                                .newAnnotatedMapBinder<String, String, Annotated>(kotlinBinder)
                        stringBinder.addBinding("Hello").toInstance("Another World")
                    }
                }

                val createInjector = {
                    Guice.createInjector(module1, module2)
                }
                createInjector shouldThrow CreationException::class
            }

            it("binds duplicates into a multimap when using permitDuplicates") {
                val module1 = object : KotlinModule() {
                    override fun configure() {
                        val stringBinder = KotlinMapBinder
                                .newAnnotatedMapBinder<String, String, Annotated>(kotlinBinder)
                        stringBinder.addBinding("A").toProvider(Providers.of("a"))
                        stringBinder.addBinding("B").toInstance("b")
                        stringBinder.permitDuplicates()
                    }
                }
                val module2 = object : KotlinModule() {
                    override fun configure() {
                        val stringBinder = KotlinMapBinder
                                .newAnnotatedMapBinder<String, String, Annotated>(kotlinBinder)
                        stringBinder.addBinding("A").toInstance("A")
                        stringBinder.addBinding("C").toInstance("C")
                        stringBinder.permitDuplicates()
                    }
                }

                val injector = Guice.createInjector(module1, module2)

                val map = injector.getInstance(annotatedKey<Map<String, Set<String>>, Annotated>())
                map.size shouldEqual 3
                map["A"]!!.minus(setOf("a", "A")).shouldBeEmpty()
                map["B"]!!.minus(setOf("b")).shouldBeEmpty()
                map["C"]!!.minus(setOf("C")).shouldBeEmpty()
            }
        }

        describe("#ProvidesIntoMap") {
            it("binds simple types into a map") {
                val injector = Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        install(KotlinMultibindingsScanner.asModule())
                    }

                    @ProvidesIntoMap
                    @StringMapKey("AImpl")
                    fun provideAImpl(): A {
                        return AImpl()
                    }

                    @ProvidesIntoMap
                    @StringMapKey("B")
                    fun provideB(): A {
                        return B()
                    }

                    @ProvidesIntoMap
                    @StringMapKey("ACallable")
                    fun provideACallable(): Callable<A> {
                        return ACallable()
                    }
                })

                val map = injector.getInstance(key<Map<String, A>>())
                map.size shouldEqual 2
                map["AImpl"]?.get() shouldEqual "Impl of A"
                map["B"]?.get() shouldEqual "This is B"
            }

            it("binds complex types into a map") {
                val injector = Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        install(KotlinMultibindingsScanner.asModule())
                    }

                    @ProvidesIntoMap
                    @StringMapKey("ACallable")
                    fun provideACallable(): Callable<A> {
                        return ACallable()
                    }

                    @ProvidesIntoMap
                    @StringMapKey("TCallable")
                    fun provideTCallable(): Callable<A> {
                        return TCallable()
                    }

                    @ProvidesIntoMap
                    @StringMapKey("AImpl")
                    fun provideAImpl(): A {
                        return AImpl()
                    }
                })

                val map = injector.getInstance(key<Map<String, Callable<A>>>())
                map.size shouldEqual 2
                map["ACallable"]?.call()?.get() shouldEqual "Impl of A"
                map["TCallable"]?.call()?.get() shouldEqual null
            }

            it("binds simple types into an annotated map") {
                val injector = Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        install(KotlinMultibindingsScanner.asModule())
                    }

                    @ProvidesIntoMap
                    @StringMapKey("AImpl")
                    @Annotated
                    fun provideAImpl(): A {
                        return AImpl()
                    }

                    @ProvidesIntoMap
                    @StringMapKey("B")
                    @Annotated
                    fun provideB(): A {
                        return B()
                    }

                    @ProvidesIntoMap
                    @StringMapKey("ACallable")
                    fun provideACallable(): Callable<A> {
                        return ACallable()
                    }
                })

                val map = injector.getInstance(annotatedKey<Map<String, A>, Annotated>())
                map.size shouldEqual 2
                map["AImpl"]?.get() shouldEqual "Impl of A"
                map["B"]?.get() shouldEqual "This is B"
            }

            it("binds complex types into an annotated map") {
                val injector = Guice.createInjector(object : KotlinModule() {
                    override fun configure() {
                        install(KotlinMultibindingsScanner.asModule())
                    }

                    @ProvidesIntoMap
                    @StringMapKey("ACallable")
                    @Annotated
                    fun provideACallable(): Callable<A> {
                        return ACallable()
                    }

                    @ProvidesIntoMap
                    @StringMapKey("TCallable")
                    @Annotated
                    fun provideTCallable(): Callable<A> {
                        return TCallable()
                    }

                    @ProvidesIntoMap
                    @StringMapKey("AImpl")
                    fun provideAImpl(): A {
                        return AImpl()
                    }
                })

                val map = injector.getInstance(annotatedKey<Map<String, Callable<A>>, Annotated>())
                map.size shouldEqual 2
                map["ACallable"]?.call()?.get() shouldEqual "Impl of A"
                map["TCallable"]?.call()?.get() shouldEqual null
            }
        }
    }
})

fun mutableMapEntryOf(keyType: Type, valueType: Type): Type {
    return Types.newParameterizedTypeWithOwner(MutableMap::class.java,
            MutableMap.MutableEntry::class.java,
            keyType,
            valueType)
}

fun mapEntryOf(keyType: Type, valueType: Type): Type {
    return Types.newParameterizedTypeWithOwner(Map::class.java,
            Map.Entry::class.java,
            keyType,
            valueType)
}
