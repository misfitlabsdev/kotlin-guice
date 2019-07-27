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

package com.authzee.kotlinguice4.benchmarks

import com.authzee.kotlinguice4.KotlinModule
import com.authzee.kotlinguice4.getInstance
import com.google.inject.Guice
import java.util.concurrent.TimeUnit
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
