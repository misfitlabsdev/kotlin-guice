Change Log
==========

## [1.6.0] - _In Development_

### Added
* Add annotation instance support for `KotlinMapBinder`.

### Changed
* Upgrade to Guice 5.1.0

## [1.5.0] - _2021-05-08_

### Changed
* Upgrade to Guice 5.0.1

## [1.4.1] - _2019-11-02_

### Fixed
* Fix `kotlinBinder` holding a cached reference to old `Binder` instances in `KotlinModule`.

## [1.4.0] - _2019-07-27_

### Changed
* Upgrade to Guice 4.2.2.
* Move multibinding files into the main kotlin-guice module.
* Switch to Keep a Changelog style for Changelog moving forward - https://keepachangelog.com/en/1.0.0/
* Rename groupId and packages to `dev.misfitlabs`.
* Upgrade tests from Spek to Spek2.

### Removed
* Remove kotlin-guice-multibindings module to match the removal of guice-multibindings in Guice 4.2.

## Version 1.3.0

_2019-04-25_

* Add delegating builders to binder skipSources.
* Add binding support for annotation instances for kotlin-based DSL.

## Version 1.2.0

_2018-10-17_

* Actually replace dependency on JRE8 with JDK8.

## Version 1.1.0

_2018-10-11_

* Replace dependency on JRE8 with JDK8.

## Version 1.0.0

_2017-09-05_

* Initial release.

[Unreleased]: https://github.com/misfitlabsdev/kotlin-guice/compare/1.4.0...HEAD
[1.4.0]: https://github.com/misfitlabsdev/kotlin-guice/compare/1.3.0...1.4.0
[1.4.1]: https://github.com/misfitlabsdev/kotlin-guice/compare/1.4.0...1.4.1
[1.5.0]: https://gibhub.com/misfitlabsdev/kotlin-guice/compare/1.4.1...1.5.0
