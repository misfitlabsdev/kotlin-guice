# ADR 2: Use Package Name and GroupId for Major Version Interoperability

Date: 2017-09-03

## Status

Accepted

## Context

Major version updates to libraries often include breaking API changes. Upgrading an application to a new major version of a library can be difficult since other libraries may also have dependencies on the updated library.

For this library, `kotlin-guice`, the API tracks heavily to the Guice API. When Guice
makes a major version upgrade, this library will also likely need to make a corresponding major version upgrade.

## Decision

We will use the Java package and Maven group ID to allow interoperability of major versions of this library. As described by Jake Wharton in the blog post below, we will accomplish this in three ways. 

1. The Java package name will include the version number.
1. The library name will be part of the group ID.
1. The group ID will include the version number.

Since this library is meant mostly as an extension to the corresponding Guice libraries, we will use the Guice major version. If we need to make a major version change to `kotlin-guice` within a single version of Guice, then we may have to include both version numbers. Until such a situation arises, we do not have to make that decision, but one possible option is `com.authzee.kotlinguice4_2`.

http://jakewharton.com/java-interoperability-policy-for-major-version-updates/

## Consequences

The group ID and package of `kotlin-guice` will be a bit more verbose, but will also provide clarity as to what version of Guice they are intended to be used with. When Guice makes a major version upgrade, `kotlin-guice` can make a corresponding major version change, while still being interoperable with the previous version.
