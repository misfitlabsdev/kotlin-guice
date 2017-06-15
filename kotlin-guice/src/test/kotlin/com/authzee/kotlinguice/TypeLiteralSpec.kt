package com.authzee.kotlinguice

import com.google.inject.TypeLiteral
import org.amshove.kluent.shouldEqual
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import java.util.concurrent.Callable

/**
 * @author John Leacox
 */
object TypeLiteralSpec : Spek({
    describe("#typeLiteral") {
        it("should create a TypeLiteral from a simple type") {
            val simpleTypeLiteral = typeLiteral<A>()
            simpleTypeLiteral shouldEqual TypeLiteral.get(A::class.java)
        }

        it("should create a TypeLiteral from a complex type") {
            val complexTypeLiteral = typeLiteral<List<Callable<A>>>()
            complexTypeLiteral shouldEqual object : TypeLiteral<List<Callable<A>>>() {}
        }

        it("should create a TypeLiteral from array types") {
            val arrayTypeLiteral = typeLiteral<Array<String>>()
            arrayTypeLiteral shouldEqual object : TypeLiteral<Array<String>>() {}
        }
    }
})
