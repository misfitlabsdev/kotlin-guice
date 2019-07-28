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

import com.google.inject.TypeLiteral
import java.util.concurrent.Callable
import org.amshove.kluent.shouldEqual
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

/**
 * @author John Leacox
 */
object TypeLiteralSpec : Spek({
    describe("#typeLiteral") {
        it("creates a TypeLiteral from a simple type") {
            val simpleTypeLiteral = typeLiteral<A>()
            simpleTypeLiteral shouldEqual TypeLiteral.get(A::class.java)
        }

        it("creates a TypeLiteral from a complex type") {
            val complexTypeLiteral = typeLiteral<List<Callable<A>>>()
            complexTypeLiteral shouldEqual object : TypeLiteral<List<Callable<A>>>() {}
        }

        it("creates a TypeLiteral from array types") {
            val arrayTypeLiteral = typeLiteral<Array<String>>()
            arrayTypeLiteral shouldEqual object : TypeLiteral<Array<String>>() {}
        }
    }
})
