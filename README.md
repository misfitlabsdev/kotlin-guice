kotlin-guice
============

Guice extensions for Kotlin. This provides extension wrappers and extension methods for providing a better Guice DSL experience from Kotlin. It takes advantage of reified types to reduce class references like `bind(MyResource::class.java)` to `bind<MyResource>()`.

## Download

Download the latest JAR via Maven:

```xml
<dependency>
  <groupId>dev.misfitlabs.kotlinguice4</groupId>
  <artifactId>kotlin-guice</artifactId>
  <version>1.6.0</version>
</dependency>
```

or Gradle:

```gradle
compile 'dev.misfitlabs.kotlinguice4:kotlin-guice:1.6.0'
```

## Getting Started

### KotlinModule

Use `KotlinModule` for Guice modules instead of `AbstractModule` to take advantage of the enhanced Kotlin Guice DSL. 

```kotlin
import dev.misfitlabs.kotlinguice4.KotlinModule

class MyModule : KotlinModule() {
    override fun configure() {
        bind<Service>().to<ServiceImpl>().`in`<Singleton>()
        bind<PaymentService<CreditCard>>().to<CreditCardPaymentService>()
        bind<CreditCardProcessor>().annotatedWith<PayPal>().to<PayPalCreditCardProcessor>()
    }
}
```

The `KotlinPrivateModule` can also be used if only some bindings need to be exposed.

```kotlin
import dev.misfitlabs.kotlinguice4.KotlinPrivateModule

class MyPrivateModule : KotlinPrivateModule() {
    override fun configure() {
        bind<Service>().to<ServiceImpl>().`in`<Singleton>()
        bind<PaymentService<CreditCard>>().to<CreditCardPaymentService>()
        bind<CreditCardProcessor>().annotatedWith<PayPal>().to<PayPalCreditCardProcessor>()
        
        expose<PaymentService<CreditCard>>()
    }
}
```

### Injector

The Guice injector has been enhanced with extension methods to make direct use of the injector better from Kotlin.

```kotlin
import dev.misfitlabs.kotlinguice4.annotatedKey
import dev.misfitlabs.kotlinguice4.getInstance

fun main(args: Array<String>) {
  val injector = Guice.createInjector(MyModule(), MyPrivateModule())
  
  val paymentService = injector.getInstance<PaymentService<CreditCard>>()
  
  // Use the annotatedKey to get an annotated instance
  val payPalProcessor = injector.getInstance(annotatedKey<CreditCardProcessor, PayPayl>())
}
```

### Key and TypeLiteral

Package level functions are included to enhance creating `Key` and `TypeLiteral` instances from kotlin.

```kotlin
import dev.misfitlabs.kotlinguice4.annotatedKey
import dev.misfitlabs.kotlinguice4.key
import dev.misfitlabs.kotlinguice4.typeLiteral

val key = key<String>()
val annotatedKey = annotatedKey<String, SomeAnnotation>()
val sameAnnotatedDifferentKey = annotatedKey.getType<Long>()

val listType = typeLiteral<PaymentService<CreditCrd>>()
```

### Multibindings

As of version 1.4.1 the kotlin-guice-multibindings module is gone and the functionality has been merged into kotlin-guice.

#### Usage

```kotlin
val multibinder = KotlinMultibinder.newSetBinder<Snack>(kotlinBinder)
multibinder.addBinding().to<Twix>()

val mapbinder = KotlinMapBinder.newMapBinder<String, Snack>(kotlinBinder)
mapbinder.addBinding("twix").to<Twix>()
```

With Guice 4.2+, scanning for methods with the multibinding annotations `ProvidesIntoSet`, `ProvidesIntoMap`, and `ProvidesIntoOptional` is enabled by default. However, the default scanner only provides bindings for Java collection types. In order to get bindings for Kotlin collection types, install the `KotlinMultibindingsScanner`.

```kotlin
install(KotlinMultibindingsScanner.asModule())
```

## License

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
