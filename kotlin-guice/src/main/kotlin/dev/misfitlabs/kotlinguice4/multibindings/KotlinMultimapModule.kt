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

import dev.misfitlabs.kotlinguice4.KotlinModule

/**
 * A module for binding the kotlin multimap types.
 *
 * @author John Leacox
 * @since 1.0
 */
internal class KotlinMultimapModule<K, V>(private val bindingSelection: MapBindingSelection<K, V>) :
    KotlinModule() {
    override fun configure() {
        bind(bindingSelection.multimapKey).to(bindingSelection.mutableMultimapKey)
        bind(bindingSelection.providerSetMultimapKey)
                .to(bindingSelection.mutableProviderSetMultimapKey)
        bind(bindingSelection.javaxProviderSetMultimapKey)
                .to(bindingSelection.mutableJavaxProviderSetMultimapKey)
        bind(bindingSelection.providerCollectionMultimapKey)
                .to(bindingSelection.mutableProviderCollectionMultimapKey)
        bind(bindingSelection.javaxProviderCollectionMultimapKey)
                .to(bindingSelection.mutableJavaxProviderCollectionMultimapKey)
    }
}
