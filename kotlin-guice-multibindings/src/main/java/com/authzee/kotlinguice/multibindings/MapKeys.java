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

package com.authzee.kotlinguice.multibindings;

import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapKey;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Static utility methods for working with {@link MapKey} annotations.
 *
 * @author John Leacox
 * @since 1.0
 */
final class MapKeys {
  private MapKeys() {
  }

  /**
   * Returns the type and value of the map key annotation.
   *
   * @throws IllegalStateException if the map key value cannot be determined
   */
  @SuppressWarnings("unchecked")
  static TypeAndValue typeAndValueOfMapKey(Annotation mapKeyAnnotation) {
    if (!mapKeyAnnotation.annotationType().getAnnotation(MapKey.class).unwrapValue()) {
      return new TypeAndValue(TypeLiteral.get(mapKeyAnnotation.annotationType()), mapKeyAnnotation);
    } else {
      try {
        Method valueMethod = mapKeyAnnotation.annotationType().getDeclaredMethod("value");
        valueMethod.setAccessible(true);
        TypeLiteral<?> returnType =
            TypeLiteral.get(mapKeyAnnotation.annotationType()).getReturnType(valueMethod);
        return new TypeAndValue(returnType, valueMethod.invoke(mapKeyAnnotation));
      } catch (NoSuchMethodException | SecurityException | IllegalAccessException
          | InvocationTargetException e) {
        throw new IllegalStateException(e);
      }
    }
  }
}
