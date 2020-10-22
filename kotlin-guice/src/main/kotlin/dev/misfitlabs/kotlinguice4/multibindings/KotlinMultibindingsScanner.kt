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

import com.google.common.collect.ImmutableSet
import com.google.inject.AbstractModule
import com.google.inject.Binder
import com.google.inject.Key
import com.google.inject.Module
import com.google.inject.multibindings.MapKey
import com.google.inject.multibindings.ProvidesIntoMap
import com.google.inject.multibindings.ProvidesIntoOptional
import com.google.inject.multibindings.ProvidesIntoSet
import com.google.inject.spi.InjectionPoint
import com.google.inject.spi.ModuleAnnotatedMethodScanner
import java.lang.reflect.Method
import kotlin.reflect.full.findAnnotation

/**
 * Scans modules for methods annotated for multibindings, mapbindings, and optional bindings.
 *
 * This scanner provides additional bindings for Kotlin collection types. As of Guice 4.2 the
 * multibinding method scanner is installed by default, but it only works for Java collection
 * types. Install this additional Kotlin multibindings scanner to allow multibindings with
 * Kotlin collection types.
 *
 * @author John Leacox
 * @since 1.0
 */
@ExperimentalStdlibApi
object KotlinMultibindingsScanner {
    /**
     * Returns the scanner as a module.
     *
     * When installed it will scan modules for methods annotated for multibindings, mapbindings,
     * and optional bindings and provide them via Kotlin collection types.
     */
    fun asModule(): Module {
        return object : AbstractModule() {
            override fun configure() {
                binder().scanModulesForAnnotatedMethods(KotlinMultibindingsMethodScanner.INSTANCE)
            }
        }
    }
}

@ExperimentalStdlibApi
private class KotlinMultibindingsMethodScanner : ModuleAnnotatedMethodScanner() {
    companion object {
        val INSTANCE: KotlinMultibindingsMethodScanner by lazy {
            KotlinMultibindingsMethodScanner()
        }
    }

    override fun annotationClasses(): MutableSet<out Class<out Annotation>> {
        return ImmutableSet.of(
            ProvidesIntoSet::class.java,
            ProvidesIntoMap::class.java,
            ProvidesIntoOptional::class.java)
    }

    override fun <T> prepareMethod(
        binder: Binder,
        annotation: Annotation,
        key: Key<T>,
        injectionPoint: InjectionPoint
    ): Key<T> {
        val method = injectionPoint.member as Method
        val mapKeyAnnotation = findMapKeyAnnotation(method)

        return when (annotation) {
            is ProvidesIntoSet -> KotlinMultibinder.newRealSetBinder(binder, key)
                .getKeyForNewItem()
            is ProvidesIntoMap -> {
                if (mapKeyAnnotation == null) {
                    return key
                }
                val typeAndValue: TypeAndValue<Any?> = MapKeys
                    .typeAndValueOfMapKey(mapKeyAnnotation)
                val keyType = typeAndValue.type
                return KotlinMapBinder.newRealMapBinder(binder, keyType, key)
                    .getKeyForNewValue(typeAndValue.value)
            }
            is ProvidesIntoOptional -> {
                return when (annotation.value) {
                    ProvidesIntoOptional.Type.DEFAULT -> KotlinOptionalBinder
                        .newRealOptionalBinder(binder, key).getKeyForDefaultBinding()
                    ProvidesIntoOptional.Type.ACTUAL -> KotlinOptionalBinder
                        .newRealOptionalBinder(binder, key).getKeyForActualBinding()
                }
            }
            else -> throw IllegalStateException("Invalid annotation: $annotation")
        }
    }

    private fun findMapKeyAnnotation(method: Method): Annotation? {
        var foundAnnotation: Annotation? = null
        for (annotation in method.annotations) {
            val mapKey = annotation.annotationClass.findAnnotation<MapKey>()
            if (mapKey != null) {
                if (foundAnnotation != null) {
                    return null
                }
                if (mapKey.unwrapValue) {
                    try {
                        val valueMethod = annotation.annotationClass.java.getDeclaredMethod("value")
                        if (valueMethod.returnType.isArray) {
                            return null
                        }
                    } catch (invalid: NoSuchMethodException) {
                        return null
                    }
                }
                foundAnnotation = annotation
            }
        }
        return foundAnnotation
    }
}
