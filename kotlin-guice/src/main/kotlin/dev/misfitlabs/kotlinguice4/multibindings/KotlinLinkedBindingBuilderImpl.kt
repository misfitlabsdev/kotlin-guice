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

import com.google.inject.binder.LinkedBindingBuilder
import dev.misfitlabs.kotlinguice4.binder.KotlinLinkedBindingBuilder

/**
 * An internal concrete implementation of [KotlinLinkedBindingBuilder] for usage in the multibinding
 * factory methods.
 *
 * @author John Leacox
 * @since 1.0
 */
internal class KotlinLinkedBindingBuilderImpl<T>(delegate: LinkedBindingBuilder<T>) :
    KotlinLinkedBindingBuilder<T>(delegate)
