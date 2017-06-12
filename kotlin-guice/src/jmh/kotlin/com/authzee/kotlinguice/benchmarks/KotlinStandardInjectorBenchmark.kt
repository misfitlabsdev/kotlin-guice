package com.authzee.kotlinguice.benchmarks

import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.Key
import com.google.inject.TypeLiteral
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.CompilerControl
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.Warmup
import java.util.concurrent.TimeUnit

/**
 * Benchmarks showing the performance of Guice bindings from Kotlin without using the kotlin-guice
 * library extensions.
 *
 * @author John Leacox
 */
@Fork(1)
@Warmup(iterations = 10)
@Measurement(iterations = 10)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@CompilerControl(CompilerControl.Mode.DONT_INLINE)
open class KotlinStandardInjectorBenchmark {
    @Benchmark fun getSimpleInstance() {
        val injector = Guice.createInjector(object : AbstractModule() {
            override fun configure() {
                bind(Simple::class.java).to(SimpleImpl::class.java)
            }
        })

        val instance = injector.getInstance(Simple::class.java)
        instance.value()
    }

    @Benchmark fun getComplexIterableInstance() {
        val injector = Guice.createInjector(object : AbstractModule() {
            override fun configure() {
                bind(object : TypeLiteral<Complex<Iterable<String>>>() {})
                        .to(object : TypeLiteral<ComplexImpl<Iterable<String>>>() {})
            }
        })

        val instance = injector
                .getInstance(Key.get(object : TypeLiteral<Complex<Iterable<String>>>() {}))
        instance.value()
    }

    @Benchmark fun getComplexStringInstance() {
        val injector = Guice.createInjector(object : AbstractModule() {
            override fun configure() {
                bind(object : TypeLiteral<Complex<String>>() {}).to(StringComplexImpl::class.java)
            }
        })

        val instance = injector.getInstance(Key.get(object : TypeLiteral<Complex<String>>() {}))
        instance.value()
    }
}
