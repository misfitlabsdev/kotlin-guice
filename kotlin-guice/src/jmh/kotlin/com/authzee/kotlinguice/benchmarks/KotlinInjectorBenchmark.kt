package com.authzee.kotlinguice.benchmarks

import com.authzee.kotlinguice.KotlinModule
import com.authzee.kotlinguice.getInstance
import com.google.inject.Guice
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
 * Benchmarks showing the performance of Guice bindings from Kotlin when using the kotlin-guice
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
open class KotlinInjectorBenchmark {
    @Benchmark fun getSimpleInstance() {
        val injector = Guice.createInjector(object : KotlinModule() {
            override fun configure() {
                bind<Simple>().to<SimpleImpl>()
            }
        })

        val instance = injector.getInstance<Simple>()
        instance.value()
    }

    @Benchmark fun getComplexIterableInstance() {
        val injector = Guice.createInjector(object : KotlinModule() {
            override fun configure() {
                bind<Complex<Iterable<String>>>().to<ComplexImpl<Iterable<String>>>()
            }
        })

        val instance = injector.getInstance<Complex<Iterable<String>>>()
        instance.value()
    }

    @Benchmark fun getComplexStringInstance() {
        val injector = Guice.createInjector(object : KotlinModule() {
            override fun configure() {
                bind<Complex<String>>().to<StringComplexImpl>()
            }
        })

        val instance = injector.getInstance<Complex<String>>()
        instance.value()
    }

}
