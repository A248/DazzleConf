
This guide is your one-stop shop for all matters related to version 2 and upgrading from version 1.

### Introduction

When I first wrote DazzleConf, I was amazed at the way it came to life. I'd rounded off a few hard edges, covered up the gross internals, and wrote a fun, usable API. The result is what you see: DazzleConf 1.0, a cute little library that did its job. However, the API was mainly designed around simple loading, with primitives and strings in mind. Custom types were additional but not central, and the library had a couple quirks like over-using annotations.

Rewriting DazzleConf 2.0 allowed me to re-organize its parts and build an API-driven model. The outcome is far more powerful, beautiful, and expansive. It is, I hope, both conceptually simpler and ergonomically superior.

Like any API, however, DazzleConf 2.0 requires some time to become acquainted with it.

## Migrating

### Upgrading serializers

In 1.x, the `ValueSerialiser` interface handles serialization for a single type, without generic parameters.

In order to support wildcards and generics, 2.0 introduces `TypeLiaison`, which is a generics-aware handler for a type. In 2.0, all types are handled by liaisons.

However, 
