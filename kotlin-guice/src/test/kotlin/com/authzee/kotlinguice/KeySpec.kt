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

package com.authzee.kotlinguice

import com.google.inject.Key
import org.amshove.kluent.shouldEqual
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import java.lang.reflect.Type
import java.util.concurrent.Callable

/**
 * @author John Leacox
 */
object KeySpec : Spek({
    describe("#key") {
        it("creates a Key from a simple type") {
            val key = key<A>()
            key shouldEqual Key.get(A::class.java)
        }

        it("creates a Key from a complex type") {
            val key = key<List<Callable<A>>>()
            key shouldEqual Key.get(typeLiteral<List<Callable<A>>>())
        }
    }

    describe("#annotatedKey") {
        it("creates a Key from a simple type and annotation type") {
            val key = annotatedKey<A, Annotated>()
            key shouldEqual Key.get(A::class.java, Annotated::class.java)
        }

        it("creates a Key from a complex type and annotation type") {
            val key = annotatedKey<List<Callable<A>>, Annotated>()
            key shouldEqual Key.get(typeLiteral<List<Callable<A>>>(), Annotated::class.java)
        }

        it("creates a Key from a simple type and annotation instance") {
            val annotationInstance = MembersInjection::class.java
                    .getDeclaredField("annotatedMemberInjectionSite")
                    .getAnnotation(Annotated::class.java)

            val key = annotatedKey<A>(annotationInstance)
            key shouldEqual Key.get(A::class.java, annotationInstance)
        }

        it("creates a Key from a complex type and annotation instance") {
            val annotationInstance = MembersInjection::class.java
                    .getDeclaredField("annotatedMemberInjectionSite")
                    .getAnnotation(Annotated::class.java)

            val key = annotatedKey<List<Callable<A>>>(annotationInstance)
            key shouldEqual Key.get(typeLiteral<List<Callable<A>>>(), annotationInstance)
        }

        it("creates a Key from a java.lang.reflect.Type and annotation type") {
            val stringType: Type = String::class.java

            val key = annotatedKey<Annotated>(stringType)
            key shouldEqual Key.get(stringType, Annotated::class.java)
        }
    }

    describe("#ofType") {
        it("creates a Key from a simple type with the same annotation as the current key") {
            val key = annotatedKey<A, Annotated>()

            val newKey = key.ofType<String>()
            newKey.annotation shouldEqual key.annotation
            newKey shouldEqual Key.get(String::class.java, Annotated::class.java)
        }

        it("creates a Key from a complex type with the same annotation as the current key") {
            val key = annotatedKey<List<Callable<A>>, Annotated>()

            val newKey = key.ofType<Set<Callable<A>>>()
            newKey.annotation shouldEqual key.annotation
            newKey shouldEqual Key.get(typeLiteral<Set<Callable<A>>>(), Annotated::class.java)
        }
    }
})
